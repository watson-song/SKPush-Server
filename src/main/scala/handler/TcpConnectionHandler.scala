package handler

import akka.actor._
import akka.util.ByteString
import akka.io.Tcp.Write
import java.net.InetSocketAddress
import util.Constants

object TcpConnectionHandlerProps extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[TcpConnectionHandler], connection)
}

case class PushEvent(data: String)

class TcpConnectionHandler(connection: ActorRef) extends Handler(connection) {

  /**
   * Push the incoming message to all clients.
   */
  override def received(data: String) = {
    log.info("Received data => "+data)
  }
  
  override def receivedCommand(cmd: String, data: String) = {
      cmd match {
        case Constants.COMMAND_ADMINISTRATOR_PUSH_MSG => 
          context.actorSelection("../*") ! PushEvent(data)
          
        case Constants.COMMAND_HEARTBEAT_IDLE => 
          log.info("heartbeat idle")
        case Constants.COMMAND_HEARTBEAT_BUSY => 
          log.info("heartbeat busy")
        case Constants.COMMAND_API_CALL => 
          log.info("api call => "+data)
        case Constants.COMMAND_BIND_ID => 
          log.info("bind id => "+data)
        case Constants.COMMAND_CLOSE => 
          log.info("close connection => "+data)
          closed()
          stop()
        case Constants.COMMAND_TRANSFER_MSG => 
          log.info("transfer msg => "+data)
        case unknowCommand => 
          log.error("unknow command => "+unknowCommand)
      }
  }
  
}