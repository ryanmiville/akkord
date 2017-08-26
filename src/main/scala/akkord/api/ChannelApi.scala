package akkord.api

import akka.actor.Props
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akkord.api.DiscordApi.ChannelRequest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Encoder

import scala.util.Try

class ChannelApi(token: String)(implicit mat: ActorMaterializer) extends DiscordApi(token) with FailFastCirceSupport {
  import ChannelApi._

  override def pipeHttpApiRequest: Receive = {
    case msg: CreateMessage              => tellChannelRequestBundle(msg, Some(msg.payload))
    case msg: EditMessage                => tellChannelRequestBundle(msg, Some(msg.payload))
    case del: DeleteMessage              => tellChannelRequestBundle(del, None)
    case del: BulkDeleteMessages         => tellChannelRequestBundle(del, Some(del.payload))
    case mtc: ModifyTextChannel          => tellChannelRequestBundle(mtc, Some(mtc.payload))
    case mvc: ModifyVoiceChannel         => tellChannelRequestBundle(mvc, Some(mvc.payload))
    case del: DeleteChannel              => tellChannelRequestBundle(del, None)
    case cr: CreateReaction              => tellChannelRequestBundle(cr, None)
    case del: DeleteAllReactions         => tellChannelRequestBundle(del, None)
    case pin: AddPinnedChannelMessage    => tellChannelRequestBundle(pin, None)
    case pin: DeletePinnedChannelMessage => tellChannelRequestBundle(pin, None)
    case bundle: ChannelRequestBundle    => pipeChannelRequest(bundle)
  }

  private def pipeChannelRequest(bundle: ChannelRequestBundle): Unit = {
    println(s"bundle: $bundle")
    Marshal(bundle.payload)
      .to[MessageEntity]
      .map { reqEntity =>
        println("making httpRequest")
        val req = Try(HttpRequest(bundle.method, bundle.uri, reqHeaders, reqEntity))
        println(s"pipe req: $req")
        ChannelRequest(bundle.channelId, req.get)
      }
      .pipeTo(self)(sender)
  }

  private def tellChannelRequestBundle(req: ChannelReq, payload: Option[ChannelPayload]): Unit = {
    val (method, uri) = getEndpoint(req)
    println(s"req: $req")
    println(s"uri: $uri")

    self ! ChannelRequestBundle(req.channelId, method, Uri.normalize(uri), payload)
  }
}

object ChannelApi {
  import DiscordApi._

  sealed trait ChannelReq {
    val channelId: String
  }

  case class CreateMessage(channelId: String, payload: MessagePayload) extends ChannelReq {
    def this(channelId: String, content: String) = this(channelId, MessagePayload(content))
  }
  case class DeleteMessage(channelId: String, messageId: String) extends ChannelReq
  case class DeleteChannel(channelId: String) extends ChannelReq
  case class ModifyTextChannel(channelId: String, payload: ModifyTextChannelPayload) extends ChannelReq
  case class ModifyVoiceChannel(channelId: String, payload: ModifyVoiceChannelPayload) extends ChannelReq
  case class CreateReaction(channelId: String, messageId: String, emoji: String) extends ChannelReq
  case class DeleteAllReactions(channelId: String, messageId: String) extends ChannelReq
  case class EditMessage(channelId: String, messageId: String, payload: MessagePayload) extends ChannelReq {
    def this(channelId: String, messageId: String, content: String) = this(channelId, messageId, MessagePayload(content))
  }
  case class BulkDeleteMessages(channelId: String, payload: BulkDeleteMessagesPayload) extends ChannelReq {
    def this(channelId: String, messages: List[String]) = this(channelId, BulkDeleteMessagesPayload(messages))
  }
  case class AddPinnedChannelMessage(channelId: String, messageId: String) extends ChannelReq
  case class DeletePinnedChannelMessage(channelId: String, messageId: String) extends ChannelReq

  sealed trait ChannelPayload
  case class MessagePayload(content: String) extends ChannelPayload
  case class ModifyTextChannelPayload(name: Option[String] = None, position: Option[Int] = None, topic: Option[String] = None) extends ChannelPayload
  case class ModifyVoiceChannelPayload(name: Option[String] = None, position: Option[Int] = None, bitrate: Option[Int] = None, user_limit: Option[Int] = None) extends ChannelPayload
  case class BulkDeleteMessagesPayload(messages: List[String])extends ChannelPayload

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
        case entity: BulkDeleteMessagesPayload => entity.asJson
      }
  }

  private def getEndpoint(req: ChannelReq): (HttpMethod, String) = {
    req match {
      case CreateMessage(id, _)      => (HttpMethods.POST, s"$baseUrl/channels/$id/messages")
      case DeleteMessage(c, m)       => (HttpMethods.DELETE, s"$baseUrl/channels/$c/messages/$m")
      case EditMessage(c, m, _)      => (HttpMethods.PATCH, s"$baseUrl/channels/$c/messages/$m")
      case BulkDeleteMessages(id, _) => (HttpMethods.POST, s"$baseUrl/channels/$id/messages/bulk-delete")
      case ModifyTextChannel(id, _)  => (HttpMethods.PATCH, s"$baseUrl/channels/$id")
      case ModifyVoiceChannel(id, _) => (HttpMethods.PATCH, s"$baseUrl/channels/$id")
      case DeleteChannel(id)         => (HttpMethods.DELETE, s"$baseUrl/channels/$id")
      case CreateReaction(c, m, e)   => (HttpMethods.PUT, s"$baseUrl/channels/$c/messages/$m/reactions/$e/@me")
      case DeleteAllReactions(c, m)  => (HttpMethods.DELETE, s"$baseUrl/channels/$c/messages/$m/reactions")
      case AddPinnedChannelMessage(c, m) => (HttpMethods.PUT, s"$baseUrl/channels/$c/pins/$m")
      case DeletePinnedChannelMessage(c, m) => (HttpMethods.DELETE, s"$baseUrl/channels/$c/pins/$m")
    }
  }
}