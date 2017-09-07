package akkord.api

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{RawHeader, `User-Agent`}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.collection.{immutable, mutable}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class DiscordApiActor(token: String)(implicit mat: ActorMaterializer) extends Actor with ActorLogging {
  import DiscordApiActor._

  implicit protected val ec = context.system.dispatcher
  implicit val system       = context.system

  protected val reqHeaders = requestHeaders(token)
  private val rateLimits = mutable.Map[String, RateLimit]()

  override def receive: Receive =
    pipeHttpApiRequest orElse
    sendRequestWithRateLimiting

  def sendRequestWithRateLimiting: Receive = {
    case req: HttpApiRequest => sender ! sendRequest(req)
  }

  private def sendRequest(req: HttpApiRequest): Either[RateLimited, HttpEntity] = {
    getMajorEndpoint(req).map { ep =>
      val resp = Await.result(Http().singleRequest(req.request), Duration.Inf)
      log.info(s"response code: ${resp.status}")
      updateRateLimits(ep, resp)
    }
  }

  private def updateRateLimits(endpoint: String, resp: HttpResponse): HttpEntity = {
    val remaining = resp.headers.find(_.name() == remainingHeader).map(_.value().toInt)
    val reset     = resp.headers.find(_.name() == resetHeader).map(_.value().toInt)

    val rateLimit =
      for {
        rem <- remaining
        res <- reset
      } yield RateLimit(rem, res)

    rateLimit.foreach(r => rateLimits(endpoint) = r)
    resp.entity
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

  def pipeHttpApiRequest: Receive
}

object DiscordApiActor {
  trait HttpApiRequest { val request: HttpRequest }
  case class ChannelRequest(channelId: String, request: HttpRequest) extends HttpApiRequest
  case class RateLimitedException(message: String) extends Exception(message)
  private case class RateLimit(remaining: Int, reset: Int)
  case class RateLimited()

  val remainingHeader = "X-RateLimit-Remaining"
  val resetHeader     = "X-RateLimit-Reset"

  val baseUrl = "https://discordapp.com/api/v6"

  def requestHeaders(token: String): immutable.Seq[HttpHeader] = {
    val authorization = RawHeader("Authorization", s"Bot $token")
    val userAgent     = `User-Agent`("DiscordBot (https://github.com/ryanmiville/akkord, 0.1)")
    collection.immutable.Seq(authorization, userAgent)
  }
}
