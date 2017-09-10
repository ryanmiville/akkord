package akkord

import akkord.DiscordBot.DoNothing
import akkord.events.Event._

abstract class DiscordBot(token: String) extends DiscordBotActor(token) {

  override def botBehavior: Receive = {
    case e: ChannelCreate            => Some(e) collect onChannelCreate
    case e: ChannelUpdate            => Some(e) collect onChannelUpdate
    case e: ChannelDelete            => Some(e) collect onChannelDelete
    case e: GuildDelete              => Some(e) collect onGuildDelete
    case e: ChannelPinsUpdate        => Some(e) collect onChannelPinsUpdate
    case e: GuildCreate              => Some(e) collect onGuildCreate
    case e: GuildUpdate              => Some(e) collect onGuildUpdate
    case e: GuildBanAdd              => Some(e) collect onGuildBanAdd
    case e: GuildBanRemove           => Some(e) collect onGuildBanRemove
    case e: GuildEmojisUpdate        => Some(e) collect onGuildEmojisUpdate
    case e: GuildIntegrationsUpdate  => Some(e) collect onGuildIntegrationsUpdate
    case e: GuildMemberAdd           => Some(e) collect onGuildMemberAdd
    case e: GuildMemberRemove        => Some(e) collect onGuildMemberRemove
    case e: GuildMemberUpdate        => Some(e) collect onGuildMemberUpdate
    case e: GuildMembersChunk        => Some(e) collect onGuildMemberChunk
    case e: GuildRoleCreate          => Some(e) collect onGuildRoleCreate
    case e: GuildRoleUpdate          => Some(e) collect onGuildRoleUpdate
    case e: GuildRoleDelete          => Some(e) collect onGuildRoleDelete
    case e: MessageCreate            => Some(e) collect onMessageCreate
    case e: MessageUpdate            => Some(e) collect onMessageUpdate
    case e: MessageDelete            => Some(e) collect onMessageDelete
    case e: MessageDeleteBulk        => Some(e) collect onMessageDeleteBulk
    case e: MessageReactionAdd       => Some(e) collect onMessageReactionAdd
    case e: MessageReactionRemove    => Some(e) collect onMessageReactionRemove
    case e: MessageReactionRemoveAll => Some(e) collect onMessageReactionRemoveAll
    case e: PresenceUpdate           => Some(e) collect onPresenceUpdate
    case e: TypingStart              => Some(e) collect onTypingStart
    case e: UserUpdate               => Some(e) collect onUserUpdate
    case e: VoiceStateUpdate         => Some(e) collect onVoiceStateUpdate
    case e: VoiceServerUpdate        => Some(e) collect onVoiceServerUpdate
    case e: WebhooksUpdate           => Some(e) collect onWebhooksUpdate
  }

  def onChannelCreate: PartialFunction[ChannelCreate, Unit] = {
    case _ => DoNothing
  }

  def onChannelUpdate: PartialFunction[ChannelUpdate, Unit] = {
    case _ => DoNothing
  }

  def onChannelDelete: PartialFunction[ChannelDelete, Unit] = {
    case _ => DoNothing
  }

  def onGuildDelete: PartialFunction[GuildDelete, Unit] = {
    case _ => DoNothing
  }

  def onChannelPinsUpdate: PartialFunction[ChannelPinsUpdate, Unit] = {
    case _ => DoNothing
  }

  def onGuildCreate: PartialFunction[GuildCreate, Unit] = {
    case _ => DoNothing
  }

  def onGuildUpdate: PartialFunction[GuildUpdate, Unit] = {
    case _ => DoNothing
  }

  def onGuildBanAdd: PartialFunction[GuildBanAdd, Unit] = {
    case _ => DoNothing
  }

  def onGuildBanRemove: PartialFunction[GuildBanRemove, Unit] = {
    case _ => DoNothing
  }
  def onGuildEmojisUpdate: PartialFunction[GuildEmojisUpdate, Unit] = {
    case _ => DoNothing
  }

  def onGuildIntegrationsUpdate: PartialFunction[GuildIntegrationsUpdate, Unit] = {
    case _ => DoNothing
  }

  def onGuildMemberAdd: PartialFunction[GuildMemberAdd, Unit] = {
    case _ => DoNothing
  }
  def onGuildMemberRemove: PartialFunction[GuildMemberRemove, Unit] = {
    case _ => DoNothing
  }

  def onGuildMemberUpdate: PartialFunction[GuildMemberUpdate, Unit] = {
    case _ => DoNothing
  }

  def onGuildMemberChunk: PartialFunction[GuildMembersChunk, Unit] = {
    case _ => DoNothing
  }

  def onGuildRoleCreate: PartialFunction[GuildRoleCreate, Unit] = {
    case _ => DoNothing
  }

  def onGuildRoleUpdate: PartialFunction[GuildRoleUpdate, Unit] = {
    case _ => DoNothing
  }

  def onGuildRoleDelete: PartialFunction[GuildRoleDelete, Unit] = {
    case _ => DoNothing
  }

  def onMessageCreate: PartialFunction[MessageCreate, Unit] = {
    case message: MessageCreate =>
      val content = message.content.split(" ").toList
      Some(content) collect onMessageContent(message)
  }

  def onMessageContent(message: MessageCreate): PartialFunction[List[String], Unit] = {
    case _ => DoNothing
  }

  def onMessageUpdate: PartialFunction[MessageUpdate, Unit] = {
    case _ => DoNothing
  }

  def onMessageDelete: PartialFunction[MessageDelete, Unit] = {
    case _ => DoNothing
  }

  def onMessageDeleteBulk: PartialFunction[MessageDeleteBulk, Unit] = {
    case _ => DoNothing
  }

  def onMessageReactionAdd: PartialFunction[MessageReactionAdd, Unit] = {
    case _ => DoNothing
  }

  def onMessageReactionRemove: PartialFunction[MessageReactionRemove, Unit] = {
    case _ => DoNothing
  }

  def onMessageReactionRemoveAll: PartialFunction[MessageReactionRemoveAll, Unit] = {
    case _ => DoNothing
  }

  def onPresenceUpdate: PartialFunction[PresenceUpdate, Unit] = {
    case _ => DoNothing
  }

  def onTypingStart: PartialFunction[TypingStart, Unit] = {
    case _ => DoNothing
  }

  def onUserUpdate: PartialFunction[UserUpdate, Unit] = {
    case _ => DoNothing
  }

  def onVoiceStateUpdate: PartialFunction[VoiceStateUpdate, Unit] = {
    case _ => DoNothing
  }

  def onVoiceServerUpdate: PartialFunction[VoiceServerUpdate, Unit] = {
    case _ => DoNothing
  }

  def onWebhooksUpdate: PartialFunction[WebhooksUpdate, Unit] = {
    case _ => DoNothing
  }
}

object DiscordBot {
  private case object DoNothing
}
