package akkord.api

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import akkord.api.actors.ChannelApiActor
import akkord.api.actors.ChannelApiActor._
import akkord.api.actors.DiscordApiActor.RateLimited
import akkord.events.Event._
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

  def createMessage(channelId: String, content: String): Future[MessageCreate] = {
    val response = (channel ? new CreateMessage(channelId, content)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response) map { resp =>
      resp.body[Json].as[MessageCreate] match {
        case Right(messageCreate)  => messageCreate
        case Left(decodingFailure) => throw decodingFailure
      }
    }
  }

  def editMessage(channelId: String, messageId: String, content: String) = {
    val response = (channel ? new EditMessage(channelId, messageId, content)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }

  def deleteMessage(channelId: String, messageId: String) = {
    val response = (channel ? DeleteMessage(channelId, messageId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }

  def bulkDeleteMessages(channelId: String, messageIds: List[String]) = {
    val response = (channel ? new BulkDeleteMessages(channelId, messageIds)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }

  def modifyTextChannel(channelId: String, payload: ModifyTextChannelPayload) = {
    val response = (channel ? ModifyTextChannel(channelId, payload)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }

  def modifyVoiceChannel(channelId: String, payload: ModifyVoiceChannelPayload) = {
    val response = (channel ? ModifyVoiceChannel(channelId, payload)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }

  def deleteChannel(channelId: String) = {
    val response = (channel ? DeleteChannel(channelId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }

  def createReaction(channelId: String, messageId: String, reaction: String) = {
    val response = (channel ? CreateReaction(channelId, messageId, reaction)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }

  def deleteAllReactions(channelId: String, messageId: String) = {
    val response = (channel ? DeleteAllReactions(channelId, messageId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }

  def addPinnedChannelMessage(channelId: String, messageId: String) = {
    val response = (channel ? AddPinnedChannelMessage(channelId, messageId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }

  def deletePinnedChannelMessage(channelId: String, messageId: String) = {
    val response = (channel ? DeletePinnedChannelMessage(channelId, messageId)).mapTo[Either[RateLimited, StandaloneWSResponse]]
    unwrapResponse(channelId, response)
  }
}
