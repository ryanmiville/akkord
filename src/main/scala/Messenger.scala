import HttpApiActor.ChannelRequest
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.Future

class Messenger(token: String)(implicit mat: ActorMaterializer) extends HttpApiActor(token) with FailFastCirceSupport {

  import Messenger._
  import io.circe.generic.auto._

//  override def receive = {
//    case Message(channelId, content) =>
//      println(s"Messenger received: $channelId, $content")
//      val channelRequest = makeRequest(channelId, content)
//      channelRequest pipeTo self
//    case resp: HttpResponse =>
//      println(resp.status)
//      resp.discardEntityBytes()
//  }

  private def makeRequest(channelId: String, content: String): Future[ChannelRequest] = {
    Marshal(MessageEntity(content))
      .to[model.MessageEntity]
      .map { requestEntity =>
        val request = HttpRequest(HttpMethods.POST, getUri(channelId), headers = reqHeaders, entity = requestEntity)
        println("made request")
        ChannelRequest(channelId, request)
      }
  }

  override def callApi = {
    case Message(channelId, content) =>
      println(s"Messenger received: $channelId, $content")
      val response = makeRequest(channelId, content)
      response pipeTo self
  }
}

object Messenger {
  val baseUrl = "https://discordapp.com/api/v6"
  case class Message(channelId: String, content: String)
  private case class MessageEntity(content: String)

  private def getUri(channelId: String): String = s"$baseUrl/channels/$channelId/messages"

}
