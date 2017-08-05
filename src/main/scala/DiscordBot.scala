import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.pattern.ask
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy}
import akka.util.Timeout
import io.circe.Json

import scala.concurrent.duration._

abstract class DiscordBot(token: String) extends Actor {
  import DiscordBot._

  private implicit val executionContext = context.system.dispatcher
  private implicit val system = context.system
  private implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(10 seconds)

  protected val messenger = system.actorOf(Props(classOf[Messenger], token, materializer))

  private val payloadParser = system.actorOf(Props(classOf[PayloadParser], self))
  private var lastSeq: Option[Int] = None

  private var sessionId = ""
  private val (queue, killswitch) =
    Source.queue[WsMessage](Int.MaxValue, OverflowStrategy.dropBuffer)
      .via(Http().webSocketClientFlow(WebSocketRequest("wss://gateway.discord.gg?v=6&encoding=json")))
      .viaMat(KillSwitches.single)(Keep.both)
      .collect { case tm: TextMessage.Strict => tm }
      .mapAsync(1)(msg => (payloadParser ? msg).mapTo[GatewayPayload])
      .to(Sink.actorRefWithAck(self, Init, Ack, Complete))
      .run()

  override def receive =
    receiveFromDiscord orElse
    receiveStreamPlumbing orElse
    receiveGatewayPayload orElse
    { case _ => sender ! Ack }

  private def receiveStreamPlumbing: Receive = {
    case Init =>
      sender ! Ack
    case Disconnect =>
      killswitch.shutdown()
    case Complete =>
      context.stop(self)
  }

  private def receiveGatewayPayload: Receive = {
    case Hello(interval) =>
      queue.offer(identify(token))
      system.scheduler.schedule(interval millis, interval millis, self, HeartBeat)
      sender ! Ack
    case HeartBeat =>
      queue.offer(heartbeat(lastSeq))
    case HeartBeatAck =>
      sender ! Ack
    case Event(json) =>
      println(json)
      sender ! Ack
    case Ready(id) =>
      sessionId = id
      sender ! Ack
    case Reconnect =>
      queue.offer(resume(token, sessionId, lastSeq))
      sender ! Ack
    case NewSeq(s) =>
      lastSeq = Some(s)
  }

  def receiveFromDiscord: Receive
}

object DiscordBot {
  private case object Init
  case object Ack
  private case object Complete

  trait GatewayPayload
  case class Hello(heartbeatInterval: Int) extends GatewayPayload
  case class Event(json: Json) extends GatewayPayload
  case class UnsupportedMessage(text: String) extends GatewayPayload
  case class Ready(sessionId: String) extends GatewayPayload
  case class MessageCreated(channelId: String, content: String*) extends GatewayPayload
  case object NonUserMessageCreated extends GatewayPayload
  case object Reconnect extends GatewayPayload
  case object HeartBeatAck extends GatewayPayload
  case object StatusUpdate extends GatewayPayload
  case object InvalidSession extends GatewayPayload

  case object HeartBeat
  case class NewSeq(s: Int)

  case object Disconnect

  private type WsMessage = akka.http.scaladsl.model.ws.Message

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
