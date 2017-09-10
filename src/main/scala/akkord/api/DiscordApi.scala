package akkord.api

import akkord.api.actors.DiscordApiActor.{RateLimited, RateLimitedException}
import akkord.api.circesupport.CirceBodyReadable
import play.api.libs.ws.StandaloneWSResponse

import scala.concurrent.{ExecutionContext, Future}

abstract class DiscordApi(implicit ec: ExecutionContext) extends CirceBodyReadable {

  protected def unwrapResponse(
      channelId: String,
      response: Future[Either[RateLimited, StandaloneWSResponse]]): Future[StandaloneWSResponse] = response map {
    case Right(resp: StandaloneWSResponse) =>
      resp
    case Left(_) =>
      throw RateLimitedException(s"endpoint for channel ID: $channelId is being rate limited")
  }
}
