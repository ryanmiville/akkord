package akkord.api

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import akkord.api.actors.DiscordApiActor.{RateLimited, RateLimitedException}
import akkord.api.circesupport.CirceBodyReadable
import play.api.libs.ws.StandaloneWSResponse

import scala.concurrent.{ExecutionContext, Future}

abstract class DiscordApi(implicit ec: ExecutionContext, timeout: Timeout) extends CirceBodyReadable {
  protected val api: ActorRef
  protected def getApiResponse(apiRequest: Any): Future[StandaloneWSResponse] = {
    val response = (api ? apiRequest).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(response)
  }

  private def unwrapResponse(response: Future[Either[RateLimited, StandaloneWSResponse]]): Future[StandaloneWSResponse] = {
    response map {
      case Right(resp) =>
        resp
      case Left(rateLimited) =>
        throw RateLimitedException(s"major endpoint: ${rateLimited.majorEndpoint} is being rate limited")
    }
  }
}

object DiscordApi {
  case class EmptyResponse()
}
