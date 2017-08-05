import DiscordBot.{MessageCreated, NonUserMessageCreated, Ready}
import EventParser.Event
import akka.actor.Actor
import io.circe.HCursor

class EventParser extends Actor {
  override def receive = {
    case Event("READY", cursor)          => parseReady(cursor)
    case Event("MESSAGE_CREATE", cursor) => parseMessageCreated(cursor)
    case Event(_, cursor)                => sender ! DiscordBot.Event(cursor.value)
  }

  private def parseReady(cursor: HCursor) = {
    cursor
      .downField("d")
      .get[String]("session_id")
      .toOption
      .foreach(id => sender ! Ready(id))
  }

  private def parseMessageCreated(cursor: HCursor) = {
    val d = cursor.downField("d")
    if(isNonUserMessage) sender ! NonUserMessageCreated
    else parseUserMessageCreated.foreach(msg => sender ! msg)

    def isNonUserMessage: Boolean = {
      val isWebhook = cursor.get[String]("webhook_id").toOption.isDefined
      val isBot = d.downField("author").get[Boolean]("bot").getOrElse(false)
      isBot || isWebhook
    }

    def parseUserMessageCreated: Option[MessageCreated] = {
      for {
        content <- d.get[String]("content").toOption
        channelId <- d.get[String]("channel_id").toOption
      } yield MessageCreated(channelId, (content split " "): _*)
    }
  }
}

object EventParser {
  case class Event(name: String, cursor: HCursor)
}
