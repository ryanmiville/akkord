package akkord.events

case class Game
(
  name: String,
  `type`: Int,
  url: Option[String]
)
