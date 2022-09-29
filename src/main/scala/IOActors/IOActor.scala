package IOActors

//import AkkaStream.main.system
import akka.NotUsed
import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.kafka.scaladsl.Producer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import akka.stream.typed.scaladsl.ActorSource
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt

object IODeviceActor {

  sealed trait IOCommand

  object Tick extends IOCommand

  sealed trait NetworkCommand

  case class IOEvent(device: String, eventTime: LocalDateTime) extends NetworkCommand

  case class Delay(iotEvent: IOEvent) extends NetworkCommand

  def apply(Device: String, ReplyTo: ActorRef[NetworkCommand]): Behavior[IOCommand] =
    Behaviors.withTimers[IOCommand] { timers =>
      timers.startTimerAtFixedRate(Tick, 1.second)
      Behaviors.receiveMessage {
        case Tick =>
          ReplyTo ! IOEvent(Device, LocalDateTime.now())
          Behaviors.same
      }
    }
}

object Network {

  import IODeviceActor._

  val rand = new java.util.Random()

  def apply(replyTo: ActorRef[IOEvent]) =
    network(replyTo)

  private def network(replyTo: ActorRef[IOEvent]): Behavior[NetworkCommand] = {
    Behaviors.withTimers[NetworkCommand] { timers =>
      Behaviors.receiveMessage {
        case e: IOEvent =>
          timers.startSingleTimer(Delay(e), (rand.nextInt(10) / 10).second)
          Behaviors.same
        case Delay(ioEvent) =>
          replyTo ! ioEvent
          Behaviors.same
      }
    }

  }
}

object Main {

  import IODeviceActor._

  //val Complete = IOEvent("", LocalDateTime.MIN)

  def apply(): Behavior[NotUsed] = {



    val source: Source[IOEvent, ActorRef[IOEvent]] = ActorSource.actorRef[IOEvent](
      completionMatcher = PartialFunction.empty,
      failureMatcher = PartialFunction.empty,
      bufferSize = 10,
      overflowStrategy = OverflowStrategy.dropHead
    )
    Behaviors.setup { context =>

      implicit val system = context.system
      val producerSettings: ProducerSettings[String, String] =
        ProducerSettings(system, new StringSerializer, new StringSerializer)
          .withBootstrapServers("localhost:9092")

      val kafkaProducer: ActorRef[IOEvent] =
        source.map { event =>
          ProducerMessage.single(
            new ProducerRecord("topic", "key", event.toString)
          )
        }
          .via(Producer.flexiFlow(producerSettings))
          .map {
            case ProducerMessage.Result(metadata, ProducerMessage.Message(record, passThrough)) =>
              s"${metadata.topic}/${metadata.partition} ${metadata.offset}: ${record.value}"
          }
          .to(Sink.foreach(println(_))).run()

      //.to(Producer.plainSink(producerSettings)).run()
      //.toMat(Sink.foreach(println(_)))(Keep.left).run()


      val network: ActorRef[NetworkCommand] = context.spawn(
        Network(kafkaProducer), "Network"
      )
      val device01: ActorRef[IOCommand] = context.spawn(
        IODeviceActor("Device1", network), "Device1"
      )
      val device02: ActorRef[IOCommand] = context.spawn(
        IODeviceActor("Device2", network), "Device2"
      )
      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }

  def main(args: Array[String]): Unit = {
    ActorSystem(Main(), "IOTDemo")
  }
}






