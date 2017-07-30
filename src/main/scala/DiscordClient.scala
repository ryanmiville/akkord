import DiscordClient._
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy}

object DiscordClient {
  private case object Init
  private case object Ack
  private case object Complete

  case object Disconnect
}

class DiscordClient(implicit materializer: ActorMaterializer) extends Actor {
  private implicit val executionContext = context.system.dispatcher
  private implicit val system = context.system
  private var incr = 0

  val ((queue, killswitch), _) =
    Source.queue[Message](Int.MaxValue, OverflowStrategy.dropBuffer)
      .via(Http().webSocketClientFlow(WebSocketRequest("ws://echo.websocket.org")))
      .viaMat(KillSwitches.single)(Keep.both)
      .toMat(Sink.actorRefWithAck(self, Init, Ack, Complete))(Keep.both)
      .run()

  override def receive = {
    case Init =>
      queue.offer(TextMessage(s"$incr: Hello world!"))
      sender ! Ack
    case message: TextMessage.Strict =>
      println(s"received message: [${message.text}]")
      incr += 1
      queue.offer(TextMessage(s"$incr: Hello world!"))
      sender ! Ack
    case Disconnect =>
      println("Stopping")
      killswitch.shutdown()
    case Complete =>
      println("The stream has terminated")
      context.stop(self)
  }
}
