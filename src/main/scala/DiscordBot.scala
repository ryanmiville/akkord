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

  protected implicit val ec           = context.system.dispatcher
  protected implicit val system       = context.system
  protected implicit val materializer = ActorMaterializer()
  protected implicit val timeout      = Timeout(10 seconds)

  private val payloadParser = system.actorOf(Props(classOf[PayloadParser], self))

  private var lastSeq: Option[Int] = None
  private var sessionId = ""

  private val (queue, killswitch) =
    Source.queue[WsMessage](Int.MaxValue, OverflowStrategy.dropBuffer)
      .via(Http().webSocketClientFlow(WebSocketRequest("wss://gateway.discord.gg?v=6&encoding=json")))
      .viaMat(KillSwitches.single)(Keep.both)
      .collect { case tm: TextMessage.Strict => tm }
      .mapAsync(10)(msg => (payloadParser ? msg).mapTo[GatewayPayload])
      .to(Sink.actorRefWithAck(self, Init, Ack, Complete))
      .run()

  override def receive: Receive =
    botBehavior orElse
    receiveStreamPlumbing orElse
    receiveGatewayPayload orElse
    { case _ => sender ! Ack }

  private def receiveStreamPlumbing: Receive = {
    case Init       => sender ! Ack
    case Disconnect => killswitch.shutdown()
    case Complete   => context.stop(self)
  }

  private def receiveGatewayPayload: Receive = {
    case Hello(interval) => scheduleHeartBeat(interval)
    case HeartBeat       => queue.offer(heartbeat(lastSeq))
    case HeartBeatAck    => sender ! Ack
    case Event(json)     => sender ! Ack
    case Ready(id)       => saveSessionId(id)
    case Reconnect       => attemptReconnect
    case NewSeq(s)       => lastSeq = Some(s)
  }

  private def attemptReconnect: Unit = {
    queue.offer(resume(token, sessionId, lastSeq))
    sender ! Ack
  }

  private def saveSessionId(id: String): Unit = {
    sessionId = id
    sender ! Ack
  }

  private def scheduleHeartBeat(interval: Int): Unit = {
    queue.offer(identify(token))
    system.scheduler.schedule(interval millis, interval millis, self, HeartBeat)
    sender ! Ack
  }

  def botBehavior: Receive
}

object DiscordBot {
  private case object Init
  case object Ack
  private case object Complete

  case object Disconnect

  trait GatewayPayload
  case class Hello(heartbeatInterval: Int)                       extends GatewayPayload
  case class Event(json: Json)                                   extends GatewayPayload
  case class UnsupportedMessage(text: String)                    extends GatewayPayload
  case class Ready(sessionId: String)                            extends GatewayPayload
  case class MessageCreated(channelId: String, content: String*) extends GatewayPayload
  case object NonUserMessageCreated                              extends GatewayPayload
  case object Reconnect                                          extends GatewayPayload
  case object HeartBeatAck                                       extends GatewayPayload
  case object StatusUpdate                                       extends GatewayPayload
  case object InvalidSession                                     extends GatewayPayload

  case object HeartBeat
  case class NewSeq(s: Int)

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