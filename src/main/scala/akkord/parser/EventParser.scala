package akkord.parser

import akka.actor.Actor
import akkord.DiscordBot
import akkord.DiscordBot._
import io.circe.HCursor

class EventParser extends Actor {
  import EventParser._

  override def receive = {
    case Event("READY", cursor)          => parseReady(cursor)
    case Event("MESSAGE_CREATE", cursor) => parseMessageCreated(cursor)
    case Event(_, cursor)                => sender ! DiscordBot.Event(cursor.value)
  }

  private def parseReady(cursor: HCursor): Unit = {
    cursor
      .downField("d")
      .get[String]("session_id")
      .toOption
      .foreach(id => sender ! Ready(id))
  }

  private def parseMessageCreated(cursor: HCursor): Unit = {
    val d = cursor.downField("d")
    if(isNonUserMessage)
      sender ! NonUserMessageCreated
    else
      parseUserMessageCreated.foreach(msg => sender ! msg)

    def isNonUserMessage: Boolean = {
      val isWebhook = cursor.get[String]("webhook_id").toOption.isDefined
      val isBot     = d.downField("author").get[Boolean]("bot").getOrElse(false)
      isBot || isWebhook
    }

    def parseUserMessageCreated: Option[MessageCreated] = {
      for {
        content   <- d.get[String]("content").toOption
        channelId <- d.get[String]("channel_id").toOption
      } yield MessageCreated(channelId, (content split " ").toList)
    }
  }
}

object EventParser {
  case class Event(name: String, cursor: HCursor)
}
