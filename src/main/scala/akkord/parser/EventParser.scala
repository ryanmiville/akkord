package akkord.parser

import akka.actor.Actor
import akkord.DiscordBot
import akkord.events._
import io.circe.Decoder.Result
import io.circe.HCursor

class EventParser extends Actor {
  import EventParser._
  import io.circe.generic.auto._

  override def receive: Receive = {
    case Event("READY", cursor)                       => sendEvent(cursor.downField("d").as[Ready])
    case Event("RESUMED", cursor)                     => sendEvent(cursor.downField("d").as[Resumed])
    case Event("CHANNEL_CREATE", cursor)              => sendEvent(cursor.downField("d").as[ChannelCreate])
    case Event("CHANNEL_UPDATE", cursor)              => sendEvent(cursor.downField("d").as[ChannelUpdate])
    case Event("CHANNEL_DELETE", cursor)              => sendEvent(cursor.downField("d").as[ChannelDelete])
    case Event("GUILD_DELETE", cursor)                => sendEvent(cursor.downField("d").as[GuildDelete])
    case Event("CHANNEL_PINS_UPDATE", cursor)         => sendEvent(cursor.downField("d").as[ChannelPinsUpdate])
    case Event("GUILD_CREATE", cursor)                => sendEvent(cursor.downField("d").as[GuildCreate])
    case Event("GUILD_UPDATE", cursor)                => sendEvent(cursor.downField("d").as[GuildUpdate])
    case Event("GUILD_BAN_ADD", cursor)               => sendEvent(cursor.downField("d").as[GuildBanAdd])
    case Event("GUILD_BAN_REMOVE", cursor)            => sendEvent(cursor.downField("d").as[GuildBanRemove])
    case Event("GUILD_EMOJIS_UPDATE", cursor)         => sendEvent(cursor.downField("d").as[GuildEmojisUpdate])
    case Event("GUILD_INTEGRATIONS_UPDATE", cursor)   => sendEvent(cursor.downField("d").as[GuildIntegrationsUpdate])
    case Event("GUILD_MEMBER_ADD", cursor)            => sendEvent(cursor.downField("d").as[GuildMemberAdd])
    case Event("GUILD_MEMBER_REMOVE", cursor)         => sendEvent(cursor.downField("d").as[GuildMemberRemove])
    case Event("GUILD_MEMBER_UPDATE", cursor)         => sendEvent(cursor.downField("d").as[GuildMemberUpdate])
    case Event("GUILD_MEMBERS_CHUNK", cursor)         => sendEvent(cursor.downField("d").as[GuildMembersChunk])
    case Event("GUILD_ROLE_CREATE", cursor)           => sendEvent(cursor.downField("d").as[GuildRoleCreate])
    case Event("GUILD_ROLE_UPDATE", cursor)           => sendEvent(cursor.downField("d").as[GuildRoleUpdate])
    case Event("GUILD_ROLE_DELETE", cursor)           => sendEvent(cursor.downField("d").as[GuildRoleDelete])
    case Event("MESSAGE_CREATE", cursor)              => sendEvent(cursor.downField("d").as[MessageCreate])
    case Event("MESSAGE_UPDATE", cursor)              => sendEvent(cursor.downField("d").as[MessageUpdate])
    case Event("MESSAGE_DELETE", cursor)              => sendEvent(cursor.downField("d").as[MessageDelete])
    case Event("MESSAGE_DELETE_BULK", cursor)         => sendEvent(cursor.downField("d").as[MessageDeleteBulk])
    case Event("MESSAGE_REACTION_ADD", cursor)        => sendEvent(cursor.downField("d").as[MessageReactionAdd])
    case Event("MESSAGE_REACTION_REMOVE", cursor)     => sendEvent(cursor.downField("d").as[MessageReactionRemove])
    case Event("MESSAGE_REACTION_REMOVE_ALL", cursor) => sendEvent(cursor.downField("d").as[MessageReactionRemoveAll])
    case Event("PRESENCE_UPDATE", cursor)             => sendEvent(cursor.downField("d").as[PresenceUpdate])
    case Event("TYPING_START", cursor)                => sendEvent(cursor.downField("d").as[TypingStart])
    case Event("USER_UPDATE", cursor)                 => sendEvent(cursor.downField("d").as[UserUpdate])
    case Event("VOICE_STATE_UPDATE", cursor)          => sendEvent(cursor.downField("d").as[VoiceStateUpdate])
    case Event("VOICE_SERVER_UPDATE", cursor)         => sendEvent(cursor.downField("d").as[VoiceServerUpdate])
    case Event("WEBHOOKS_UPDATE", cursor)             => sendEvent(cursor.downField("d").as[WebhooksUpdate])
    case Event(_, cursor)                             => sender ! DiscordBot.Event(cursor.value)
  }

  def sendEvent(event: Result[akkord.events.Event]): Unit = {
    println(event)
    sender ! event.getOrElse(akkord.events.AkkordParsingError)
  }
}

object EventParser {
  case class Event(name: String, cursor: HCursor)
}
