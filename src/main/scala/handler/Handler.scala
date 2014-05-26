package handler

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.Tcp.Aborted
import akka.io.Tcp.Closed
import akka.io.Tcp.ConfirmedClosed
import akka.io.Tcp.ErrorClosed
import akka.io.Tcp.PeerClosed
import akka.io.Tcp.Received
import akka.io.Tcp.Write
import akka.util.ByteString
import db.DB
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json

trait HandlerProps {
  def props(connection: ActorRef): Props
}

/**
 * case class PushEvent 
 * Push data to specific tag group of the tcp connection
 */
case class PushEvent(tags: Seq[JsValue], data: JsValue)
/**
 * case class BindIDEvent
 * Bind the id to the specific connection when the device first connected
 */
case class BindIDEvent(id: String)
/**
 * case class BindTagsEvent
 * Bind tags to the specific connection 
 */
case class BindTagsEvent(tags: List[String])

abstract class Handler(val connection: ActorRef) extends Actor with ActorLogging with DB{
  /*** Group the connections, useful when push msg to specify group*/
  var mTags: Seq[JsValue] = List(JsString("global"))
  var mId: String = ""

  val COMMAND = "^:((?i)[a-zA-Z0-9_]+)(\\s*->\\s*([\\{\\s\\S\\}]*)){0,1}$".r("cmd", "datagroup", "data")

  def receive: Receive = {
    case Received(source) =>
      source.utf8String.trim match {
        case COMMAND(cmd, datagroup, data) => {
          if(data!=null) {
        	  receivedCommand(cmd, Some(Json.parse(data)))
          }else{
        	  receivedCommand(cmd, None)
          }
        }
        case unknow => received(unknow)
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
      
    case PushEvent(tags, data) => 
      push(tags, data)
  }

  def received(str: String): Unit
  def receivedCommand(cmd: String, data: Option[JsValue]): Unit

  def push(tags: Seq[JsValue], data: JsValue) {
    log.info("my tags = "+mTags+", push tags="+tags+", data="+data)
    mTags.find(tag => tags.exists(_ == tag)).map(_ => {
    	connection ! Write(ByteString(data.toString + "\n"))
    })
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
