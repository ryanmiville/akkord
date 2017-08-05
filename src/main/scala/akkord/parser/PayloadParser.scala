package akkord.parser

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.ws.TextMessage
import akkord.DiscordBot._
import io.circe.parser.parse
import io.circe.{HCursor, Json}

class PayloadParser(bot: ActorRef) extends Actor {
  import PayloadParser._

  val eventParser = context.actorOf(Props(classOf[EventParser]))

  override def receive = {
    case TextMessage.Strict(text) => parseText(text)
    case Payload(0, cursor)       => parseEvent(cursor)
    case Payload(3, _)            => sender ! StatusUpdate
    case Payload(7, _)            => sender ! Reconnect
    case Payload(9, _)            => sender ! InvalidSession
    case Payload(10, cursor)      => parseHello(cursor)
    case Payload(11, _)           => sender ! HeartBeatAck
    case Payload(-1, cursor)      => sender ! UnsupportedMessage(cursor.value.toString())
  }

  private def parseText(text: String): Unit = {
    println(s"received $text")
    val json   = parse(text).getOrElse(Json.Null)
    val cursor = json.hcursor
    val op     = cursor.get[Int]("op").toOption
    self forward Payload(op getOrElse -1, cursor)
  }

  private def parseEvent(cursor: HCursor): Unit = {
    cursor
      .get[Int]("s")
      .toOption
      .foreach(s => bot ! NewSeq(s))
    cursor
      .get[String]("t")
      .toOption
      .foreach(t => eventParser forward EventParser.Event(t, cursor))
  }

  private def parseHello(cursor: HCursor): Unit = {
    cursor
      .downField("d")
      .get[Int]("heartbeat_interval")
      .toOption
      .foreach(interval => sender ! Hello(interval))
  }
}

object PayloadParser {
  case class Payload(op: Int, cursor: HCursor)
}
