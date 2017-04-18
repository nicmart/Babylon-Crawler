package babylon.nhs.actor

import akka.actor.{Actor, ActorLogging}
import babylon.nhs.actor.SupervisorActor.DumpReady
import babylon.nhs.output.Output.PageList
import babylon.nhs.writer.Writer

/**
  * Dump the output received using the writer
  */
class DumperActor(writer: Writer[PageList]) extends Actor with ActorLogging {

    import DumperActor._

    def receive: Receive = {
        case Dump(output) =>
            writer.write(output)
            sender ! DumpReady
    }
}

object DumperActor {
    sealed trait Message

    /**
      * The only message supported by this actor: a request to dump the output
      */
    case class Dump(output: PageList) extends Message
}
