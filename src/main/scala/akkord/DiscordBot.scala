package akkord

import akkord.DiscordBot.DoNothing
import akkord.events.Event._

abstract class DiscordBot(token: String) extends DiscordBotActor(token) {
  type ReceiveMessageContent = DiscordBot.ReceiveMessageContent

  override def botBehavior: Receive = {
    case e: ChannelCreate            => onChannelCreate(e)
    case e: ChannelUpdate            => onChannelUpdate(e)
    case e: ChannelDelete            => onChannelDelete(e)
    case e: GuildDelete              => onGuildDelete(e)
    case e: ChannelPinsUpdate        => onChannelPinsUpdate(e)
    case e: GuildCreate              => onGuildCreate(e)
    case e: GuildUpdate              => onGuildUpdate(e)
    case e: GuildBanAdd              => onGuildBanAdd(e)
    case e: GuildBanRemove           => onGuildBanRemove(e)
    case e: GuildEmojisUpdate        => onGuildEmojisUpdate(e)
    case e: GuildIntegrationsUpdate  => onGuildIntegrationsUpdate(e)
    case e: GuildMemberAdd           => onGuildMemberAdd(e)
    case e: GuildMemberRemove        => onGuildMemberRemove(e)
    case e: GuildMemberUpdate        => onGuildMemberUpdate(e)
    case e: GuildMembersChunk        => onGuildMemberChunk(e)
    case e: GuildRoleCreate          => onGuildRoleCreate(e)
    case e: GuildRoleUpdate          => onGuildRoleUpdate(e)
    case e: GuildRoleDelete          => onGuildRoleDelete(e)
    case e: MessageCreate            => onMessageCreate(e)
    case e: MessageUpdate            => onMessageUpdate(e)
    case e: MessageDelete            => onMessageDelete(e)
    case e: MessageDeleteBulk        => onMessageDeleteBulk(e)
    case e: MessageReactionAdd       => onMessageReactionAdd(e)
    case e: MessageReactionRemove    => onMessageReactionRemove(e)
    case e: MessageReactionRemoveAll => onMessageReactionRemoveAll(e)
    case e: PresenceUpdate           => onPresenceUpdate(e)
    case e: TypingStart              => onTypingStart(e)
    case e: UserUpdate               => onUserUpdate(e)
    case e: VoiceStateUpdate         => onVoiceStateUpdate(e)
    case e: VoiceServerUpdate        => onVoiceServerUpdate(e)
    case e: WebhooksUpdate           => onWebhooksUpdate(e)
  }

  def onChannelCreate(channelCreate: ChannelCreate): Unit = DoNothing

  def onChannelUpdate(channelUpdate: ChannelUpdate): Unit = DoNothing

  def onChannelDelete(channelDelete: ChannelDelete): Unit = DoNothing

  def onGuildDelete(guildDelete: GuildDelete): Unit = DoNothing

  def onChannelPinsUpdate(channelPinsUpdate: ChannelPinsUpdate): Unit = DoNothing

  def onGuildCreate(guildCreate: GuildCreate): Unit = DoNothing

  def onGuildUpdate(guildUpdate: GuildUpdate): Unit = DoNothing

  def onGuildBanAdd(guildBanAdd: GuildBanAdd): Unit = DoNothing

  def onGuildBanRemove(guildBanRemove: GuildBanRemove): Unit = DoNothing

  def onGuildEmojisUpdate(guildEmojisUpdate: GuildEmojisUpdate): Unit = DoNothing

  def onGuildIntegrationsUpdate(guildIntegrationsUpdate: GuildIntegrationsUpdate): Unit = DoNothing

  def onGuildMemberAdd(guildMemberAdd: GuildMemberAdd): Unit = DoNothing

  def onGuildMemberRemove(guildMemberRemove: GuildMemberRemove): Unit = DoNothing

  def onGuildMemberUpdate(guildMemberUpdate: GuildMemberUpdate): Unit = DoNothing

  def onGuildMemberChunk(guildMembersChunk: GuildMembersChunk): Unit = DoNothing

  def onGuildRoleCreate(guildRoleCreate: GuildRoleCreate): Unit = DoNothing

  def onGuildRoleUpdate(guildRoleUpdate: GuildRoleUpdate): Unit = DoNothing

  def onGuildRoleDelete(guildRoleDelete: GuildRoleDelete): Unit = DoNothing

  def onMessageCreate(message: MessageCreate): Unit = {
    val content = message.content.split(" ").toList
    Some(content) collect onMessageContent(message)
  }

  def onMessageContent(message: MessageCreate): ReceiveMessageContent = {
    case _ => DoNothing
  }

  def onMessageUpdate(messageUpdate: MessageUpdate): Unit = DoNothing

  def onMessageDelete(messageDelete: MessageDelete): Unit = DoNothing

  def onMessageDeleteBulk(messageDeleteBulk: MessageDeleteBulk): Unit = DoNothing

  def onMessageReactionAdd(messageReactionAdd: MessageReactionAdd): Unit = DoNothing

  def onMessageReactionRemove(messageReactionRemove: MessageReactionRemove): Unit = DoNothing

  def onMessageReactionRemoveAll(messageReactionRemoveAll: MessageReactionRemoveAll): Unit = DoNothing

  def onPresenceUpdate(presenceUpdate: PresenceUpdate): Unit = DoNothing

  def onTypingStart(typingStart: TypingStart): Unit = DoNothing

  def onUserUpdate(userUpdate: UserUpdate): Unit = DoNothing

  def onVoiceStateUpdate(voiceStateUpdate: VoiceStateUpdate): Unit = DoNothing

  def onVoiceServerUpdate(voiceServerUpdate: VoiceServerUpdate): Unit = DoNothing

  def onWebhooksUpdate(webhooksUpdate: WebhooksUpdate): Unit = DoNothing
}

object DiscordBot {
  type ReceiveMessageContent = PartialFunction[List[String], Unit]
  private case object DoNothing
}
