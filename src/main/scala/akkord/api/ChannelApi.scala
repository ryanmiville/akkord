package akkord.api

import akka.actor.Props
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akkord.api.DiscordApi.ChannelRequest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Encoder

class ChannelApi(token: String)(implicit mat: ActorMaterializer) extends DiscordApi(token) with FailFastCirceSupport {
  import ChannelApi._

  override def pipeHttpApiRequest: Receive = {
    case msg @ SendMessage(_, content)    => tellChannelRequestBundle(msg, MessagePayload(content))
    case bundle: ChannelRequestBundle => pipeChannelRequest(bundle)
  }

  private def pipeChannelRequest(bundle: ChannelRequestBundle): Unit = {
    Marshal(bundle.payload)
      .to[MessageEntity]
      .map { reqEntity =>
        val req = HttpRequest(bundle.method, bundle.uri, reqHeaders, reqEntity)
        ChannelRequest(bundle.channelId, req)
      }
      .pipeTo(self)
  }

  private def tellChannelRequestBundle(msg: SendMessage, payload: ChannelPayload) = {
    val (method, uri) = getEndpoint(msg)
    self ! ChannelRequestBundle(msg.channelId, method, uri, payload)
  }
}

object ChannelApi {
  import DiscordApi._

  trait ChannelRequest
  case class SendMessage(channelId: String, content: String) extends ChannelRequest

  sealed trait ChannelPayload
  case class MessagePayload(content: String) extends ChannelPayload

  private case class ChannelRequestBundle(channelId: String, method: HttpMethod, uri: String, payload: ChannelPayload)

  def props(token:String)(implicit mat: ActorMaterializer): Props =
    Props(classOf[ChannelApi], token, mat)

  implicit val encodeChannelEntity: Encoder[ChannelPayload] =
    (channelEntity: ChannelPayload) => {
      import io.circe.generic.auto._
      import io.circe.syntax._
      channelEntity match {
        case entity: ChannelApi.MessagePayload => entity.asJson
      }
  }

  private def getEndpoint(req: ChannelRequest) = {
    req match {
      case SendMessage(id, _) => (HttpMethods.POST, s"$baseUrl/channels/$id/messages")
    }
  }
  private def createMessageEndpoint(channelId: String): (HttpMethod, String) =
    (HttpMethods.POST, s"$baseUrl/channels/$channelId/messages")

}