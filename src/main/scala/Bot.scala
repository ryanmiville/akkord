import akka.actor.{Actor, Props}

class Bot(token: String) extends Actor {
  import Bot._

  implicit private val system = context.system
  private val messenger = system.actorOf(Props(classOf[Messenger], token))

  override def receive = {
    case Msg(id, "ping") =>
      messenger ! Messenger.Message(id, "pong")
  }
}

object Bot {
  case class Msg(channelId: String, content: String*)
}
