import DiscordClient._
import akka.actor.{Actor, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy}
import io.circe.Json
import io.circe.optics.JsonPath.root
import io.circe.parser.parse
import scala.concurrent.duration._

class DiscordClient(token: String) extends Actor {
  private implicit val executionContext = context.system.dispatcher
  private implicit val system = context.system
  private implicit val materializer = ActorMaterializer()

  var lastSeq: Option[Int] = None
  var sessionId = ""

  val (queue, killswitch) =
    Source.queue[Message](Int.MaxValue, OverflowStrategy.dropBuffer)
      .via(Http().webSocketClientFlow(WebSocketRequest("wss://gateway.discord.gg?v=6&encoding=json")))
      .viaMat(KillSwitches.single)(Keep.both)
      .collect {
        case TextMessage.Strict(text) =>
          mapToGatewayPayload(text, self)
      }
      .to(Sink.actorRefWithAck(self, Init, Ack, Complete))
      .run()

  override def receive = {
    case Init =>
      sender ! Ack
    case Hello(interval) =>
      println(s"received Hello message")
      println("Sending Identify.")
      queue.offer(identify(token))
      system.scheduler.schedule(interval millis, interval millis, self, HeartBeat)
      sender ! Ack
    case HeartBeat =>
      println("sending heartbeat")
      queue.offer(heartbeat(lastSeq))
    case HeartBeatAck =>
      println("received Heartbeat Ack")
      sender ! Ack
    case Event(json) =>
      println(s"received event message: $json")
      sender ! Ack
    case Ready(id) =>
      sessionId = id
      sender ! Ack
    case MessageCreate(content) =>
      if(content == "ping") println("pong")
      sender ! Ack
    case Reconnect =>
      println("received Reconnect message")
      println("sending Resume message")
      queue.offer(resume(token, sessionId, lastSeq))
      sender ! Ack
    case UnsupportedMessage(text) =>
      println(s"received unsupported message: $text")
      sender ! Ack
    case NewSeq(s) =>
      lastSeq = Some(s)
    case Disconnect =>
      println("Stopping")
      killswitch.shutdown()
    case Complete =>
      println("The stream has terminated")
      context.stop(self)
  }
}

object DiscordClient {
  private case object Init
  private case object Ack
  private case object Complete

  private trait GatewayPayload
  private case class Hello(heartbeatInterval: Int) extends GatewayPayload
  private case class Event(json: Json) extends GatewayPayload
  private case class UnsupportedMessage(text: String) extends GatewayPayload
  private case class Ready(sessionId: String) extends GatewayPayload
  private case class MessageCreate(content: String) extends GatewayPayload
  private case object Reconnect extends GatewayPayload
  private case object HeartBeatAck extends GatewayPayload
  private case object StatusUpdate extends GatewayPayload
  private case object InvalidSession extends GatewayPayload

  private case object HeartBeat
  private case class NewSeq(s: Int)

  case object Disconnect

  private def mapToGatewayPayload(text: String, ref: ActorRef): GatewayPayload = {
    val json = parse(text).getOrElse(Json.Null)
    val op = json.hcursor.get[Int]("op").toOption
    val s = json.hcursor.get[Int]("s").toOption

    s.foreach(newSeq => ref ! NewSeq(newSeq))

    def mapToHello = {
      val heartbeatPath = root.d.heartbeat_interval.int
      val heartbeatInterval = heartbeatPath.getOption(json).get
      Hello(heartbeatInterval)
    }

    op.getOrElse(-1) match {
      case 0 => mapToEvent(json)
      case 3 => StatusUpdate
      case 7 => Reconnect
      case 9 => InvalidSession
      case 10 => mapToHello
      case 11 => HeartBeatAck
      case _ => UnsupportedMessage(text)
    }
  }

  private def mapToEvent(json: Json): GatewayPayload = {
    val t = json.hcursor.get[String]("t").toOption
    t.getOrElse("UNDEFINED") match {
      case "READY" =>
        val sessionIdPath = root.d.session_id.string
        val sessionId = sessionIdPath.getOption(json).get
        Ready(sessionId)
      case "MESSAGE_CREATE" =>
        val contentPath = root.d.content.string
        val content = contentPath.getOption(json).get
        MessageCreate(content)
      case _ =>
        Event(json)
    }
  }

  private def identify(token: String) = TextMessage(
    s"""{
       |    "op": 2,
       |    "d": {
       |        "token": "$token",
       |        "properties": {
       |            "$$os": "macos",
       |            "$$browser": "akkord",
       |            "$$device": "akkord"
       |        },
       |        "compress": false,
       |        "large_threshold": 50,
       |        "shard": [
       |            0,
       |            1
       |        ],
       |        "presence": {
       |            "game": null,
       |            "status": "online",
       |            "since": null,
       |            "afk": false
       |        }
       |    }
       |}""".stripMargin
  )

  private def heartbeat(lastSeq: Option[Int]) = TextMessage(
    s"""
       |{
       |    "op": 1,
       |    "d": ${lastSeq.orNull}
       |}
    """.stripMargin
  )

  private def resume(token: String, sessionId: String, lastSeq: Option[Int]) = TextMessage(
    s"""
       |{
       |    "op": 6,
       |    "d": {
       |        "token": "$token",
       |        "session_id": "$sessionId",
       |        "seq": ${lastSeq.orNull}
       |    }
       |}
     """.stripMargin
  )
}
