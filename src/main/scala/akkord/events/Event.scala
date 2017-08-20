package akkord.events

import akkord.DiscordBot.GatewayPayload

object Event {

  sealed trait Event extends GatewayPayload

  case class Ready
  (
    v: Int,
    user: UserImpl,
    private_channels: List[ChannelImpl],
    guilds: List[UnavailableGuild],
    session_id: String,
    _trace: List[String]
  ) extends Event

  case class Resumed(_trace: List[String]) extends Event

  case class ChannelCreate
  (
    override val id: String,
    override val `type`: Int,
    override val guild_id: Option[String],
    override val position: Option[Int],
    override val permission_overwrites: Option[List[Overwrite]],
    override val name: Option[String],
    override val topic: Option[String],
    override val last_message_id: Option[String],
    override val bitrate: Option[Int],
    override val user_limit: Option[Int],
    override val recipients: Option[List[UserImpl]],
    override val icon: Option[String],
    override val owner_id: Option[String],
    override val application_id: Option[String],
    override val nsfw: Option[Boolean]
  ) extends Channel with Event

  case class ChannelUpdate
  (
    override val id: String,
    override val `type`: Int,
    override val guild_id: Option[String],
    override val position: Option[Int],
    override val permission_overwrites: Option[List[Overwrite]],
    override val name: Option[String],
    override val topic: Option[String],
    override val last_message_id: Option[String],
    override val bitrate: Option[Int],
    override val user_limit: Option[Int],
    override val recipients: Option[List[UserImpl]],
    override val icon: Option[String],
    override val owner_id: Option[String],
    override val application_id: Option[String],
    override val nsfw: Option[Boolean]
  ) extends Channel with Event

  case class ChannelDelete
  (
    override val id: String,
    override val `type`: Int,
    override val guild_id: Option[String],
    override val position: Option[Int],
    override val permission_overwrites: Option[List[Overwrite]],
    override val name: Option[String],
    override val topic: Option[String],
    override val last_message_id: Option[String],
    override val bitrate: Option[Int],
    override val user_limit: Option[Int],
    override val recipients: Option[List[UserImpl]],
    override val icon: Option[String],
    override val owner_id: Option[String],
    override val application_id: Option[String],
    override val nsfw: Option[Boolean]
  ) extends Channel with Event

  case class GuildDelete
  (
    id: String,
    unavailable: Boolean
  ) extends Event

  case class ChannelPinsUpdate(channel_id: String, last_pin_timestamp: Option[String]) extends Event

  case class GuildCreate
  (
    override val id: String,
    override val name: String,
    override val icon: Option[String],
    override val splash: Option[String],
    override val owner_id: String,
    override val region: String,
    override val afk_channel_id: Option[String],
    override val afk_timeout: Int,
    override val embed_enabled: Option[Boolean],
    override val embed_channel_id: Option[String],
    override val verification_level: Int,
    override val default_message_notifications: Int,
    override val explicit_content_filter: Int,
    override val roles: List[Role],
    override val emojis: List[Emoji],
    override val features: List[String],
    override val mfa_level: Int,
    override val application_id: Option[String],
    override val widget_enabled: Option[Boolean],
    override val widget_channel_id: Option[String]
  ) extends Guild with Event

  case class GuildUpdate
  (
    override val id: String,
    override val name: String,
    override val icon: Option[String],
    override val splash: Option[String],
    override val owner_id: String,
    override val region: String,
    override val afk_channel_id: Option[String],
    override val afk_timeout: Int,
    override val embed_enabled: Option[Boolean],
    override val embed_channel_id: Option[String],
    override val verification_level: Int,
    override val default_message_notifications: Int,
    override val explicit_content_filter: Int,
    override val roles: List[Role],
    override val emojis: List[Emoji],
    override val features: List[String],
    override val mfa_level: Int,
    override val application_id: Option[String],
    override val widget_enabled: Option[Boolean],
    override val widget_channel_id: Option[String]
  ) extends Guild with Event

  case class GuildBanAdd
  (
    id: String,
    username: Option[String],
    discriminator: Option[String],
    avatar: Option[String],
    bot: Option[Boolean],
    mfa_enabled: Option[Boolean],
    verified: Option[Boolean],
    email: Option[String],
    guild_id: String
  ) extends User with Event

  case class GuildBanRemove
  (
    id: String,
    username: Option[String],
    discriminator: Option[String],
    avatar: Option[String],
    bot: Option[Boolean],
    mfa_enabled: Option[Boolean],
    verified: Option[Boolean],
    email: Option[String],
    guild_id: String
  ) extends User with Event

  case class GuildEmojisUpdate(guild_id: String, emojis: List[Emoji]) extends Event

  case class GuildIntegrationsUpdate(guild_id: String) extends Event

  case class GuildMemberAdd
  (
    user: UserImpl,
    nick: Option[String],
    roles: List[String],
    joined_at: String,
    deaf: Boolean,
    mute: Boolean,
    guild_id: String
  ) extends GuildMember with Event

  case class GuildMemberRemove(guild_id: String, user: UserImpl) extends Event

  case class GuildMemberUpdate(guild_id: String, roles: List[String], user: UserImpl, nick: Option[String]) extends Event

  case class GuildMembersChunk(guild_id: String, members: List[GuildMemberImpl]) extends Event

  case class GuildRoleCreate(guild_id: String, role: Role) extends Event

  case class GuildRoleUpdate(guild_id: String, role: Role) extends Event

  case class GuildRoleDelete(guild_id: String, role_id: String) extends Event


  case class MessageCreate
  (
    id: String,
    channel_id: String,
    author: UserImpl,
    content: String,
    timestamp: String,
    edited_timestamp: Option[String],
    tts: Boolean,
    mention_everyone: Boolean,
    mentions: List[UserImpl],
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
    author: UserImpl,
    content: String,
    timestamp: String,
    edited_timestamp: Option[String],
    tts: Boolean,
    mention_everyone: Boolean,
    mentions: List[UserImpl],
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

  case class PresenceUpdate
  (
    user: Option[UserImpl],
    roles: Option[List[String]],
    game: Option[Game],
    guild_id: Option[String],
    status: Option[String]
  ) extends Presence with Event

  case class TypingStart(channel_id: String, user_id: String, timestamp: Int) extends Event

  case class UserUpdate
  (
    id: String,
    username: Option[String],
    discriminator: Option[String],
    avatar: Option[String],
    bot: Option[Boolean],
    mfa_enabled: Option[Boolean],
    verified: Option[Boolean],
    email: Option[String],
  ) extends User with Event

  case class VoiceStateUpdate
  (
    override val channel_id: Option[String],
    override val user_id: String,
    override val session_id: String,
    override val guild_id: Option[String],
    override val deaf: Boolean,
    override val mute: Boolean,
    override val self_deaf: Boolean,
    override val self_mute: Boolean,
    override val suppress: Boolean
  ) extends VoiceState with Event

  case class VoiceServerUpdate(token: String, guild_id: String, endpoint: String) extends Event

  case class WebhooksUpdate(guild_id: String, channel_id: String) extends Event

  case object AkkordParsingError extends Event

}
