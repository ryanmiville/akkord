package akkord

import akkord.events.Event.MessageCreate

abstract class SimpleDiscordBot(token: String) extends DiscordBot(token) {
  type ReceiveMessageCreate = SimpleDiscordBot.ReceiveMessageCreate

  override def botBehavior: ReceiveEvent = {
    case msg: MessageCreate =>
      val content = msg.content.split(" ").toList
      Some(content) collect onMessage(msg)
  }

  def onMessage(message: MessageCreate): ReceiveMessageCreate
}

object SimpleDiscordBot {
  type ReceiveMessageCreate = PartialFunction[List[String], Unit]
}
