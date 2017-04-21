package babylon.crawler.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import babylon.crawler.actor.DumperActor.Dump
import babylon.crawler.actor.ProxyActor.Message
import babylon.crawler.actor.SupervisorActor.DumpReady
import babylon.crawler.output.Output.PageList
import babylon.crawler.output.PageElement
import babylon.crawler.writer.Writer
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