package akkord

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.ws.TextMessage
import akka.pattern.pipe
import akkord.WebsocketConnectionBehavior._
import akkord.api.DiscordApi._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json

import scala.concurrent.duration._

abstract class DiscordBot(token: String) extends Actor
  with WebsocketConnectionBehavior with FailFastCirceSupport {
  import DiscordBot._

  private var lastSeq: Option[Int] = None
  private var sessionId = ""

  override def preStart() = {
    Http(system).singleRequest(HttpRequest(uri = s"$baseUrl/gateway"))
      .pipeTo(self)
  }

  override def receive: Receive = connecting

  override def connected: Receive =
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
  case object Disconnect

  trait GatewayPayload
  case class Hello(heartbeatInterval: Int)                            extends GatewayPayload
  case class Event(json: Json)                                        extends GatewayPayload
  case class UnsupportedMessage(text: String)                         extends GatewayPayload
  case class Ready(sessionId: String)                                 extends GatewayPayload
  case class MessageCreated(channelId: String, content: List[String]) extends GatewayPayload
  case object NonUserMessageCreated                                   extends GatewayPayload
  case object Reconnect                                               extends GatewayPayload
  case object HeartBeatAck                                            extends GatewayPayload
  case object StatusUpdate                                            extends GatewayPayload
  case object InvalidSession                                          extends GatewayPayload

  case object HeartBeat
  case class NewSeq(s: Int)

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