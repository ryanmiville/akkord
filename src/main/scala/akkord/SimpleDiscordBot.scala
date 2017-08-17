package akkord

import akkord.api.ChannelApi
import akkord.events.CreateMessage

abstract class SimpleDiscordBot(token: String) extends DiscordBot(token) {
  type MessageReply = SimpleDiscordBot.MessageReply

  private val channel = system.actorOf(ChannelApi.props(token))

  override def botBehavior: ReceiveEvent = {
    case msg: CreateMessage =>
      val content      = msg.content.split(" ").toList
      val replyContent = Some(content) collect onMessage

      replyContent foreach { rc =>
        channel ! ChannelApi.SendMessage(msg.channel_id, rc)
      }
  }

  def onMessage: MessageReply
}

object SimpleDiscordBot {
  type MessageReply = PartialFunction[List[String], String]
}
