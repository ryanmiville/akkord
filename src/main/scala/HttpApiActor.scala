import HttpApiActor._
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.pattern.pipe
import akka.stream.ActorMaterializer

import scala.collection.{immutable, mutable}

abstract class HttpApiActor(token: String)(implicit mat: ActorMaterializer) extends Actor {
  implicit protected val ec = context.system.dispatcher
  implicit val system = context.system
  protected val reqHeaders = requestHeaders(token)

  private var rateLimits = mutable.Map[String, RateLimit]()

  override def receive =
    callApi orElse
    checkRateLimit

  def checkRateLimit: Receive = {
    case Response(endpoint, resp) =>
      resp.discardEntityBytes()
      val remaining = resp.headers.find(_.name() == remainingHeader).map(_.value().toInt)
      val reset = resp.headers.find(_.name() == resetHeader).map(_.value().toInt)
      val rateLimit =
        for {
          rem <- remaining
          res <- reset
        } yield RateLimit(rem, res)

      rateLimit.foreach(r => rateLimits(endpoint) = r)
    case req: HttpApiRequest =>
      println("received HttpApiRequest")
      getMajorEndpoint(req).map { ep =>
        println(s"got endpoint: $ep")
        println(s"uri: ${req.request.uri}")
        Http().singleRequest(req.request)
          .map(resp => Response(ep, resp))
          .pipeTo(self)
      }
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
    val rateLimit = rateLimits.getOrElse(majorEndpoint, RateLimit(Int.MaxValue, 0))
    val currentTime = System.currentTimeMillis / 1000
    rateLimit.remaining < 1 && currentTime < rateLimit.reset
  }

  def callApi: Receive
}

object HttpApiActor {
  case class Response(majorEndpoint: String, response: HttpResponse)
  case class ChannelRequest(channelId: String, request: HttpRequest) extends HttpApiRequest
  case class GuildRequest(guildId: String, request: HttpRequest)

  trait HttpApiRequest { val request: HttpRequest }
  private case class RateLimit(remaining: Int, reset: Int)

  private case object RateLimited
  val remainingHeader = "X-RateLimit-Remaining"
  val resetHeader = "X-RateLimit-Reset"
  val baseUrl = "https://discordapp.com/api/v6"

  def requestHeaders(token: String): immutable.Seq[HttpHeader] = {
    val authorization = RawHeader("Authorization", s"Bot $token")
    val userAgent = RawHeader("User-Agent","DiscordBot (https://github.com/ryanmiville/akkord, 1.0)")
    collection.immutable.Seq(authorization, userAgent)
  }
}
