package akkord.events

trait VoiceState {
  val channel_id: Option[String]
  val user_id: String
  val session_id: String
  val guild_id: Option[String]
  val deaf: Boolean
  val mute: Boolean
  val self_deaf: Boolean
  val self_mute: Boolean
  val suppress: Boolean
}
case class VoiceStateImpl
(
  override val channel_id: Option[String],
  override val user_id: String,
  override val session_id: String,
  override val guild_id: Option[String],
  override val deaf: Boolean,
  override val mute: Boolean,
  override val self_deaf: Boolean,
  override val self_mute: Boolean,
  override val suppress: Boolean
) extends VoiceState
