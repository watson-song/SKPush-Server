package handler

import scala.concurrent.ExecutionContext.Implicits.global
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
import api.Api
import spray.http.HttpMethod
import spray.http.HttpMethod
import spray.http.HttpMethods

object TcpConnectionHandlerProps extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[TcpConnectionHandler], connection)
}

class TcpConnectionHandler(connection: ActorRef) extends Handler(connection) {
  val HEARTBEAT = ByteString("0x1OK#\n");

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
    			  context.actorSelection("../*") ! PushEvent(array, wrap(jsonData.as[JsObject].-("tags")))
    		  }
			  case _ => log.error("wrong push command, data => "+data)
			}
    	  })
          
        case Constants.COMMAND_HEARTBEAT_IDLE => 
          connection ! Write(HEARTBEAT)
        case Constants.COMMAND_HEARTBEAT_BUSY => 
          connection ! Write(HEARTBEAT)
          
        case Constants.COMMAND_API_CALL => 
          data.map(jsonData => {
            jsonData\("uri") match {
              case JsString(uri) => 
	              Api.httpRequest(method = HttpMethods.GET, uri = uri) map {
	            	  response => connection ! Write(ByteString(response.entity.asString.replaceAll("\n", "") + "\n"))
	              }
              case _ =>
            }
          })
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
          data.map(jsonData => {
    		jsonData\("to") match {
    		  case JsString(to) => {
    		      //TODO lookup the specific connection id for device id(mId) from db
    		      val connectionId = to //fake connection id
    			  context.actorSelection("../"+connectionId) ! TransferMessageEvent(wrap(jsonData.as[JsObject].-("to")))
    		  }
			  case _ => log.error("wrong transfer message command, data => "+data)
			}
    	  })
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