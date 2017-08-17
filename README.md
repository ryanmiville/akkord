# Akkord
Akkord's goal is to make writing a bot as easy as writing an Akka Actor, while handling all of the necessary connection infrastructure and API rules (rate limiting, etc.) for you.

Akkord is still very much in the early stages of development.

# Installation
```scala
resolvers += Resolver.bintrayRepo("ryanmiville", "maven")

libraryDependencies += "com.github.ryanmiville" %% "akkord" % "0.1"
```

# Usage
Your bot's behavior is described as a partial function that takes an implementation of the `Event` trait. These classes correspond to the events found [here](https://discordapp.com/developers/docs/topics/gateway#events)
```scala
class Bot(token: String) extends DiscordBot(token) {

  val channel = system.actorOf(ChannelApi.props(token))

  override def botBehavior: ReceiveEvent = {
    case msg: Message if msg.content == "ping" =>
      channel ! ChannelApi.Message(msg.channel_id, "pong")
    case msg: Message if msg.content.startsWith("greet ") =>
      val who = msg.content.split(" ", 2)(1)
      channel ! ChannelApi.Message(msg.channel_id, s"Hello $who!")
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val token = args(0)
    ActorSystem().actorOf(Props(classOf[Bot], token))
  }
}
```

If you wish to create a straight-forward bot that only replies to a message, the `SimpleDiscordBot` class can be extended for a more streamlined API for this simple case. You will describe your bot's behavior with a partial function that takes the message content as a list of the words in the message, and return your reply as a String
```scala
class SimpleBot(token: String) extends DiscordBot(token) {

  override def onMessage: MessageReply = {
    case "ping" :: Nil => "pong"
    case "greet" :: who => ("Hello" :: who).mkString(" ") + "!"
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val token = args(0)
    ActorSystem().actorOf(Props(classOf[SimpleBot], token))
  }
}
```

Both of the above examples will create the following bot:
![Screenshot](https://user-images.githubusercontent.com/2359050/28999933-e2e703f6-7a28-11e7-8e92-11445b1ce8f4.png)

# Discord APIs
You interact with the Discord APIs via Actors. Each API resource has a corresponding actor... eventually :)

Please visit the [developer documentation](https://discordapp.com/developers/docs/intro) to learn what's capable and to get a good idea of what to expect from Akkord.
