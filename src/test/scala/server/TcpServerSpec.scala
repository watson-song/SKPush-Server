package server

import akka.actor.ActorSystem
import akka.io.Tcp.{ Register, Connected }
import akka.testkit.TestKit
import java.net.InetSocketAddress
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import handler.EchoHandlerProps

class TcpServerSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("TcpServerSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A TcpServer actor" must {

    "register a handler when a client connected" in {
      val server = system.actorOf(TcpServer.props(EchoHandlerProps), "ServerActor")
      server ! Connected(new InetSocketAddress(5555),
        new InetSocketAddress(9000))
      expectMsgPF() { case _: Register => }
    }

  }
}