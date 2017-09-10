# Akkord
Akkord's goal is to make writing a bot as easy and idiomatic in Scala as possible, while handling all of the necessary connection infrastructure and API rules (rate limiting, etc.) for you.

As the name suggests, Akkord uses Akka heavily to make your bot as non-blocking and concurrent as possible. The primary API hides the underlying actor model from you though, so knowing Akka is not a prerequisite to create bots with Akkord. 

However, an actor can feel like a natural model for a chat bot for anyone that is familiar with Akka. If you do wish to write your bot as an Akka actor, there will be examples in the future showing how to accomplish this.

Akkord is still very much in the early stages of development.

# Installation
```scala
resolvers += Resolver.bintrayRepo("ryanmiville", "maven")

libraryDependencies += "com.github.ryanmiville" %% "akkord-experimental" % "0.1"
```

# Usage
Your bot can react to any implementation of the `Event` trait. These classes correspond to the events found [here](https://discordapp.com/developers/docs/topics/gateway#events).

The `DiscordBot` abstract class has a corresponding callback method that you can override for each `Event` (i.e. `onMessageCreate`, `onGuildMemberAdd`, etc.). Each callback method is a `PartialFunction[Event, Unit]` where `Event` is the particular case class for that callback. This way, you simply pattern match on the triggers for your bot.
```scala
class Bot(token: String) extends DiscordBot(token) {
  val channel = new ChannelApi(token)

  override def onMessageCreate: PartialFunction[MessageCreate, Unit] = {
    case m: MessageCreate if m.content == "ping" =>
      channel.createMessage(m.channel_id, "pong")
    case m: MessageCreate if m.content.startsWith("greet ") =>
      val who = msg.content.split(" ", 2)(1)
      channel.createMessage(m.channel_id, s"Hello $who!")
  }
}
```
Since reacting to the contents of a message is such a common use case, `DiscordBot` also has a special `onMessageContent` callback in which you match on the contents of the message as a list of words. This pattern can prove useful when your bot commands take multiple arguments.
```scala
    override def onMessageContent(message: MessageCreate) = {
      case "ping" :: Nil =>
        channel.createMessage(message.channel_id, "pong")
      case "greet" :: who =>
        channel.createMessage(message.channel_id, s"Hello ${who.mkString(" ")}!")
    }
```
In many cases, extending `DiscordBot` and overriding `onMessageContent` will be all you need for your bot.

Since your bot is an actor underneath, currently to start your bot you would just start it like a normal Akka actor.
```scala
object Main {
  def main(args: Array[String]): Unit = {
    val token = "your_bot_token_here"
    ActorSystem().actorOf(Props(classOf[Bot], token))
  }
}
```
Both of the above examples will create the following bot:
![Screenshot](https://user-images.githubusercontent.com/2359050/28999933-e2e703f6-7a28-11e7-8e92-11445b1ce8f4.png)

# Discord APIs
Currently, you interact with the Discord APIs via classes that, once again, wrap an underlying actor in order to be as non-blocking as possible. Each API resource will have a corresponding class and actor... eventually :)

Please visit the [developer documentation](https://discordapp.com/developers/docs/intro) to learn what's capable and to get a good idea of what to expect from Akkord.
