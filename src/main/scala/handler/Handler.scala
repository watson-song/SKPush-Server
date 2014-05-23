package handler

import db.DB
import akka.actor.{ Props, ActorRef, Actor }
import akka.io.Tcp._
import akka.io.Tcp.Received
import akka.util.ByteString
import akka.actor.ActorLogging
import akka.actor.ActorIdentity
import java.net.InetSocketAddress

trait HandlerProps {
  def props(connection: ActorRef): Props
}

abstract class Handler(val connection: ActorRef) extends Actor with ActorLogging with DB{

  val command = "^:((?i)[a-zA-Z_]+)(\\s*->\\s*(\\{[\\s\\S]*\\})){0,1}$".r("cmd", "datagroup", "data")

  def receive: Receive = {
    case Received(data) =>
      data.utf8String.trim match {
        case command(cmd, datagroup, data) => receivedCommand(cmd, data)
        case str => received(str)
      }
    case PeerClosed =>
      peerClosed()
      stop()
    case ErrorClosed =>
      errorClosed()
      stop()
    case Closed =>
      closed()
      stop()
    case ConfirmedClosed =>
      confirmedClosed()
      stop()
    case Aborted =>
      aborted()
      stop()
      
    case PushEvent(data) => 
      pushContent(data)
  }

  def received(str: String): Unit
  def receivedCommand(cmd: String, data: String): Unit

  def pushContent(data: String) {
    connection ! Write(ByteString(data + "\n"))
  }
  
  def peerClosed() {
    println("PeerClosed")
  }

  def errorClosed() {
    println("ErrorClosed")
  }

  def closed() {
    println("Closed")
  }

  def confirmedClosed() {
    println("ConfirmedClosed")
  }

  def aborted() {
    println("Aborted")
  }

  def stop() {
    println("Stopping")
    context stop self
  }
}
