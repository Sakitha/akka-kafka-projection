package AkkaStream

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import com.google.protobuf.timestamp.Timestamp
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt


object main extends App {
  implicit val system = ActorSystem("QuickStart")
  //val config = system.settings.config.getConfig("akka.kafka.producer")
  val producerSettings: ProducerSettings[String, Array[Byte]] =
    ProducerSettings(system, new StringSerializer, new ByteArraySerializer)
      .withBootstrapServers("localhost:9092")

  //akkaSystem.scheduler.scheduleOnce(5 seconds, actor, "msgFoo")
  // #plainSink

  val done: Future[Done] =
    Source.tick(1.second, 1.second, new MyEvent(1, Some(Timestamp.apply(123456, 123))))
      .map(value => new ProducerRecord("topic","key" ,value.toByteArray))
      .runWith(Producer.plainSink(producerSettings))

  

}
