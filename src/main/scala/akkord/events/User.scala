package akkord.events

case class User
(
  id: String,
  username: String,
  discriminator: String,
  avatar: Option[String],
  bot: Option[Boolean],
  mfa_enabled: Option[Boolean],
  verified: Option[Boolean],
  email: Option[String]
)
