package akkord

import akkord.DiscordBot.GatewayPayload

object Event {
  trait Event

  case class Message(
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
    `type`: Int
  ) extends Event with GatewayPayload {
    def reply(replyContent: String) = SendMessage(channel_id, replyContent)
  }

  case class User(
    id: String,
    username: String,
    discriminator: String,
    avatar: String,
    bot: Option[Boolean],
    mfa_enabled: Option[String],
    verified: Option[Boolean],
    email: Option[String]
  )

  case class Attachment(
    id: String,
    filename: String,
    size: Int,
    url: String,
    proxy_url: String,
    height: Option[Int],
    width: Option[Int]
  )

  case class Embed(
    title: String,
    `type`: String,
    description: Option[String],
    url: String,
    timestamp: Option[String],
    color: Option[Int],
    footer: Option[Footer],
    image: Option[Image],
    thumbnail: Option[Thumbnail],
    video: Option[Video],
    provider: Option[Provider],
    author: Option[Author],
    fields: Option[List[Field]]
  )

  case class Thumbnail(
    url: String,
    proxy_url: String,
    height: Int,
    width: Int
  )

  case class Video(
    url: String,
    height: Int,
    width: Int
  )

  case class Image(
    url: String,
    proxy_url: String,
    height: Int,
    width: Int
  )

  case class Provider(
    name: String,
    url: String
  )

  case class Author(
    name: String,
    url: String,
    icon_url: Option[String],
    proxy_icon_url: Option[String]
  )

  case class Footer(
    text: String,
    icon_url: String,
    proxy_icon_url: String
  )

  case class Field(
    name: String,
    value: String,
    inline: Boolean
  )

  case class Reaction(
    count: Int,
    me: Boolean,
    emoji: Emoji
  )

  case class Emoji(
    id: String,
    name: String,
    roles: Option[List[String]],
    require_colons: Option[Boolean],
    managed: Option[Boolean]
  )

  case class SendMessage(channelId: String, content: String)
}
