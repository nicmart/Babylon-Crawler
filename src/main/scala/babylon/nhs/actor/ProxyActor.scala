package babylon.nhs.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import babylon.nhs.actor.ProxyActor.Message

import scala.concurrent.duration._
import akka.NotUsed
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.OverflowStrategy
import akka.stream.ThrottleMode
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

/**
  * Created by Nicolò Martini on 17/04/2017.
  */
class ProxyActor extends Actor with ActorLogging {

    def receive: Receive = {
        case Message(to, message, sender) =>
            to.tell(message, sender)
    }
}

object ProxyActor {
    case class Message(to: ActorRef, message: Any, sender: ActorRef)

    def throttledProxy(
        target: ActorRef,
        messagesPerSecond: Int
    )(implicit materializer: akka.stream.Materializer): ActorRef = {
        Source.actorRef(bufferSize = 10000, OverflowStrategy.dropNew)
            .throttle(messagesPerSecond, 1.second, messagesPerSecond, ThrottleMode.Shaping)
            .to(Sink.actorRef(target, NotUsed))
            .run()
    }
}
