# Akkord
Akkord's goal is to make writing a bot as easy and idiomatic in Scala as possible, while handling all of the necessary connection infrastructure and API rules (rate limiting, etc.) for you.

As the name suggests, Akkord uses Akka heavily to make your bot as non-blocking and concurrent as possible. You can choose to create your bot as a specialized Akka actor, which can feel like a natural model of a bot for anyone familiar with Akka. If you do not have experience with Akka, or just prefer to not program towards that paradigm, there are straight-forward APIs that hide the underlying actor model.

Akkord is still very much in the early stages of development.

# Installation
```scala
resolvers += Resolver.bintrayRepo("ryanmiville", "maven")

libraryDependencies += "com.github.ryanmiville" %% "akkord-experimental" % "0.1"
```

# Usage
Your bot can react to any implementation of the `Event` trait. These classes correspond to the events found [here](https://discordapp.com/developers/docs/topics/gateway#events).

The `DiscordBot` abstract class has a corresponding callback method that you can override for each `Event` (i.e. `onMessageCreate`, `onGuildMemberAdd`, etc.).
```scala
class Bot(token: String) extends DiscordBot(token) {
  val channel = system.actorOf(ChannelApi.props(token))

  override def onMessageCreate(message: MessageCreate): Unit = {
    message.content match {
      case "ping" =>
        channel ! new CreateMessage(message.channel_id, "pong")
      case c if c.startsWith("greet ") =>
        val who = msg.content.split(" ", 2)(1)
        channel ! new CreateMessage(msg.channel_id, s"Hello $who!")
    }
  }
}
```
Since reacting to the contents of a message is such a common use case, `DiscordBot` also has a special `onMessageContent` callback in which you match on the contents of the message as a list of words. This pattern can prove useful when your bot commands take multiple arguments. (Note we're also using the `reply` convenience method on `MessageCreate` in this example.)
```scala
    override def onMessageContent(message: MessageCreate): ReceiveMessageContent = {
      case "ping" :: Nil =>
        channel ! message.reply("pong")
      case "greet" :: who =>
        channel ! message.reply(s"Hello ${who.mkString(" ")}!")
    }
```
In many cases, extending `DiscordBot` and overriding `onMessageContent` will be all you need for your bot.

As mentioned above, you may also choose to not have the underlying actor model hidden from you. In this case you would extend the `DiscordBotActor` class and override the `botBehavior` method as a normal `receive` method that handles all the `Events` with which your bot is concerned.
```scala
class Bot(token: String) extends DiscordBotActor(token) {
  val channel = system.actorOf(ChannelApi.props(token))

  override def botBehavior: Receive = {
    case msg: MessageCreate if msg.content == "ping" =>
      channel ! msg.reply(pong)
    case msg: MessageCreate if msg.content.startsWith("greet ") =>
      val who = msg.content.split(" ", 2)(1)
      channel ! msg.reply(s"Hello $who!")
  }
}
```

Since both APIs are actors underneath, to start your Bot you would just start it like a normal Akka actor.
```scala
object Main {
  def main(args: Array[String]): Unit = {
    val token = args(0)
    ActorSystem().actorOf(Props(classOf[Bot], token))
  }
}
```
All of the above examples will create the following bot:
![Screenshot](https://user-images.githubusercontent.com/2359050/28999933-e2e703f6-7a28-11e7-8e92-11445b1ce8f4.png)

# Discord APIs
Currently, you interact with the Discord APIs via actors, as seen with the channel resource in the previous examples. Each API resource will have a corresponding actor... eventually :)

There are plans to provide APIs that abstract the actor model for these as well.

Please visit the [developer documentation](https://discordapp.com/developers/docs/intro) to learn what's capable and to get a good idea of what to expect from Akkord.
