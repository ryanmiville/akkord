package akkord.api

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import akka.util.Timeout
import akkord.api.DiscordApi.EmptyResponse
import akkord.api.actors.ChannelApiActor
import akkord.api.actors.ChannelApiActor._
import akkord.events.Event._
import akkord.events.{Channel, ChannelImpl, Message}
import io.circe.Json
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class ChannelApi(
    token: String)(
    implicit system: ActorSystem,
    mat: Materializer,
    timeout: Timeout,
    ec: ExecutionContext)
  extends DiscordApi {
  override protected val api: ActorRef = system.actorOf(ChannelApiActor.props(token))

  def getChannel(channelId: String): Future[Channel] = {
    getApiResponse(GetChannel(channelId)) map { resp =>
      resp.body[Json].as[ChannelImpl] match {
        case Right(channelImpl)    => channelImpl
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def getChannelMessages(channelId: String): Future[Seq[Message]] = {
    getApiResponse(GetChannelMessages(channelId)) map { resp =>
      resp.body[Json].as[List[MessageCreate]] match {
        case Right(messages)       => messages
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def getChannelMessage(channelId: String, messageId: String): Future[Message] = {
    getApiResponse(GetChannelMessage(channelId, messageId)) map { resp =>
      resp.body[Json].as[MessageCreate] match {
        case Right(messageCreate)  => messageCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def createMessage(channelId: String, content: String): Future[Message] = {
    getApiResponse(new CreateMessage(channelId, content)) map { resp =>
      resp.body[Json].as[MessageCreate] match {
        case Right(messageCreate)  => messageCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def editMessage(channelId: String, messageId: String, content: String): Future[Message] = {
    getApiResponse(new EditMessage(channelId, messageId, content)) map { resp =>
      resp.body[Json].as[MessageCreate] match {
        case Right(messageCreate)  => messageCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def deleteMessage(channelId: String, messageId: String): Future[EmptyResponse] = {
    getApiResponse(DeleteMessage(channelId, messageId)) map { _ =>
      EmptyResponse()
    }
  }

  def bulkDeleteMessages(channelId: String, messageIds: List[String]): Future[EmptyResponse] = {
    getApiResponse(new BulkDeleteMessages(channelId, messageIds)) map { _ =>
      EmptyResponse()
    }
  }

  def modifyTextChannel(channelId: String, payload: ModifyTextChannelPayload): Future[Channel] = {
    getApiResponse(ModifyTextChannel(channelId, payload)) map { resp =>
      resp.body[Json].as[ChannelCreate] match {
        case Right(channelCreate)  => channelCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def modifyVoiceChannel(channelId: String, payload: ModifyVoiceChannelPayload): Future[Channel] = {
    getApiResponse(ModifyVoiceChannel(channelId, payload)) map { resp =>
      resp.body[Json].as[ChannelCreate] match {
        case Right(channelCreate)  => channelCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def deleteChannel(channelId: String): Future[Channel] = {
    getApiResponse(DeleteChannel(channelId)) map { resp =>
      resp.body[Json].as[ChannelCreate] match {
        case Right(channelCreate)  => channelCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def createReaction(channelId: String, messageId: String, reaction: String): Future[EmptyResponse] = {
    getApiResponse(CreateReaction(channelId, messageId, reaction)) map { _ =>
      EmptyResponse()
    }
  }

  def deleteAllReactions(channelId: String, messageId: String): Future[EmptyResponse] = {
    getApiResponse(DeleteAllReactions(channelId, messageId)) map { _ =>
      EmptyResponse()
    }
  }

  def getPinnedMessages(channelId: String): Future[Seq[Message]] = {
    getApiResponse(GetPinnedMessages(channelId)) map { resp =>
      resp.body[Json].as[List[MessageCreate]] match {
        case Right(messageCreates)  => messageCreates
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def addPinnedChannelMessage(channelId: String, messageId: String): Future[EmptyResponse] = {
    getApiResponse(AddPinnedChannelMessage(channelId, messageId)) map { _ =>
      EmptyResponse()
    }
  }

  def deletePinnedChannelMessage(channelId: String, messageId: String): Future[EmptyResponse] = {
    getApiResponse(DeletePinnedChannelMessage(channelId, messageId)) map { _ =>
      EmptyResponse()
    }
  }
}
