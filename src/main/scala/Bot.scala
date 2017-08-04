import DiscordBot.{Ack, MessageCreated}
import Messenger.Message
import akka.actor.Actor

class Bot(val token: String) extends Actor with DiscordBot {

  override def receiveFromDiscord = {
    case MessageCreated(id, "ping") =>
      println("message received")
      messenger ! Message(id, "pong")
      sender ! Ack
  }
}

