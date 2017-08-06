package akkord

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.pattern.pipe
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy, UniqueKillSwitch}
import akka.util.Timeout
import io.circe.Json
import parser.PayloadParser
import api.DiscordApi._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.duration._

abstract class DiscordBot(token: String) extends Actor with FailFastCirceSupport{
  import DiscordBot._

  protected implicit val ec           = context.system.dispatcher
  protected implicit val system       = context.system
  protected implicit val materializer = ActorMaterializer()
  protected implicit val timeout      = Timeout(10 seconds)

  private val payloadParser = system.actorOf(Props(classOf[PayloadParser], self))

  private var connectionUrl = ""
  private var lastSeq: Option[Int] = None
  private var sessionId = ""

  var queue: SourceQueueWithComplete[DiscordBot.WsMessage] = null
  var killswitch: UniqueKillSwitch = null

  override def preStart() = {
    Http().singleRequest(HttpRequest(uri = s"$baseUrl/gateway"))
      .pipeTo(self)
  }

  override def receive: Receive = connecting

  private def connecting: Receive = {
    case HttpResponse(StatusCodes.OK, _, entity, _) =>
      import io.circe.generic.auto._
      Unmarshal(entity)
        .to[Connection]
        .pipeTo(self)
    case Connection(url) =>
      connectionUrl = url
      context become connected
      val (q, ks) =
        Source.queue[WsMessage](Int.MaxValue, OverflowStrategy.dropBuffer)
          .via(Http().webSocketClientFlow(WebSocketRequest(s"$url?v=6&encoding=json")))
          .viaMat(KillSwitches.single)(Keep.both)
          .collect { case tm: TextMessage.Strict => tm }
          .mapAsync(10)(msg => (payloadParser ? msg).mapTo[GatewayPayload])
          .to(Sink.actorRefWithAck(self, Init, Ack, Complete))
          .run()
      queue = q
      killswitch = ks
    case resp @ HttpResponse(code, _, _, _) =>
      println(s"Failed to retrieve URL: $code")
  }

  private def connected: Receive =
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
  case class Connection(url: String)

  private case object Init
  case object Ack
  private case object Complete

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