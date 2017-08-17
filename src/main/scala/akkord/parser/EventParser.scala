package akkord.parser

import akka.actor.Actor
import akkord.DiscordBot
import akkord.DiscordBot._
import akkord.events.{Message, MessageCreate, MessageUpdate}
import io.circe.Decoder.Result
import io.circe.HCursor

class EventParser extends Actor {
  import EventParser._

  override def receive = {
    case Event("READY", cursor)          => parseReady(cursor)
    case Event("MESSAGE_CREATE", cursor) => parseMessageCreate(cursor)
    case Event("MESSAGE_UPDATE", cursor) => parseMessageUpdate(cursor)
    case Event(_, cursor)                => sender ! DiscordBot.Event(cursor.value)
  }

  private def parseReady(cursor: HCursor): Unit = {
    cursor
      .downField("d")
      .get[String]("session_id")
      .toOption
      .foreach(id => sender ! Ready(id))
  }

  private def parseMessageCreate(cursor: HCursor): Unit = {
    parseMessage(cursor).foreach(msg => sender ! MessageCreate(msg))
  }

  private def parseMessageUpdate(cursor: HCursor): Unit = {
    parseMessage(cursor).foreach(msg => sender ! MessageUpdate(msg))
  }


  def parseMessage(cursor: HCursor): Result[Message] = {
    import io.circe.generic.auto._
    val msg = cursor.downField("d").as[Message]
    println(msg)
    msg
  }
}

object EventParser {
  case class Event(name: String, cursor: HCursor)
}
