package akkord.events

import akkord.DiscordBot.GatewayPayload

trait Event extends GatewayPayload

case class MessageCreate(message: Message) extends Event
case class MessageUpdate(message: Message) extends Event
object Event {

}
