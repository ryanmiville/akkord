import ChannelApi.Message
import DiscordBot.{Ack, MessageCreated}

class Bot(token: String) extends DiscordBot(token) {

  val channel = system.actorOf(ChannelApi.props(token))

  override def receiveFromDiscord = {
    case MessageCreated(id, "ping") =>
      channel ! Message(id, "pong")
      sender ! Ack
  }
}

