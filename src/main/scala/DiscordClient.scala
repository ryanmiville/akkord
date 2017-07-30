
import DiscordClient._
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy}
import io.circe.Json
import io.circe.parser._

object DiscordClient {
  private case object Init
  private case object Ack
  private case object Complete
  private case object Hello
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
      .to(Sink.actorRefWithAck(self, Init, Ack, Complete))
      .run()

  override def receive = {
    case Init =>
      sender ! Ack
    case TextMessage.Strict(message) =>
      println(s"received message: [$message]")
      val json = parse(message).getOrElse(Json.Null)
      val op = json.hcursor.get[Int]("op").toOption
      if(op.get == 10) self ! Hello
      sender ! Ack
    case Hello =>
      println("Sending Identify.")
      queue.offer(TextMessage(identify))
    case Disconnect =>
      println("Stopping")
      killswitch.shutdown()
    case Complete =>
      println("The stream has terminated")
      context.stop(self)
  }
}
