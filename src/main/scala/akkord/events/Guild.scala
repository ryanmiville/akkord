package akkord.events

trait Guild {
  val id: String
  val name: String
  val icon: Option[String]
  val splash: Option[String]
  val owner_id: String
  val region: String
  val afk_channel_id: Option[String]
  val afk_timeout: Int
  val embed_enabled: Option[Boolean]
  val embed_channel_id: Option[String]
  val verification_level: Int
  val default_message_notifications: Int
  val explicit_content_filter: Int
  val roles: List[Role]
  val emojis: List[Emoji]
  val features: List[String]
  val mfa_level: Int
  val application_id: Option[String]
  val widget_enabled: Option[Boolean]
  val widget_channel_id: Option[String]
}
