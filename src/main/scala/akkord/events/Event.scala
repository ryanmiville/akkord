package akkord.events

import akkord.DiscordBot.GatewayPayload

sealed trait Event extends GatewayPayload

case class Ready
(
  v: Int,
  user: User,
  private_channels: List[Channel],
  guilds: List[UnavailableGuild],
  session_id: String,
  _trace: List[String]
) extends Event

case class Resumed(_trace: List[String]) extends Event

case class ChannelCreate
(
  id: String,
  `type`: String,
  guild_id: Option[String],
  position: Option[Int],
  permission_overwrites: Option[List[Overwrite]],
  name: Option[String],
  topic: Option[String],
  last_message_id: Option[String],
  bitrate: Option[Int],
  user_limit: Option[Int],
  recipients: Option[List[User]],
  icon: Option[String],
  owner_id: Option[String],
  application_id: Option[String]
) extends Channel with Event

case class ChannelUpdate
(
  id: String,
  `type`: String,
  guild_id: Option[String],
  position: Option[Int],
  permission_overwrites: Option[List[Overwrite]],
  name: Option[String],
  topic: Option[String],
  last_message_id: Option[String],
  bitrate: Option[Int],
  user_limit: Option[Int],
  recipients: Option[List[User]],
  icon: Option[String],
  owner_id: Option[String],
  application_id: Option[String]
) extends Channel with Event

case class ChannelDelete
(
  id: String,
  `type`: String,
  guild_id: Option[String],
  position: Option[Int],
  permission_overwrites: Option[List[Overwrite]],
  name: Option[String],
  topic: Option[String],
  last_message_id: Option[String],
  bitrate: Option[Int],
  user_limit: Option[Int],
  recipients: Option[List[User]],
  icon: Option[String],
  owner_id: Option[String],
  application_id: Option[String]
) extends Channel with Event

case class GuildDelete
(
  override val id: String,
  override val unavailable: Boolean
) extends UnavailableGuild(id, unavailable) with Event

case class ChannelPinsUpdate(channel_id: String, last_pin_timestamp: Option[String]) extends Event

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
