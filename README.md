# Akkord
Akkord's goal is to make writing a bot as easy as writing an Akka Actor, while handling all of the necessary connection infrastructure and API rules (rate limiting, etc.) for you.

Akkord is still very much in the early stages of development.

# Installation
```scala
resolvers += Resolver.bintrayRepo("ryanmiville", "maven")

libraryDependencies += "com.github.ryanmiville" %% "akkord" % "0.1"
```

# Usage
Your bot is simply a specialized actor and should feel very familiar to anyone familiar with Akka.
```scala
class Bot(token: String) extends DiscordBot(token) {

  val channel = system.actorOf(ChannelApi.props(token))

  override def botBehavior: Receive = {
    case MessageCreated(id, "ping" :: Nil) =>
      channel ! Message(id, "pong")
      sender ! Ack
    case MessageCreated(id, "greet" :: who) =>
      val greeting = ("Hello" :: who).mkString(" ") + "!"
      channel ! Message(id, greeting)
      sender ! Ack
  }
}
```
![image](https://user-images.githubusercontent.com/2359050/28999933-e2e703f6-7a28-11e7-8e92-11445b1ce8f4.png)

**IMPORTANT NOTE:** Your bot _MUST_ rely with `Ack` to `sender` at the end of each case to provide a back-pressure signal to the underlying stream that represents your connection with Discord's servers. Without this signal, the stream will stop sending messages to your bot because it thinks it is being overwhelmed.

# Discord APIs
You interact with the Discord APIs via Actors. Each API resource has a corresponding actor... eventually :)

Please visit the [developer documentation](https://discordapp.com/developers/docs/intro) to learn what's capable and to get a good idea of what to expect from Akkord.
