import akka.actor.{ActorSystem, Props}

import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    val token = "MzAxNDMwMzcxNTI0OTM1Njky.DF_bEw.KzXWwb7fCunVfheg6SVtWdKg6ME"
    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher
    val actorRef = system.actorOf(Props(classOf[DiscordClient], token))
    system.scheduler.scheduleOnce(2 minutes) {
      actorRef ! DiscordClient.Disconnect
      system.terminate()
    }
  }
}