import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

class Messenger(token: String)(implicit mat: ActorMaterializer) extends Actor with FailFastCirceSupport {

  import Messenger._
  import io.circe.generic.auto._

  implicit protected val executionContext = context.system.dispatcher
  implicit val system = context.system
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
          println("made request")
          Http().singleRequest(request)
        }
        .map { resp =>
          println(resp.status)
          resp.discardEntityBytes()
        }
  }
}

object Messenger {
  val baseUrl = "https://discordapp.com/api/v6"
  case class Message(channelId: String, content: String)
  private case class MessageEntity(content: String)

  private def getUri(channelId: String): String = s"$baseUrl/channels/$channelId/messages"

}
