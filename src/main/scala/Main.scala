import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    val actorRef = system.actorOf(Props(classOf[DiscordClient], materializer))
    system.scheduler.scheduleOnce(5 seconds) {
      actorRef ! DiscordClient.Disconnect
      system.terminate()
    }
  }
}