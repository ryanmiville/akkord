package akkord.events

trait Presence {
  val user: Option[UserImpl]
  val roles: Option[List[String]]
  val game: Option[Game]
  val guild_id: Option[String]
  val status: Option[String]
}

case class PresenceUpdateImpl
(
  user: Option[UserImpl],
  roles: Option[List[String]],
  game: Option[Game],
  guild_id: Option[String],
  status: Option[String]
) extends Presence

