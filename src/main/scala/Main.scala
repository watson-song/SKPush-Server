import akka.actor.{ Props, ActorSystem }
import handler._
import server.TcpServer
import server.ServerChannelClassificationEventBus
import akka.util.ByteString
import server.HttpServer

object MainWithPushHandler extends App {
	val system = ActorSystem("server")
	val tcpService = system.actorOf(TcpServer.props(TcpConnectionHandlerProps), "TcpServerActor")
	val httpServer = system.actorOf(Props(new HttpServer(9999)), "HttpServerActor")
}