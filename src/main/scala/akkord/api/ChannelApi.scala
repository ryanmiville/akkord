package akkord.api

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpEntity
import akka.pattern.ask
import akkord.api.ChannelApiActor.{AddPinnedChannelMessage, BulkDeleteMessages, CreateMessage, CreateReaction, DeleteAllReactions, DeleteChannel, DeleteMessage, DeletePinnedChannelMessage, EditMessage, MessagePayload, ModifyTextChannel, ModifyTextChannelPayload, ModifyVoiceChannel, ModifyVoiceChannelPayload}
import akkord.api.DiscordApiActor.{RateLimited, RateLimitedException}

import scala.concurrent.Future

class ChannelApi(token: String)(implicit system: ActorSystem) {
  private val channel = system.actorOf(ChannelApiActor.props(token))

  def createMessage(channelId: String, content: String) = {
    val response = (channel ? new CreateMessage(channelId, content)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def editMessage(channelId: String, messageId: String, content: String) = {
    val response = (channel ? new EditMessage(channelId, messageId, content)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def deleteMessage(channelId: String, messageId: String) = {
    val response = (channel ? DeleteMessage(channelId, messageId)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def bulkDeleteMessages(channelId: String, messageIds: List[String]) = {
    val response = (channel ? new BulkDeleteMessages(channelId, messageIds)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def modifyTextChannel(channelId: String, payload: ModifyTextChannelPayload) = {
    val response = (channel ? ModifyTextChannel(channelId, payload)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def modifyVoiceChannel(channelId: String, payload: ModifyVoiceChannelPayload) = {
    val response = (channel ? ModifyVoiceChannel(channelId, payload)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def deleteChannel(channelId: String) = {
    val response = (channel ? DeleteChannel(channelId)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def createReaction(channelId: String, messageId: String, reaction: String) = {
    val response = (channel ? CreateReaction(channelId, messageId, reaction)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def deleteAllReactions(channelId: String, messageId: String) = {
    val response = (channel ? DeleteAllReactions(channelId, messageId)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def addPinnedChannelMessage(channelId: String, messageId: String) = {
    val response = (channel ? AddPinnedChannelMessage(channelId, messageId)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  def deletePinnedChannelMessage(channelId: String, messageId: String) = {
    val response = (channel ? DeletePinnedChannelMessage(channelId, messageId)).mapTo[Either[RateLimited, HttpEntity]]
    unwrapResponse(channelId, response)
  }

  private def unwrapResponse(
      channelId: String,
      response: Future[Either[RateLimited, HttpEntity]]) = response collect {
    case Right(e: HttpEntity) => e
    case Left(_) => throw RateLimitedException(s"endpoint for channel ID: $channelId is being rate limited")
  }
}
