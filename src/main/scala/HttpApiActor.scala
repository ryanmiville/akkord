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
  protected val baseUrl = "https://discordapp.com/api/v6"

  private var rateLimits = mutable.Map[String, RateLimit]()

  override def receive =
    callApi orElse
    checkRateLimit

  def checkRateLimit: Receive = {
    case Response(endpoint, resp) =>
      val headers =
        resp.headers
        .map(h => h.name() -> h.value())
        .toMap
      println(s"headers: $headers")
      val rateLimit =
        for {
          remaining <- headers.get(remainingHeader)
          reset <- headers.get(resetHeader)
        } yield RateLimit(remaining.toInt, reset.toInt)
      rateLimit.foreach(r => rateLimits(endpoint) = r)
    case ChannelRequest(id, req) =>
      val rateLimit = rateLimits.getOrElse(s"channel/$id", RateLimit(Int.MaxValue, 0))
      val currentTime = System.currentTimeMillis / 1000
      if(rateLimit.remaining < 1 && currentTime < rateLimit.reset) {

      } else {
        Http().singleRequest(req)
          .map(resp => Response(s"channel/$id", resp))
          .pipeTo(self)
      }
  }

  def callApi: Receive
}

object HttpApiActor {
  case class Response(endpoint: String, response: HttpResponse)
  case class ChannelRequest(channelId: String, request: HttpRequest)
  case class GuildRequest(guildId: String, request: HttpRequest)

  private case class RateLimit(remaining: Int, reset: Int)

  val remainingHeader = "X-RateLimit-Remaining"
  val resetHeader = "X-RateLimit-Reset"

  def requestHeaders(token: String): immutable.Seq[HttpHeader] = {
    val authorization = RawHeader("Authorization", s"Bot $token")
    val userAgent = RawHeader("User-Agent","DiscordBot (https://github.com/ryanmiville/akkord, 1.0)")
    collection.immutable.Seq(authorization, userAgent)
  }
}
