package akkord.events

case class Role
(
  id: String,
  name: String,
  color: Int,
  hoist: Boolean,
  position: Int,
  permissions: Int,
  managed: Boolean,
  mentionable: Boolean
)
