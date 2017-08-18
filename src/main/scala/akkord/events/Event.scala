package akkord.events

import akkord.DiscordBot.GatewayPayload

sealed trait Event extends GatewayPayload

case class MessageCreate
(
  id: String,
  channel_id: String,
  author: User,
  content: String,
  timestamp: String,
  edited_timestamp: Option[String],
  tts: Boolean,
  mention_everyone: Boolean,
  mentions: List[User],
  mention_roles: List[String],
  attachments: List[Attachment],
  embeds: List[Embed],
  reactions: Option[List[Reaction]],
  nonce: Option[String],
  pinned: Boolean,
  webhook: Option[String],
  `type`: Int) extends Message with Event

case class MessageUpdate
(
  id: String,
  channel_id: String,
  author: User,
  content: String,
  timestamp: String,
  edited_timestamp: Option[String],
  tts: Boolean,
  mention_everyone: Boolean,
  mentions: List[User],
  mention_roles: List[String],
  attachments: List[Attachment],
  embeds: List[Embed],
  reactions: Option[List[Reaction]],
  nonce: Option[String],
  pinned: Boolean,
  webhook: Option[String],
  `type`: Int) extends Message with Event

case class MessageDelete(id: String, channel_id: String) extends Event

case class MessageDeleteBulk(ids: List[String], channel_id: String) extends Event

case class MessageReactionAdd(user_id: String, channel_id: String, message_id: String, emoji: Emoji) extends Event

case class MessageReactionRemove(user_id: String, channel_id: String, message_id: String, emoji: Emoji) extends Event

case class MessageReactionRemoveAll(channel_id: String, message_id: String) extends Event

object Event {

}
