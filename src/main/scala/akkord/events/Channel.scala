package akkord.events

trait Channel {
  val id: String
  val `type`: String
  val guild_id: Option[String]
  val position: Option[Int]
  val permission_overwrites: Option[List[Overwrite]]
  val name: Option[String]
  val topic: Option[String]
  val last_message_id: Option[String]
  val bitrate: Option[Int]
  val user_limit: Option[Int]
  val recipients: Option[List[User]]
  val icon: Option[String]
  val owner_id: Option[String]
  val application_id: Option[String]
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
) extends Channel