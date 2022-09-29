package test

import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ChatRoom {
  //#chatroom-behavior
  sealed trait RoomCommand
  final case class Connect(replyTo: ActorRef[ClientCommand]) extends RoomCommand
  //#chatroom-protocol
  //#chatroom-behavior
  final case class PublishClientMessage(screenName: String, message: String) extends RoomCommand
  //#chatroom-behavior
  //#chatroom-protocol

  sealed trait ClientCommand
  final case class Connected(handle: ActorRef[RoomCommand]) extends ClientCommand
  final case class MessagePosted(screenName: String, message: String) extends ClientCommand


  //#chatroom-protocol
  //#chatroom-behavior

  def apply(): Behavior[RoomCommand] =
    chatRoom(List.empty)

  private def chatRoom(sessions: List[ActorRef[ClientCommand]]): Behavior[RoomCommand] =
    Behaviors.receive { (context, message) =>
      message match {
        case Connect( client) =>
          client ! Connected(context.self)
          chatRoom(client :: sessions)
        case PublishClientMessage(screenName, message) =>
          sessions.foreach(_ ! MessagePosted(screenName, message))
          Behaviors.same
      }
    }
}