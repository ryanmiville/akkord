

import DiscordClient._
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy}
import io.circe.Json
import io.circe.parser._
import io.circe.optics.JsonPath._
import scala.concurrent.duration._

object DiscordClient {
  private trait DiscordMessage
  private case object Init
  private case object Ack
  private case object Complete
  private case class Hello(json: Json) extends DiscordMessage
  private case class ToDo(text: String) extends DiscordMessage
  private case class UnsupportedMessage(text: String) extends DiscordMessage
  private case object HeartBeat
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

  var heartbeatInterval = Int.MaxValue
  var lastSeq: Option[Int] = None
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
    val s = json.hcursor.get[Int]("s").toOption
    s.foreach(newSeq => lastSeq = Some(newSeq))
    op.getOrElse(-1) match {
      case 0 => ToDo(text)
      case 3 => ToDo(text)
      case 7 => ToDo(text)
      case 9 => ToDo(text)
      case 10 => Hello(json)
      case 11 => ToDo(text)
      case _ => UnsupportedMessage(text)
    }
  }

  private def heartbeatMessage = {
    s"""
      |{
      |    "op": 1,
      |    "d": ${lastSeq.orNull}
      |}
    """.stripMargin
  }
  override def receive = {
    case Init =>
      sender ! Ack
    case Hello(json) =>
      println(s"received Hello message: $json")
      println("Sending Identify.")
      queue.offer(TextMessage(identify))
      val heartbeatPath = root.d.heartbeat_interval.int
      heartbeatInterval = heartbeatPath.getOption(json).get
      system.scheduler.scheduleOnce(heartbeatInterval millis, self, HeartBeat)
      sender ! Ack
    case HeartBeat =>
      println("sending heartbeat")
      queue.offer(TextMessage(heartbeatMessage))
      system.scheduler.scheduleOnce(heartbeatInterval millis, self, HeartBeat)
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
