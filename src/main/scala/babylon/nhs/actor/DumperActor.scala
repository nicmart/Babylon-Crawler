package babylon.nhs.actor

import akka.actor.{Actor, ActorLogging}
import babylon.nhs.actor.Supervisor.DumpReady
import babylon.nhs.output.Output.PageList
import babylon.nhs.writer.Writer

/**
  * Created by Nicolò Martini on 17/04/2017.
  */
class DumperActor(writer: Writer[PageList]) extends Actor with ActorLogging {

    import DumperActor._

    def receive: Receive = {
        case Dump(storage) =>
            writer.write(storage)
            sender ! DumpReady
    }
}

object DumperActor {
    sealed trait Message
    case class Dump(storage: PageList) extends Message
}
