import DiscordBot.{Ack, MessageCreated}
import Messenger.Message

class Bot(val token: String) extends DiscordBot {

  override def receiveFromDiscord = {
    case MessageCreated(id, "ping") =>
      println("message received")
      messenger ! Message(id, "pong")
      sender ! Ack
  }
}

