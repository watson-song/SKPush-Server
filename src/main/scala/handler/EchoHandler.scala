package handler

import akka.actor._
import akka.util.ByteString
import akka.io.Tcp.Write
import java.net.InetSocketAddress

object EchoHandlerProps extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[EchoHandler], connection)
}

class EchoHandler(connection: ActorRef) extends Handler(connection) {

  /**
   * Echoes incoming message.
   */
  def received(data: String) = connection ! Write(ByteString(data + "\n"))
  
  override def receivedCommand(cmd: String, data: String) = {
  }
}