package akkord.events

trait Channel {
  val id: String
  val `type`: Int
  val guild_id: Option[String]
  val position: Option[Int]
  val permission_overwrites: Option[List[Overwrite]]
  val name: Option[String]
  val topic: Option[String]
  val last_message_id: Option[String]
  val bitrate: Option[Int]
  val user_limit: Option[Int]
  val recipients: Option[List[UserImpl]]
  val icon: Option[String]
  val owner_id: Option[String]
  val application_id: Option[String]
  val nsfw: Option[Boolean]
}

case class Overwrite
(
  id: String,
  `type`: String,
  allow: Int,
  deny: Int
)

case class ChannelImpl
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
) extends Channel