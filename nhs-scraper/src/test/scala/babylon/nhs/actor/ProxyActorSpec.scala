package babylon.nhs.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import babylon.nhs.actor.DumperActor.Dump
import babylon.nhs.actor.ProxyActor.Message
import babylon.nhs.actor.SupervisorActor.DumpReady
import babylon.nhs.output.Output.PageList
import babylon.nhs.output.PageElement
import babylon.nhs.writer.Writer
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ProxyActorSpec extends TestKit(ActorSystem("ProxyActorSpec"))
    with WordSpecLike with BeforeAndAfterAll with ImplicitSender with Matchers {

    "A ProxyActor" must {

        "relays the messages to the correct destination" in {
            val proxy = system.actorOf(Props(new ProxyActor))
            proxy ! Message(testActor, "messageContent1", testActor)
            proxy ! Message(testActor, "messageContent2", testActor)
            expectMsg("messageContent1")
            expectMsg("messageContent2")
        }

    }
}