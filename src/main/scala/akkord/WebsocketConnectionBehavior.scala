package akkord

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.{ask, pipe}
import akka.stream._
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.util.Timeout
import akkord.DiscordBot.GatewayPayload
import akkord.parser.PayloadParser
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.duration._
import scala.language.postfixOps

trait WebsocketConnectionBehavior {
  this: Actor  with FailFastCirceSupport =>
  import WebsocketConnectionBehavior._

  var connectionUrl: Option[String] = None

  var queue: SourceQueueWithComplete[Message] = _
  var killswitch: UniqueKillSwitch            = _

  val system = context.system

  protected implicit val ec           = system.dispatcher
  protected implicit val materializer = ActorMaterializer()
  protected implicit val timeout      = Timeout(10 seconds)

  private val payloadParser = system.actorOf(Props(classOf[PayloadParser], self))

  def connecting: Receive = {
    case HttpResponse(StatusCodes.OK, _, entity, _) => pipeConnection(entity)
    case Connection(url)                            => connect(url)
  }

  protected def connect(url: String): Unit = {
    context become connected
    connectionUrl = Some(url)
    val (q, ks)   = runStream(url)
    queue         = q
    killswitch    = ks
  }

  private def runStream(url: String): (SourceQueueWithComplete[Message], UniqueKillSwitch) = {
    Source.queue[Message](Int.MaxValue, OverflowStrategy.dropBuffer)
      .via(Http(context.system).webSocketClientFlow(WebSocketRequest(s"$url?v=6&encoding=json")))
      .viaMat(KillSwitches.single)(Keep.both)
      .collect { case tm: TextMessage.Strict => tm }
      .mapAsync(10)(msg => (payloadParser ? msg).mapTo[GatewayPayload])
      .to(Sink.actorRefWithAck(self, Init, Ack, Complete))
      .run()
  }

  private def pipeConnection(entity: ResponseEntity): Unit = {
    import io.circe.generic.auto._
    Unmarshal(entity)
      .to[Connection]
      .pipeTo(self)
  }

  def connected: Receive
}

object WebsocketConnectionBehavior {
  case object Init
  case object Ack
  case object Complete

  case class Connection(url: String)
}