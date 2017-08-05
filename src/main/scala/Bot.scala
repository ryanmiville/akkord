import DiscordBot.{Ack, MessageCreated}
import Messenger.Message

class Bot(token: String) extends DiscordBot(token) {

  override def receiveFromDiscord = {
    case MessageCreated(id, "ping") =>
      println("message received")
      messenger ! Message(id, "pong")
      sender ! Ack
  }
}

