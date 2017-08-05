import HttpApiActor._
import akka.actor.Props
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Encoder

class ChannelApi(token: String)(implicit mat: ActorMaterializer) extends HttpApiActor(token) with FailFastCirceSupport {

  import ChannelApi._

  override def createHttpApiRequest = {
    case Message(channelId, content) =>
      println(s"Messenger received: $channelId, $content")
      val (method, uri) = createMessageEndpoint(channelId)
      val channelEntity = MessageEntity(content)
      self ! ChannelRequestBundle(channelId, method, uri, channelEntity)
    case ChannelRequestBundle(channelId, method, uri, channelEntity) =>
      println("channelRequestBundle received")
      Marshal(channelEntity)
        .to[model.MessageEntity]
        .map { reqEntity =>
          println(s"entity: ${reqEntity.toString}")
          val req = HttpRequest(method, uri, reqHeaders, reqEntity)
          ChannelRequest(channelId, req)
        }
        .pipeTo(self)
  }
}

object ChannelApi {
  case class Message(channelId: String, content: String)

  sealed trait ChannelEntity
  case class MessageEntity(content: String) extends ChannelEntity
  private case class ChannelRequestBundle
  (
    channelId: String,
    method: HttpMethod,
    uri: String,
    channelEntity: ChannelEntity
  )

  def props(token:String)(implicit mat: ActorMaterializer) = Props(classOf[ChannelApi], token, mat)

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