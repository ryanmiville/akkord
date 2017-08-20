package akkord.events

trait User
{
  val id: String
  val username: Option[String]
  val discriminator: Option[String]
  val avatar: Option[String]
  val bot: Option[Boolean]
  val mfa_enabled: Option[Boolean]
  val verified: Option[Boolean]
  val email: Option[String]
}

case class UserImpl
(
  id: String,
  username: Option[String],
  discriminator: Option[String],
  avatar: Option[String],
  bot: Option[Boolean],
  mfa_enabled: Option[Boolean],
  verified: Option[Boolean],
  email: Option[String]
) extends User
