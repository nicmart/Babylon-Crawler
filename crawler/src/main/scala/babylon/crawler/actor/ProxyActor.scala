package babylon.crawler.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import babylon.crawler.actor.ProxyActor.Message

import scala.concurrent.duration._
import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.OverflowStrategy
import akka.stream.ThrottleMode
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

/**
  * The purpose of this actor is to make messages sent to diffrent actors all pass through this
  * proxy actor. In this way some messages flows can be centralized and controlled in some wats.
  * (see throttling in the companion object)
  */
class ProxyActor extends Actor with ActorLogging {

    def receive: Receive = {
        case Message(to, message, sender) =>
            to.tell(message, sender)
    }
}

object ProxyActor {

    /**
      * A Meta-Message describing the intention to send a message to a target from a sender
      *
      * @param to The target of the message
      * @param message The message
      * @param sender The sender of the message
      */
    case class Message(to: ActorRef, message: Any, sender: ActorRef)

    /**
      * Use Akka-streams to throttle the incoming messages to a n actor.
      * See http://doc.akka.io/docs/akka/current/project/migration-guide-2.4.x-2.5.x.html#migration-guide-timerbasedthrottler
      */
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
