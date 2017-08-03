import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

class Messenger(token: String) extends Actor with FailFastCirceSupport {

  import Messenger._
  import io.circe.generic.auto._

  implicit protected val executionContext = context.system.dispatcher
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  protected val http = Http(context.system)
  protected val authorization = RawHeader("Authorization", s"Bot $token")
  protected val userAgent = headers.`User-Agent`("DiscordBot (https://github.com/ryanmiville/akkord, 1.0)")
  protected val reqHeaders = List(authorization, userAgent)

  override def receive = {
    case Message(channelId, content) =>
      println(s"Messenger received: $channelId, $content")
      Marshal(MessageEntity(content))
        .to[RequestEntity]
        .flatMap { requestEntity =>
          val request = HttpRequest(HttpMethods.POST, getUri(channelId), headers = reqHeaders, entity = requestEntity)
          http.singleRequest(request)
        }
        .map(resp => println(resp.status))
  }
}

object Messenger {
  val baseUrl = "https://discordapp.com/api/v6"
  case class Message(channelId: String, content: String)
  private case class MessageEntity(content: String)

  private def getUri(channelId: String): String = s"$baseUrl/channels/$channelId/messages"

}
