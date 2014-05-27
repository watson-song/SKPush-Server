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
import java.util.UUID
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber
import server.MessageDispatchEvent
import server.Message
import server.ServerChannelClassificationEventBus

trait HandlerProps {
  def props(connection: ActorRef): Props
}

/**
 * case class PushEvent 
 * Push data to specific tag group of the tcp connection
 */
case class PushEvent(tag: JsValue, data: JsObject)
/**
 * case class TransferMessageEvent 
 * Transfer data to specific tcp connection client
 */
case class TransferMessageEvent(data: JsObject)
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
  var mId: String = self.path.name

  def receive: Receive = {
    case Received(source) =>
      log.info(source.mkString(","))
      getMessage(source) match {
	    case Some(message) => {
	      val version = message(0)
		  val packageType = message(1)
		  val command = message.slice(2, 6)
		  val length = message.slice(6, 10)
		  val data = if(message.length>10) Some(Json.parse(message.drop(10).utf8String)) else None
	      log.info("version="+version+", packageType="+packageType+", command="+command+", length="+length+", data="+data)
	      receivedCommand(byteArrayToInt(command.toArray), data)
	    }
	    case None => received(source.toString)
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
      
    case Message(id, data) => {
      connection ! Write(new Message(id, data).toByteString.concat(ByteString("\n")))
    }
    case PushEvent(tag, msg) => {
      ServerChannelClassificationEventBus.publish(MessageDispatchEvent(tag.as[JsString].value, msg))
    }
    case TransferMessageEvent(data) => 
      transfer(data)
  }
  
  def byteArrayToInt(b: Array[Byte]): Integer = {
    b(3) & 0xFF | (b(2) & 0xFF) << 8 | (b(1) & 0xFF) << 16 |(b(0) & 0xFF) << 24;
  }
  def intToByteArray(value: Int): Array[Byte] = {
	Array[Byte]((value >>> 24).toByte, (value >>> 16).toByte, (value >>> 8).toByte, value.toByte)
  }

  def received(str: String): Unit
  def receivedCommand(cmd: Int, data: Option[JsValue]): Unit

  def getMessage(data: ByteString): Option[ByteString] = {
    getMessageRange(data) match {
      case Some((start, end)) => {
        if(start == -1) {
          //TODO no start command message
          return None
        }
        if(end == -1) {
          //TODO no end command message
        	return None
        }
        return Some(data.slice(start, end))
      }
      case _ => None
    }
  }
  
  def getMessageRange(data: ByteString): Option[(Integer, Integer)] = {
    val length = data.length
    
    def getStartIndex(): Either[Integer, Integer] = {
      for(i <- 0 to length-1) {
	      if(data(i)==37&&i+1<length&&data(i+1)==37) {
	        //start of the command
	        return Left(i+2)
	      }
	      
	      if(data(i)==36&&i+1<length&&data(i+1)==36) {
			//end of the command
			return Right(i)
	      }
      }
      Left(-1)
    }
    
    getStartIndex() match {
      case Left(startIndex) => {
        def getEndIndex(): Integer = {
    		for(j <- startIndex.toInt to length-1) {
    			if(j > -1&&data(j)==36&&j+1<length&&data(j+1)==36) {
    				//end of the command
    				return j
    			}
    		}
    		-1
        }
        
        return Some(startIndex, getEndIndex)
      }
      case Right(endIndex) => {
        return Some(-1, endIndex)
      }
    }
    
    None
  }
  
  def transfer(data: JsObject) {
    log.info("transfer message "+data);
    //TODO write the msg to db
    connection ! Write(ByteString(data.toString+"\n"))
  }
  
  def wrap(data: JsObject):JsObject = {
    data.+("from", JsString(mId)).+("timestamp", JsNumber(System.currentTimeMillis()))
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
