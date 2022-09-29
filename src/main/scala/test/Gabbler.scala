package test

import akka.actor.typed._
import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}

object Gabbler {
  import ChatRoom._

  def apply( screenName: String): Behavior[ClientCommand] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Connected(handle) =>
          handle ! PublishClientMessage(screenName,"Hello World!")
          Behaviors.same
        case MessagePosted(screenName, message) =>
          context.log.info2("message has been posted by '{}': {}", screenName, message)
          Behaviors.stopped
      }
    }
}