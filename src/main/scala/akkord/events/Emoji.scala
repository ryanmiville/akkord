package akkord.events

case class Emoji
(
  id: Option[String],
  name: String,
  roles: Option[List[String]],
  require_colons: Option[Boolean],
  managed: Option[Boolean]
)
