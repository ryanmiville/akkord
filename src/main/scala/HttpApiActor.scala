import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{RawHeader, `User-Agent`}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.pattern.pipe
import akka.stream.ActorMaterializer

import scala.collection.{immutable, mutable}

abstract class HttpApiActor(token: String)(implicit mat: ActorMaterializer) extends Actor {
  import HttpApiActor._
  
  implicit protected val ec = context.system.dispatcher
  implicit val system       = context.system

  protected val reqHeaders = requestHeaders(token)
  private var rateLimits   = mutable.Map[String, RateLimit]()

  override def receive =
    pipeHttpApiRequest orElse
    sendRequestWithRateLimiting

  def sendRequestWithRateLimiting: Receive = {
    case Response(endpoint, resp) => updateRateLimits(endpoint, resp)
    case req: HttpApiRequest      => sendRequest(req)
  }

  private def sendRequest(req: HttpApiRequest) = {
    getMajorEndpoint(req).map { ep =>
      Http().singleRequest(req.request)
        .map(resp => Response(ep, resp))
        .pipeTo(self)
    }
  }

  private def updateRateLimits(endpoint: String, resp: HttpResponse) = {
    resp.discardEntityBytes()
    val remaining = resp.headers.find(_.name() == remainingHeader).map(_.value().toInt)
    val reset     = resp.headers.find(_.name() == resetHeader).map(_.value().toInt)

    val rateLimit =
      for {
        rem <- remaining
        res <- reset
      } yield RateLimit(rem, res)

    rateLimit.foreach(r => rateLimits(endpoint) = r)
  }

  private def getMajorEndpoint(request: HttpApiRequest) = {
    val majorEndpoint = request match {
      case ChannelRequest(id, req) => s"channel/$id"
    }
    if (isRateLimited(majorEndpoint))
      Left(RateLimited)
    else
      Right(majorEndpoint)
  }

  def isRateLimited(majorEndpoint: String) = {
    val rateLimit   = rateLimits.getOrElse(majorEndpoint, RateLimit(Int.MaxValue, 0))
    val currentTime = System.currentTimeMillis / 1000

    rateLimit.remaining < 1 && currentTime < rateLimit.reset
  }

  def pipeHttpApiRequest: Receive
}

object HttpApiActor {
  trait HttpApiRequest { val request: HttpRequest }
  case class ChannelRequest(channelId: String, request: HttpRequest) extends HttpApiRequest
  case class Response(majorEndpoint: String, response: HttpResponse)

  private case class RateLimit(remaining: Int, reset: Int)
  private case object RateLimited

  val remainingHeader = "X-RateLimit-Remaining"
  val resetHeader     = "X-RateLimit-Reset"

  val baseUrl = "https://discordapp.com/api/v6"

  def requestHeaders(token: String): immutable.Seq[HttpHeader] = {
    val authorization = RawHeader("Authorization", s"Bot $token")
    val userAgent     = `User-Agent`("DiscordBot (https://github.com/ryanmiville/akkord, 1.0)")
    collection.immutable.Seq(authorization, userAgent)
  }
}
