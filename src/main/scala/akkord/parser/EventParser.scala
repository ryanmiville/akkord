package akkord.parser

import akka.actor.Actor
import akkord.DiscordBot
import akkord.DiscordBot._
import akkord.events._
import io.circe.Decoder.Result
import io.circe.HCursor

class EventParser extends Actor {
  import EventParser._
  import io.circe.generic.auto._

  override def receive: Receive = {
    case Event("READY", cursor)                       => parseReady(cursor)
    case Event("MESSAGE_CREATE", cursor)              => sendEvent(cursor.downField("d").as[MessageCreate])
    case Event("MESSAGE_UPDATE", cursor)              => sendEvent(cursor.downField("d").as[MessageUpdate])
    case Event("MESSAGE_DELETE", cursor)              => sendEvent(cursor.downField("d").as[MessageDelete])
    case Event("MESSAGE_DELETE_BULK", cursor)         => sendEvent(cursor.downField("d").as[MessageDeleteBulk])
    case Event("MESSAGE_REACTION_ADD", cursor)        => sendEvent(cursor.downField("d").as[MessageReactionAdd])
    case Event("MESSAGE_REACTION_REMOVE", cursor)     => sendEvent(cursor.downField("d").as[MessageReactionRemove])
    case Event("MESSAGE_REACTION_REMOVE_ALL", cursor) => sendEvent(cursor.downField("d").as[MessageReactionRemoveAll])
    case Event(_, cursor)                             => sender ! DiscordBot.Event(cursor.value)
  }

  private def parseReady(cursor: HCursor): Unit = {
    cursor
      .downField("d")
      .get[String]("session_id")
      .toOption
      .foreach(id => sender ! Ready(id))
  }

  def sendEvent(event: Result[akkord.events.Event]): Unit = {
    println(event)
    event.foreach(e => sender ! e)
  }
}

object EventParser {
  case class Event(name: String, cursor: HCursor)
}
