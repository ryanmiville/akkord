package akkord

import akka.actor.Actor
import akka.http.scaladsl.model.ws.TextMessage
import akkord.WebsocketConnectionBehavior._
import akkord.events.Ready
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json

import scala.concurrent.duration._
import scala.language.postfixOps

abstract class DiscordBot(token: String) extends Actor
  with WebsocketConnectionBehavior with FailFastCirceSupport {
  import DiscordBot._

  type ReceiveEvent = DiscordBot.ReceiveEvent

  private var lastSeq: Option[Int] = None
  private var sessionId = ""

  override def receive: Receive = connecting

  override def connected: Receive =
    botBehaviorWithBackPressure orElse
    receiveStreamPlumbing orElse
    receiveGatewayPayload orElse
    { case _ => sender ! Ack }

  private def receiveGatewayPayload: Receive = {
    case Hello(interval)     => scheduleHeartBeat(interval)
    case HeartBeat(interval) => sendHeartBeat(interval)
    case HeartBeatAck        => sender ! Ack
    case Event(json)         => sender ! Ack
    case r: Ready           => saveSessionId(r.session_id)
    case Reconnect           => attemptReconnect
    case NewSeq(s)           => lastSeq = Some(s)
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
    system.scheduler.scheduleOnce(interval millis, self, HeartBeat(interval))
    sender ! Ack
  }

  private def sendHeartBeat(interval: Int): Unit = {
    queue.offer(heartbeat(lastSeq))
    system.scheduler.scheduleOnce(interval millis, self, HeartBeat(interval))
    sender ! Ack
  }

  private def botBehaviorWithBackPressure: Receive = {
    case event: events.Event =>
      Some(event) collect botBehavior
      sender ! Ack
  }
  
  def botBehavior: ReceiveEvent
}

object DiscordBot {
  trait GatewayPayload
  case class Hello(heartbeatInterval: Int)                            extends GatewayPayload
  case class Event(json: Json)                                        extends GatewayPayload
  case class UnsupportedMessage(text: String)                         extends GatewayPayload
  case object NonUserMessageCreated                                   extends GatewayPayload
  case object Reconnect                                               extends GatewayPayload
  case object HeartBeatAck                                            extends GatewayPayload
  case object StatusUpdate                                            extends GatewayPayload
  case object InvalidSession                                          extends GatewayPayload

  case class HeartBeat(interval: Int)
  case class NewSeq(s: Int)

  type ReceiveEvent = PartialFunction[events.Event, Unit]

  private val os = System.getProperty("os.name")

  private def identify(token: String) = TextMessage(
    s"""{
       |    "op": 2,
       |    "d": {
       |        "token": "$token",
       |        "properties": {
       |            "$$os": "$os",
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