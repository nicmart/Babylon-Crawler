package babylon.nhs.actor

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import babylon.nhs.actor.ProxyActor.Message

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
}
