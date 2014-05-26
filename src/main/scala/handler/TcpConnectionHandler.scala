package handler

import akka.actor.ActorRef
import akka.actor.ActorSelection.toScala
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.Tcp.Write
import akka.util.ByteString
import util.Constants
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.JsArray
import play.api.libs.json.JsUndefined
import play.api.libs.json.JsArray
import play.api.libs.json.JsString

object TcpConnectionHandlerProps extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[TcpConnectionHandler], connection)
}

class TcpConnectionHandler(connection: ActorRef) extends Handler(connection) {

  /**
   * Push the incoming message to all clients.
   */
  override def received(data: String) = {
    log.info("Received data => "+data)
  }
  
  override def receivedCommand(cmd: String, data: Option[JsValue]) = {
      cmd match {
        case Constants.COMMAND_ADMINISTRATOR_PUSH_MSG => 
    	  data.map(jsonData => {
    		jsonData\("tags") match {
    		  case JsArray(array) => {
    	    	  val start = System.currentTimeMillis() 
    			  context.actorSelection("../*") ! PushEvent(array, jsonData \ ("data"))
    			  log.info("done to push msg("+jsonData \ ("data")+") to "+array+", use "+(System.currentTimeMillis() - start)+"ms")
    		  }
			  case _ => log.error("wrong push command, data => "+data)
			}
    	  })
          
        case Constants.COMMAND_HEARTBEAT_IDLE => 
          log.info("heartbeat idle")
          connection ! Write(ByteString("1\n"))
        case Constants.COMMAND_HEARTBEAT_BUSY => 
          log.info("heartbeat busy")
          connection ! Write(ByteString("1\n"))
        case Constants.COMMAND_API_CALL => 
          log.info("api call => "+data)
        case Constants.COMMAND_BIND_ID => 
          log.info("bind id => "+data)
          data.map(jsonData => {
            jsonData match {
			  case JsString(id) => bindID(id) 
			  case _ => log.error("wrong bind id command, data =>"+data)
			}
          })
        case Constants.COMMAND_BIND_TAGS => 
          log.info("bind tags => "+data)
          data.map(jsonData => {
              jsonData match {
			  	case JsArray(tags) => bindTags(tags) 
			  	case _ => log.error("wrong bind tags command, data =>"+data)
			  }
          })
        case Constants.COMMAND_UNBIND_TAG => 
          log.info("unbind tag => "+data)
          data.map(jsonData => {
            jsonData match {
              case JsUndefined() => log.error("wrong unbind tag command, data => "+data)
              case tag => unbindTag(tag)
            }
          })
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
    
  def bindID(id: String) {
    mId = id
    //TODO initialize mTags information from DB if this id already registered
    log.info("bind id ->"+mId)
  }
  def bindTags(tags: Seq[JsValue]) {
    mTags = mTags ++: (tags)
    log.info("bind tags ->"+mTags)
  }
  def unbindTag(tag: JsValue) {
    mTags = mTags.filter(_ != tag)
    log.info("unbind tag ->"+mTags)
  }
  
}