package akkord.parser

import akka.actor.Actor
import akkord.DiscordBot
import akkord.events.Event._
import io.circe.Decoder.Result
import io.circe.HCursor

class EventParser extends Actor {
  import EventParser._
  import io.circe.generic.auto._

  override def receive: Receive = {
    case RawEvent("READY", cursor)                       => sendEvent(cursor.downField("d").as[Ready])
    case RawEvent("RESUMED", cursor)                     => sendEvent(cursor.downField("d").as[Resumed])
    case RawEvent("CHANNEL_CREATE", cursor)              => sendEvent(cursor.downField("d").as[ChannelCreate])
    case RawEvent("CHANNEL_UPDATE", cursor)              => sendEvent(cursor.downField("d").as[ChannelUpdate])
    case RawEvent("CHANNEL_DELETE", cursor)              => sendEvent(cursor.downField("d").as[ChannelDelete])
    case RawEvent("GUILD_DELETE", cursor)                => sendEvent(cursor.downField("d").as[GuildDelete])
    case RawEvent("CHANNEL_PINS_UPDATE", cursor)         => sendEvent(cursor.downField("d").as[ChannelPinsUpdate])
    case RawEvent("GUILD_CREATE", cursor)                => sendEvent(cursor.downField("d").as[GuildCreate])
    case RawEvent("GUILD_UPDATE", cursor)                => sendEvent(cursor.downField("d").as[GuildUpdate])
    case RawEvent("GUILD_BAN_ADD", cursor)               => sendEvent(cursor.downField("d").as[GuildBanAdd])
    case RawEvent("GUILD_BAN_REMOVE", cursor)            => sendEvent(cursor.downField("d").as[GuildBanRemove])
    case RawEvent("GUILD_EMOJIS_UPDATE", cursor)         => sendEvent(cursor.downField("d").as[GuildEmojisUpdate])
    case RawEvent("GUILD_INTEGRATIONS_UPDATE", cursor)   => sendEvent(cursor.downField("d").as[GuildIntegrationsUpdate])
    case RawEvent("GUILD_MEMBER_ADD", cursor)            => sendEvent(cursor.downField("d").as[GuildMemberAdd])
    case RawEvent("GUILD_MEMBER_REMOVE", cursor)         => sendEvent(cursor.downField("d").as[GuildMemberRemove])
    case RawEvent("GUILD_MEMBER_UPDATE", cursor)         => sendEvent(cursor.downField("d").as[GuildMemberUpdate])
    case RawEvent("GUILD_MEMBERS_CHUNK", cursor)         => sendEvent(cursor.downField("d").as[GuildMembersChunk])
    case RawEvent("GUILD_ROLE_CREATE", cursor)           => sendEvent(cursor.downField("d").as[GuildRoleCreate])
    case RawEvent("GUILD_ROLE_UPDATE", cursor)           => sendEvent(cursor.downField("d").as[GuildRoleUpdate])
    case RawEvent("GUILD_ROLE_DELETE", cursor)           => sendEvent(cursor.downField("d").as[GuildRoleDelete])
    case RawEvent("MESSAGE_CREATE", cursor)              => sendEvent(cursor.downField("d").as[MessageCreate])
    case RawEvent("MESSAGE_UPDATE", cursor)              => sendEvent(cursor.downField("d").as[MessageUpdate])
    case RawEvent("MESSAGE_DELETE", cursor)              => sendEvent(cursor.downField("d").as[MessageDelete])
    case RawEvent("MESSAGE_DELETE_BULK", cursor)         => sendEvent(cursor.downField("d").as[MessageDeleteBulk])
    case RawEvent("MESSAGE_REACTION_ADD", cursor)        => sendEvent(cursor.downField("d").as[MessageReactionAdd])
    case RawEvent("MESSAGE_REACTION_REMOVE", cursor)     => sendEvent(cursor.downField("d").as[MessageReactionRemove])
    case RawEvent("MESSAGE_REACTION_REMOVE_ALL", cursor) => sendEvent(cursor.downField("d").as[MessageReactionRemoveAll])
    case RawEvent("PRESENCE_UPDATE", cursor)             => sendEvent(cursor.downField("d").as[PresenceUpdate])
    case RawEvent("TYPING_START", cursor)                => sendEvent(cursor.downField("d").as[TypingStart])
    case RawEvent("USER_UPDATE", cursor)                 => sendEvent(cursor.downField("d").as[UserUpdate])
    case RawEvent("VOICE_STATE_UPDATE", cursor)          => sendEvent(cursor.downField("d").as[VoiceStateUpdate])
    case RawEvent("VOICE_SERVER_UPDATE", cursor)         => sendEvent(cursor.downField("d").as[VoiceServerUpdate])
    case RawEvent("WEBHOOKS_UPDATE", cursor)             => sendEvent(cursor.downField("d").as[WebhooksUpdate])
    case RawEvent(_, cursor)                             => sender ! DiscordBot.UnknownEvent(cursor.value)
  }

  def sendEvent(event: Result[Event]): Unit = {
    println(event)
    sender ! event.getOrElse(AkkordParsingError)
  }
}

object EventParser {
  case class RawEvent(name: String, cursor: HCursor)
}
