import akkord.DiscordBot.Disconnect
import akka.actor.{ActorSystem, Props}

import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    val token = "MzAxNDMwMzcxNTI0OTM1Njky.DF_bEw.KzXWwb7fCunVfheg6SVtWdKg6ME"
    val system = ActorSystem()
    implicit val executionContext = system.dispatcher
    val botRef = system.actorOf(Props(classOf[Bot], token))
    system.scheduler.scheduleOnce(1 minutes) {
      botRef ! Disconnect
      system.terminate()
    }
  }
}