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
    case msg: SendMessage              => tellChannelRequestBundle(msg, Some(msg.payload))
    case del: DeleteMessage            => tellChannelRequestBundle(del, None)
    case mtc: ModifyTextChannel        => tellChannelRequestBundle(mtc, Some(mtc.payload))
    case mvc: ModifyVoiceChannel       => tellChannelRequestBundle(mvc, Some(mvc.payload))
    case bundle: ChannelRequestBundle  => pipeChannelRequest(bundle)
  }

  private def pipeChannelRequest(bundle: ChannelRequestBundle): Unit = {
    println(bundle)
    Marshal(bundle.payload)
      .to[MessageEntity]
      .map { reqEntity =>
        val req = HttpRequest(bundle.method, bundle.uri, reqHeaders, reqEntity)
        ChannelRequest(bundle.channelId, req)
      }
      .pipeTo(self)
  }

  private def tellChannelRequestBundle(req: ChannelReq, payload: Option[ChannelPayload]): Unit = {
    val (method, uri) = getEndpoint(req)
    self ! ChannelRequestBundle(req.channelId, method, uri, payload)
  }
}

object ChannelApi {
  import DiscordApi._

  sealed trait ChannelReq {
    val channelId: String
  }

  case class SendMessage(channelId: String, payload: MessagePayload) extends ChannelReq {
    def this(channelId: String, content: String) = this(channelId, MessagePayload(content))
  }
  case class DeleteMessage(channelId: String, messageId: String) extends ChannelReq
  case class ModifyTextChannel(channelId: String, payload: ModifyTextChannelPayload) extends ChannelReq
  case class ModifyVoiceChannel(channelId: String, payload: ModifyVoiceChannelPayload) extends ChannelReq

  sealed trait ChannelPayload
  case class MessagePayload(content: String) extends ChannelPayload
  case class ModifyTextChannelPayload(name: Option[String] = None, position: Option[Int] = None, topic: Option[String] = None) extends ChannelPayload
  case class ModifyVoiceChannelPayload(name: Option[String] = None, position: Option[Int] = None, bitrate: Option[Int] = None, user_limit: Option[Int] = None) extends ChannelPayload

  private case class ChannelRequestBundle(channelId: String, method: HttpMethod, uri: String, payload: Option[ChannelPayload])

  def props(token:String)(implicit mat: ActorMaterializer): Props =
    Props(classOf[ChannelApi], token, mat)

  implicit val encodeChannelEntity: Encoder[ChannelPayload] =
    (channelEntity: ChannelPayload) => {
      import io.circe.generic.auto._
      import io.circe.syntax._
      channelEntity match {
        case entity: MessagePayload            => entity.asJson
        case entity: ModifyTextChannelPayload  => entity.asJson
        case entity: ModifyVoiceChannelPayload => entity.asJson
      }
  }

  private def getEndpoint(req: ChannelReq): (HttpMethod, String) = {
    req match {
      case SendMessage(id, _)        => (HttpMethods.POST, s"$baseUrl/channels/$id/messages")
      case DeleteMessage(cId, mId)   => (HttpMethods.DELETE, s"$baseUrl/channels/$cId/messages/$mId")
      case ModifyTextChannel(id, _)  => (HttpMethods.PATCH, s"$baseUrl/channels/$id")
      case ModifyVoiceChannel(id, _) => (HttpMethods.PATCH, s"$baseUrl/channels/$id")
    }
  }
}