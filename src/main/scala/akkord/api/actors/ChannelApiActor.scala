package akkord.api.actors

import akka.actor.Props
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akkord.api.actors.DiscordApiActor.ChannelRequest
import akkord.api.circesupport.CirceBodyWritable
import io.circe.Encoder
import io.circe.syntax._

class ChannelApiActor(token: String)(implicit mat: Materializer)
  extends DiscordApiActor(token)
  with CirceBodyWritable {
  import ChannelApiActor._

  override def tellHttpApiRequest: Receive = {
    case c: GetChannel                   => tellChannelRequestBundle(c, None)
    case gcm: GetChannelMessages         => tellChannelRequestBundle(gcm, None)
    case gcm: GetChannelMessage          => tellChannelRequestBundle(gcm, None)
    case msg: CreateMessage              => tellChannelRequestBundle(msg, Some(msg.payload))
    case msg: EditMessage                => tellChannelRequestBundle(msg, Some(msg.payload))
    case del: DeleteMessage              => tellChannelRequestBundle(del, None)
    case del: BulkDeleteMessages         => tellChannelRequestBundle(del, Some(del.payload))
    case mtc: ModifyTextChannel          => tellChannelRequestBundle(mtc, Some(mtc.payload))
    case mvc: ModifyVoiceChannel         => tellChannelRequestBundle(mvc, Some(mvc.payload))
    case del: DeleteChannel              => tellChannelRequestBundle(del, None)
    case cr: CreateReaction              => tellChannelRequestBundle(cr, None)
    case del: DeleteAllReactions         => tellChannelRequestBundle(del, None)
    case pin: GetPinnedMessages          => tellChannelRequestBundle(pin, None)
    case pin: AddPinnedChannelMessage    => tellChannelRequestBundle(pin, None)
    case pin: DeletePinnedChannelMessage => tellChannelRequestBundle(pin, None)
    case bundle: ChannelRequestBundle    => forwardChannelRequest(bundle)
  }

  private def forwardChannelRequest(bundle: ChannelRequestBundle): Unit = {
    val req =
      wsClient
        .url(bundle.uri)
        .withMethod(bundle.method)
        .withBody(bundle.payload.asJson)
        .withHttpHeaders(reqHeaders: _*)
    self forward ChannelRequest(bundle.channelId, req)
  }

  private def tellChannelRequestBundle(req: ChannelReq, payload: Option[ChannelPayload]): Unit = {
    val (method, uri) = getEndpoint(req)
    self forward ChannelRequestBundle(req.channelId, method, Uri.normalize(uri), payload)
  }
}

object ChannelApiActor {
  import DiscordApiActor._

  sealed trait ChannelReq {
    val channelId: String
  }

  case class GetChannel(channelId: String) extends ChannelReq
  case class GetChannelMessages(channelId: String) extends ChannelReq
  case class GetChannelMessage(channelId: String, messageId: String) extends ChannelReq
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
  case class GetPinnedMessages(channelId: String) extends ChannelReq
  case class AddPinnedChannelMessage(channelId: String, messageId: String) extends ChannelReq
  case class DeletePinnedChannelMessage(channelId: String, messageId: String) extends ChannelReq

  sealed trait ChannelPayload
  case class MessagePayload(content: String) extends ChannelPayload
  case class ModifyTextChannelPayload(name: Option[String] = None, position: Option[Int] = None, topic: Option[String] = None) extends ChannelPayload
  case class ModifyVoiceChannelPayload(name: Option[String] = None, position: Option[Int] = None, bitrate: Option[Int] = None, user_limit: Option[Int] = None) extends ChannelPayload
  case class BulkDeleteMessagesPayload(messages: List[String])extends ChannelPayload

  private case class ChannelRequestBundle(channelId: String, method: String, uri: String, payload: Option[ChannelPayload])

  def props(token:String)(implicit mat: Materializer): Props =
    Props(classOf[ChannelApiActor], token, mat)

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

  private def getEndpoint(req: ChannelReq): (String, String) = req match {
    case GetChannel(id)                   => ("GET", s"$baseUrl/channels/$id")
    case GetChannelMessages(id)           => ("GET", s"$baseUrl/channels/$id/messages")
    case GetChannelMessage(c, m)          => ("GET", s"$baseUrl/channels/$c/messages/$m")
    case CreateMessage(id, _)             => ("POST", s"$baseUrl/channels/$id/messages")
    case DeleteMessage(c, m)              => ("DELETE", s"$baseUrl/channels/$c/messages/$m")
    case EditMessage(c, m, _)             => ("PATCH", s"$baseUrl/channels/$c/messages/$m")
    case BulkDeleteMessages(id, _)        => ("POST", s"$baseUrl/channels/$id/messages/bulk-delete")
    case ModifyTextChannel(id, _)         => ("PATCH", s"$baseUrl/channels/$id")
    case ModifyVoiceChannel(id, _)        => ("PATCH", s"$baseUrl/channels/$id")
    case DeleteChannel(id)                => ("DELETE", s"$baseUrl/channels/$id")
    case CreateReaction(c, m, e)          => ("PUT", s"$baseUrl/channels/$c/messages/$m/reactions/$e/@me")
    case DeleteAllReactions(c, m)         => ("DELETE", s"$baseUrl/channels/$c/messages/$m/reactions")
    case GetPinnedMessages(id)            => ("GET", s"$baseUrl/channels/$id/pins")
    case AddPinnedChannelMessage(c, m)    => ("PUT", s"$baseUrl/channels/$c/pins/$m")
    case DeletePinnedChannelMessage(c, m) => ("DELETE", s"$baseUrl/channels/$c/pins/$m")
  }
}