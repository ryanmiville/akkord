package akkord.api

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import akkord.api.DiscordApi.EmptyResponse
import akkord.api.actors.ChannelApiActor
import akkord.api.actors.ChannelApiActor._
import akkord.api.actors.DiscordApiActor.RateLimited
import akkord.events.Event._
import akkord.events.{Channel, Message}
import io.circe.Json
import io.circe.generic.auto._
import play.api.libs.ws.StandaloneWSResponse

import scala.concurrent.{ExecutionContext, Future}

class ChannelApi(
    token: String)(
    implicit system: ActorSystem,
    mat: Materializer,
    timeout: Timeout,
    ec: ExecutionContext)
  extends DiscordApi {
  private val channel = system.actorOf(ChannelApiActor.props(token))

  def createMessage(channelId: String, content: String): Future[Message] = {
    val response = (channel ? new CreateMessage(channelId, content)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      resp.body[Json].as[MessageCreate] match {
        case Right(messageCreate)  => messageCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def editMessage(channelId: String, messageId: String, content: String): Future[Message] = {
    val response = (channel ? new EditMessage(channelId, messageId, content)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      resp.body[Json].as[MessageCreate] match {
        case Right(messageCreate)  => messageCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def deleteMessage(channelId: String, messageId: String): Future[EmptyResponse] = {
    val response = (channel ? DeleteMessage(channelId, messageId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      EmptyResponse()
    }
  }

  def bulkDeleteMessages(channelId: String, messageIds: List[String]) = {
    val response = (channel ? new BulkDeleteMessages(channelId, messageIds)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      EmptyResponse()
    }
  }

  def modifyTextChannel(channelId: String, payload: ModifyTextChannelPayload): Future[Channel] = {
    val response = (channel ? ModifyTextChannel(channelId, payload)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      resp.body[Json].as[ChannelCreate] match {
        case Right(channelCreate)        => channelCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def modifyVoiceChannel(channelId: String, payload: ModifyVoiceChannelPayload): Future[Channel] = {
    val response = (channel ? ModifyVoiceChannel(channelId, payload)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      resp.body[Json].as[ChannelCreate] match {
        case Right(channelCreate)        => channelCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def deleteChannel(channelId: String): Future[Channel] = {
    val response = (channel ? DeleteChannel(channelId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      resp.body[Json].as[ChannelCreate] match {
        case Right(channelCreate)        => channelCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def createReaction(channelId: String, messageId: String, reaction: String): Future[EmptyResponse] = {
    val response = (channel ? CreateReaction(channelId, messageId, reaction)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      EmptyResponse()
    }
  }

  def deleteAllReactions(channelId: String, messageId: String): Future[EmptyResponse] = {
    val response = (channel ? DeleteAllReactions(channelId, messageId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      EmptyResponse()
    }
  }

  def addPinnedChannelMessage(channelId: String, messageId: String): Future[EmptyResponse] = {
    val response = (channel ? AddPinnedChannelMessage(channelId, messageId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      EmptyResponse()
    }
  }

  def deletePinnedChannelMessage(channelId: String, messageId: String): Future[EmptyResponse] = {
    val response = (channel ? DeletePinnedChannelMessage(channelId, messageId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      EmptyResponse()
    }
  }
}
