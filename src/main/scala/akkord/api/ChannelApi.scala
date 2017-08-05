package akkord.api

import akka.actor.Props
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akkord.api.HttpApiActor.ChannelRequest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Encoder

class ChannelApi(token: String)(implicit mat: ActorMaterializer) extends HttpApiActor(token) with FailFastCirceSupport {
  import ChannelApi._

  override def pipeHttpApiRequest: Receive = {
    case Message(channelId, content)  => tellChannelRequestBundle(channelId, content)
    case bundle: ChannelRequestBundle => pipeChannelRequest(bundle)
  }

  private def pipeChannelRequest(bundle: ChannelRequestBundle): Unit = {
    Marshal(bundle.channelEntity)
      .to[model.MessageEntity]
      .map { reqEntity =>
        val req = HttpRequest(bundle.method, bundle.uri, reqHeaders, reqEntity)
        ChannelRequest(bundle.channelId, req)
      }
      .pipeTo(self)
  }

  private def tellChannelRequestBundle(channelId: String, content: String): Unit = {
    val (method, uri) = createMessageEndpoint(channelId)
    val channelEntity = MessageEntity(content)
    self ! ChannelRequestBundle(channelId, method, uri, channelEntity)
  }
}

object ChannelApi {
  import HttpApiActor._

  case class Message(channelId: String, content: String)

  sealed trait ChannelEntity
  case class MessageEntity(content: String) extends ChannelEntity

  private case class ChannelRequestBundle(channelId: String, method: HttpMethod, uri: String, channelEntity: ChannelEntity)

  def props(token:String)(implicit mat: ActorMaterializer): Props =
    Props(classOf[ChannelApi], token, mat)

  implicit val encodeChannelEntity: Encoder[ChannelEntity] =
    (channelEntity: ChannelEntity) => {
      import io.circe.generic.auto._
      import io.circe.syntax._
      channelEntity match {
        case entity: ChannelApi.MessageEntity => entity.asJson
      }
  }

  private def createMessageEndpoint(channelId: String): (HttpMethod, String) =
    (HttpMethods.POST, s"$baseUrl/channels/$channelId/messages")

}