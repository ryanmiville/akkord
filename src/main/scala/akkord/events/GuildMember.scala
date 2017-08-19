package akkord.events

trait GuildMember {
  val user: UserImpl
  val nick: Option[String]
  val roles: List[String]
  val joined_at: String
  val deaf: Boolean
  val mute: Boolean
}

case class GuildMemberImpl
(
  user: UserImpl,
  nick: Option[String],
  roles: List[String],
  joined_at: String,
  deaf: Boolean,
  mute: Boolean
) extends GuildMember
