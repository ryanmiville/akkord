package akkord.api.actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.stream.Materializer
import akkord.api.circesupport.CirceBodyReadable
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.{StandaloneWSRequest, StandaloneWSResponse}

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

abstract class DiscordApiActor(token: String)(implicit mat: Materializer)
  extends Actor
  with ActorLogging
  with CirceBodyReadable {
  import DiscordApiActor._

  implicit protected val ec: ExecutionContext = context.system.dispatcher
  implicit protected val system: ActorSystem  = context.system

  protected val wsClient: StandaloneAhcWSClient   = StandaloneAhcWSClient()
  protected val reqHeaders: Seq[(String, String)] = requestHeaders(token)

  private val rateLimits = mutable.Map[String, RateLimit]()

  override def receive: Receive =
    tellHttpApiRequest orElse
    sendRequestWithRateLimiting

  def sendRequestWithRateLimiting: Receive = {
    case req: HttpApiRequest => sender ! sendRequest(req)
  }

  private def sendRequest(req: HttpApiRequest): Either[RateLimited, StandaloneWSResponse] = {
    getMajorEndpoint(req).map { ep =>
      val resp = Await.result(req.request.execute(), Duration.Inf)
      log.info(s"response code: ${resp.status}")
      updateRateLimits(ep, resp)
    }
  }

  private def updateRateLimits(endpoint: String, resp: StandaloneWSResponse): StandaloneWSResponse = {
    val remaining = resp.header(remainingHeader).map(_.toInt)
    val reset     = resp.header(resetHeader).map(_.toInt)

    val rateLimit = for {
      rem <- remaining
      res <- reset
    } yield RateLimit(rem, res)

    rateLimit foreach { r => rateLimits(endpoint) = r }
    resp
  }

  private def getMajorEndpoint(request: HttpApiRequest): Either[RateLimited, String] = {
    val majorEndpoint = request match {
      case ChannelRequest(id, _) => s"channel/$id"
    }
    if (isRateLimited(majorEndpoint)) {
      log.info(s"$majorEndpoint is being rate limited")
      Left(RateLimited())
    } else {
      Right(majorEndpoint)
    }
  }

  def isRateLimited(majorEndpoint: String): Boolean = {
    val rateLimit   = rateLimits.getOrElse(majorEndpoint, RateLimit(Int.MaxValue, 0))
    val currentTime = System.currentTimeMillis / 1000

    rateLimit.remaining < 1 && currentTime < rateLimit.reset
  }

  def tellHttpApiRequest: Receive
}

object DiscordApiActor {
  trait HttpApiRequest { val request: StandaloneWSRequest }
  case class ChannelRequest(channelId: String, request: StandaloneWSRequest) extends HttpApiRequest

  private case class RateLimit(remaining: Int, reset: Int)
  sealed case class RateLimited()

  case class RateLimitedException(message: String) extends Throwable

  protected val remainingHeader = "X-RateLimit-Remaining"
  protected val resetHeader     = "X-RateLimit-Reset"

  val baseUrl = "https://discordapp.com/api/v6"

  def requestHeaders(token: String): Seq[(String, String)] = {
    Seq(
      "Authorization" -> s"Bot $token",
      "User-Agent" -> "DiscordBot (https://github.com/ryanmiville/akkord, 0.1)"
    )
  }
}
