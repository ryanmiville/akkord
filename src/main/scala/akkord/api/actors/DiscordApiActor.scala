package akkord.api.actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.stream.Materializer
import akkord.api.circesupport.CirceBodyReadable
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.{StandaloneWSRequest, StandaloneWSResponse}

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.language.postfixOps
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

  private val rateLimits      = mutable.Map[String, RateLimit]()
  private var globalRateLimit = false

  override def receive: Receive =
    tellHttpApiRequest orElse {
      case req: HttpApiRequest  => sender ! sendRequest(req)
      case ResetGlobalRateLimit => globalRateLimit = false
    }

  private def sendRequest(req: HttpApiRequest): Either[RateLimited, StandaloneWSResponse] = {
    getMajorEndpoint(req).map { ep =>
      val resp = Await.result(req.request.execute(), Duration.Inf)
      log.info(req.toString)
      log.info(s"response code: ${resp.status}\nbody: ${resp.body}")
      updateRateLimits(ep, resp)
    }
  }

  private def updateRateLimits(endpoint: String, resp: StandaloneWSResponse): StandaloneWSResponse = {
    def updateEndpointRateLimit() = {
      val remaining = resp.header(remainingHeader).map(_.toInt)
      val reset     = resp.header(resetHeader).map(_.toInt)

      for {
        rem <- remaining
        res <- reset
      } yield {
        val rateLimit = RateLimit(rem, res)
        rateLimits(endpoint) = rateLimit
      }
    }

    def updateGlobalRateLimit() = {
      val global = resp.header(globalHeader).map(_.toBoolean)
      val retry  = resp.header(retryHeader).map(_.toInt)

      for {
        g <- global
        r <- retry
      } yield {
        globalRateLimit = g
        system.scheduler.scheduleOnce(r millis, self, ResetGlobalRateLimit)
      }
    }

    updateEndpointRateLimit()
    updateGlobalRateLimit()
    resp
  }

  private def getMajorEndpoint(request: HttpApiRequest): Either[RateLimited, String] = {
    val majorEndpoint = request match {
      case ChannelRequest(id, _) => s"channels/$id"
    }
    if (isRateLimited(majorEndpoint)) {
      log.info(s"$majorEndpoint is being rate limited")
      Left(RateLimited(majorEndpoint))
    } else {
      Right(majorEndpoint)
    }
  }

  def isRateLimited(majorEndpoint: String): Boolean = {
    val rateLimit   = rateLimits.getOrElse(majorEndpoint, RateLimit(Int.MaxValue, 0))
    val currentTime = System.currentTimeMillis / 1000

    globalRateLimit || (rateLimit.remaining < 1 && currentTime < rateLimit.reset)
  }

  def tellHttpApiRequest: Receive
}

object DiscordApiActor {
  trait HttpApiRequest { val request: StandaloneWSRequest }
  case class ChannelRequest(channelId: String, request: StandaloneWSRequest) extends HttpApiRequest

  private case class RateLimit(remaining: Int, reset: Int)
  sealed case class RateLimited(majorEndpoint: String)

  case class RateLimitedException(message: String) extends Throwable

  case object ResetGlobalRateLimit

  protected val remainingHeader = "X-RateLimit-Remaining"
  protected val resetHeader     = "X-RateLimit-Reset"
  protected val globalHeader    = "X-RateLimit-Global"
  protected val retryHeader     = "Retry-After"

  val baseUrl = "https://discordapp.com/api/v6"

  def requestHeaders(token: String): Seq[(String, String)] = {
    Seq(
      "Authorization" -> s"Bot $token",
      "Content-Type"  -> "application/json",
      "User-Agent"    -> "DiscordBot (https://github.com/ryanmiville/akkord, 0.1)"
    )
  }
}
