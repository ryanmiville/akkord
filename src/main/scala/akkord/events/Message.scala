package akkord.events

import akkord.api.ChannelApi.SendMessage

trait Message {
  val id: String
  val channel_id: String
  val author: UserImpl
  val content: String
  val timestamp: String
  val edited_timestamp: Option[String]
  val tts: Boolean
  val mention_everyone: Boolean
  val mentions: List[UserImpl]
  val mention_roles: List[String]
  val attachments: List[Attachment]
  val embeds: List[Embed]
  val reactions: Option[List[Reaction]]
  val nonce: Option[String]
  val pinned: Boolean
  val webhook: Option[String]
  val `type`: Int

  def reply(replyContent: String) = new SendMessage(channel_id, replyContent)
}

case class Attachment
(
  id: String,
  filename: String,
  size: Int,
  url: String,
  proxy_url: String,
  height: Option[Int],
  width: Option[Int]
)

case class Embed
(
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

case class Thumbnail
(
  url: String,
  proxy_url: String,
  height: Int,
  width: Int
)

case class Video
(
  url: String,
  height: Int,
  width: Int
)

case class Image
(
  url: String,
  proxy_url: String,
  height: Int,
  width: Int
)

case class Provider
(
  name: String,
  url: String
)

case class Author
(
  name: String,
  url: String,
  icon_url: Option[String],
  proxy_icon_url: Option[String]
)

case class Footer
(
  text: String,
  icon_url: String,
  proxy_icon_url: String
)

case class Field
(
  name: String,
  value: String,
  inline: Boolean
)

case class Reaction
(
  count: Int,
  me: Boolean,
  emoji: Emoji
)
