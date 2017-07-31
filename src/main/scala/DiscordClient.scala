
import DiscordClient._
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy}
import io.circe.Json
import io.circe.parser._

object DiscordClient {
  private trait DiscordMessage
  private case object Init
  private case object Ack
  private case object Complete
  private case class Hello(text: String) extends DiscordMessage
  private case class ToDo(text: String) extends DiscordMessage
  private case class UnsupportedMessage(text: String) extends DiscordMessage
  case object Disconnect

  val identify = """{
                   |    "op": 2,
                   |    "d": {
                   |        "token": "MzAxNDMwMzcxNTI0OTM1Njky.DF_bEw.KzXWwb7fCunVfheg6SVtWdKg6ME",
                   |        "properties": {
                   |            "$os": "macos",
                   |            "$browser": "akkord",
                   |            "$device": "akkord"
                   |        },
                   |        "compress": false,
                   |        "large_threshold": 50,
                   |        "shard": [
                   |            0,
                   |            1
                   |        ],
                   |        "presence": {
                   |            "game": {
                   |                "name": "Cards Against Humanity"
                   |            },
                   |            "status": "online",
                   |            "since": 91879201,
                   |            "afk": false
                   |        }
                   |    }
                   |}""".stripMargin
}

class DiscordClient(implicit materializer: ActorMaterializer) extends Actor {
  private implicit val executionContext = context.system.dispatcher
  private implicit val system = context.system

  val (queue, killswitch) =
    Source.queue[Message](Int.MaxValue, OverflowStrategy.dropBuffer)
      .via(Http().webSocketClientFlow(WebSocketRequest("wss://gateway.discord.gg?v=6&encoding=json")))
      .viaMat(KillSwitches.single)(Keep.both)
      .map {
        case TextMessage.Strict(text) =>
          toDiscordMessage(text)
        case _ =>
          UnsupportedMessage
      }
      .to(Sink.actorRefWithAck(self, Init, Ack, Complete))
      .run()

  private def toDiscordMessage(text: String): DiscordMessage = {
    val json = parse(text).getOrElse(Json.Null)
    val op = json.hcursor.get[Int]("op").toOption
    op.getOrElse(-1) match {
      case 0 => ToDo(text)
      case 3 => ToDo(text)
      case 7 => ToDo(text)
      case 9 => ToDo(text)
      case 10 => Hello(text)
      case 11 => ToDo(text)
      case _ => UnsupportedMessage(text)
    }
  }

  override def receive = {
    case Init =>
      sender ! Ack
    case Hello(text) =>
      println(s"received Hello message: $text")
      println("Sending Identify.")
      queue.offer(TextMessage(identify))
      sender ! Ack
    case ToDo(text) =>
      println(s"received message: $text")
      sender ! Ack
    case UnsupportedMessage(text) =>
      println(s"received unsupported message: $text")
      sender ! Ack
    case Disconnect =>
      println("Stopping")
      killswitch.shutdown()
    case Complete =>
      println("The stream has terminated")
      context.stop(self)
  }
}
