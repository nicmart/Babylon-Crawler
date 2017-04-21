package babylon.crawler.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import babylon.crawler.actor.DumperActor.Dump
import babylon.crawler.actor.SupervisorActor.DumpReady
import babylon.crawler.output.Output.PageList
import babylon.crawler.output.PageElement
import babylon.crawler.writer.Writer
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class DumperActorSpec extends TestKit(ActorSystem("DumperActorSpec"))
    with WordSpecLike with BeforeAndAfterAll with ImplicitSender with Matchers {

    import DumperActorSpec._

    "DumperActor" must {

        "use the writer to dump the output" in {
            var output: PageList = null
            val dumper = system.actorOf(dumperProps( output = _ ))
            dumper ! Dump(pageList)
            expectMsg(DumpReady)
            output shouldBe pageList
        }

    }
}

object DumperActorSpec {
    val pageList = List(PageElement(
        "http://www.test.org",
        "MyTitle",
        "my content",
        List()
    ))

    def writer(callback: PageList => Unit): Writer[PageList] = new Writer[PageList] {
        def write(value: PageList): Unit = callback(value)
    }

    def dumperProps(callback: PageList => Unit): Props = {
        Props(new DumperActor(writer(callback)))
    }
}