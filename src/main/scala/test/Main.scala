package test
import akka.NotUsed
import akka.actor.typed._
import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}

object Main {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      val chatRoom = context.spawn(ChatRoom(), "chatroom")
      val gabblerRef = context.spawn(Gabbler("ol’ Gabbler"), "gabbler")
      context.watch(gabblerRef)
      chatRoom ! ChatRoom.Connect(gabblerRef)

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }

  def main(args: Array[String]): Unit = {
    ActorSystem(Main(), "ChatRoomDemo")
  }

}