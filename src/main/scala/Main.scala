import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Future

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()

    val numberSource: Source[Int, NotUsed] = Source(1 to 1000)

    //Only let pass even numbers through the Flow
    val isEvenFlow: Flow[Int, Int, NotUsed] = Flow[Int].filter((num) => num % 2 == 0)

    //Create a Source of even random numbers by combining the random number Source with the even number filter Flow
    val evenNumbersSource: Source[Int, NotUsed] = numberSource.via(isEvenFlow)

    //A Sink that will write its input onto the console
    val consoleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)

    //Connect the Source with the Sink and run it using the materializer
    evenNumbersSource.runWith(consoleSink)
  }
}