import akka.actor.{ Props, ActorSystem }
import handler._
import server.TcpServer

object MainWithPushHandler extends App {
	val system = ActorSystem("server")
	val tcpService = system.actorOf(TcpServer.props(TcpConnectionHandlerProps), "TcpServerActor")
}

//object MainWithEchoHandler extends App {
//  val system = ActorSystem("server")
//  val service = system.actorOf(TcpServer.props(EchoHandlerProps), "ServerActor")
//}
//
//object MainWithApiHandler extends App {
//  val system = ActorSystem("server")
//  val service = system.actorOf(TcpServer.props(ApiHandlerProps), "ServerActor")
//}
//
//object MainWithDbHandler extends App {
//  val system = ActorSystem("server")
//  val service = system.actorOf(TcpServer.props(DbHandlerProps), "ServerActor")
//}