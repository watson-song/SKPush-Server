import akka.actor.{ Props, ActorSystem }
import handler._
import server.TcpServer
import server.ServerChannelClassificationEventBus
import akka.util.ByteString
import server.HttpServer

object MainWithPushHandler extends App {
  
	val system = ActorSystem("server")
	/*** Start a TCP server to listen client connection*/
	val tcpService = system.actorOf(TcpServer.props(TcpConnectionHandlerProps), "TcpServerActor")
	
	/*** Start a HTTP server to provide the web ui for push messages <- ONLY FOR TEST*/
	val httpServer = system.actorOf(Props(new HttpServer(9999)), "HttpServerActor")
}