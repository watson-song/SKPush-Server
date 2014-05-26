package server

import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress
import util._
import handler._
import akka.actor.Props
import akka.actor.ActorRef
import java.util.ArrayList
import akka.io.Tcp.Bound
import akka.actor.ActorLogging
import java.util.UUID

object TcpServer {
  def props(handlerProps: HandlerProps): Props = Props(classOf[TcpServer], handlerProps)
}

class TcpServer(handlerProps: HandlerProps) extends Server with ActorLogging {

  import context.system

  IO(Tcp) ! Tcp.Bind(self, new InetSocketAddress(Conf.appHostName, Conf.appPort))

  override def receive = {
    case Tcp.Bound(localAddress) => log.info("TcpServer Bound successfully => "+localAddress.getHostName()+":"+localAddress.getPort())
    
    case Tcp.CommandFailed(_: Tcp.Bind) => context stop self

    case Tcp.Connected(remote, local) =>
      val handler = context.actorOf(handlerProps.props(sender)/*, UUID.randomUUID().toString()*/)
      log.info("NewClientConnect => "+(remote.getHostName()+":"+remote.getPort())+", "+handler.path)
      sender ! Tcp.Register(handler)
  }

}