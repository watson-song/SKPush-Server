package handler

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.Tcp.Write
import akka.util.ByteString
import api.Api
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsUndefined
import play.api.libs.json.JsValue
import server.Message
import server.MessageDispatchEvent
import server.ServerChannelClassificationEventBus
import spray.http.HttpMethods
import util.Constants
import java.util.UUID

object TcpConnectionHandlerProps extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[TcpConnectionHandler], connection)
}

class TcpConnectionHandler(connection: ActorRef) extends Handler(connection) {
  val RESPONSE_HEARTBEAT = ByteString(Array[Byte]('%', '%', 1.toByte, 2.toByte, (Constants.SKEP_COMMAND_RESPONSE_HEARTBEAT >>> 24).toByte, (Constants.SKEP_COMMAND_RESPONSE_HEARTBEAT >>> 16).toByte, (Constants.SKEP_COMMAND_RESPONSE_HEARTBEAT >>> 8).toByte, Constants.SKEP_COMMAND_RESPONSE_HEARTBEAT.toByte, 0.toByte,0.toByte, 0.toByte, 0.toByte, '$', '$', '\n'));
  val RESPONSE_BINDID = ByteString(Array[Byte]('%', '%', 1.toByte, 2.toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_ID >>> 24).toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_ID >>> 16).toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_ID >>> 8).toByte, Constants.SKEP_COMMAND_RESPONSE_BIND_ID.toByte, 0.toByte,0.toByte, 0.toByte, 0.toByte, '$', '$', '\n'));
  val RESPONSE_BINDTAGS = ByteString(Array[Byte]('%', '%', 1.toByte, 2.toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_TAGS >>> 24).toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_TAGS >>> 16).toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_TAGS >>> 8).toByte, Constants.SKEP_COMMAND_RESPONSE_BIND_TAGS.toByte, 0.toByte,0.toByte, 0.toByte, 0.toByte, '$', '$', '\n'));
  val RESPONSE_UNBINDTAG = ByteString(Array[Byte]('%', '%', 1.toByte, 2.toByte, (Constants.SKEP_COMMAND_RESPONSE_UNBIND_TAG >>> 24).toByte, (Constants.SKEP_COMMAND_RESPONSE_UNBIND_TAG >>> 16).toByte, (Constants.SKEP_COMMAND_RESPONSE_UNBIND_TAG >>> 8).toByte, Constants.SKEP_COMMAND_RESPONSE_UNBIND_TAG.toByte, 0.toByte,0.toByte, 0.toByte, 0.toByte, '$', '$', '\n'));

  /**
   * Push the incoming message to all clients.
   */
  override def received(data: String) = {
    log.info("Received data => "+data)
  }
  
  override def receivedCommand(cmd: Int, data: Option[JsValue]) = {
      cmd match {
        case Constants.SKEP_COMMAND_MSG_PUSH_OUT => 
    	  data.map(jsonData => {
    		jsonData\("tags") match {
    		  case JsArray(array) => {
    		      val msg = Message(UUID.randomUUID().toString(), wrap(jsonData.as[JsObject].-("tags")))
    		      //TODO should think about to reduce the event dispatch
    		      for(tag <- array) {
    		    	  ServerChannelClassificationEventBus.publish(MessageDispatchEvent(tag.as[JsString].value, msg))
    		      }
    		  }
			  case _ => log.error("wrong push command, data => "+data)
			}
    	  })
          
        case Constants.SKEP_COMMAND_REQUEST_HEARTBEAT_IDLE => 
          connection ! Write(RESPONSE_HEARTBEAT)
        case Constants.SKEP_COMMAND_REQUEST_HEARTBEAT_BUSY => 
          connection ! Write(RESPONSE_HEARTBEAT)
          
        case Constants.SKEP_COMMAND_REQUEST_API_CALL => 
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
          
        case Constants.SKEP_COMMAND_REQUEST_BIND_ID => 
          log.info("bind id => "+data)
          data.map(jsonData => {
            jsonData match {
			  case JsString(id) => bindID(id) 
			  case _ => log.error("wrong bind id command, data =>"+data)
			}
          })
          
        case Constants.SKEP_COMMAND_REQUEST_BIND_TAGS => 
          log.info("bind tags => "+data)
          data.map(jsonData => {
              jsonData match {
			  	case JsArray(tags) => bindTags(tags) 
			  	case _ => log.error("wrong bind tags command, data =>"+data)
			  }
          })
        case Constants.SKEP_COMMAND_REQUEST_UNBIND_TAG => 
          log.info("unbind tag => "+data)
          data.map(jsonData => {
            jsonData match {
              case JsUndefined() => log.error("wrong unbind tag command, data => "+data)
              case tag => unbindTag(tag)
            }
          })
        case Constants.SKEP_COMMAND_MSG_TRANSFER => 
          data.map(jsonData => {
    		jsonData\("to") match {
    		  case JsString(to) => {
    		      //TODO lookup the specific connection id for device id(mId) from db
    		      ServerChannelClassificationEventBus.publish(MessageDispatchEvent("""/private/"""+to, wrap(jsonData.as[JsObject].-("to"))))
    		  }
			  case _ => log.error("wrong transfer message command, data => "+data)
			}
    	  })
          log.info("transfer msg => "+data)
          
        case unknowCommand => 
          log.error("unknow command => "+unknowCommand)
      }
  }
  
  /***
   * Bind id to specific tcp connection, after bind, every tcp connection can talk to each other
   */
  def bindID(id: String) {
    mId = id
	ServerChannelClassificationEventBus.subscribe(self, """/private/"""+id)
	connection ! Write(RESPONSE_BINDID)
    //TODO initialize mTags information from DB if this id already registered
    log.info("bind id ->"+mId)
  }
  
  /***
   * Bind tags if any connection interested in some tags content
   */
  def bindTags(tags: Seq[JsValue]) {
    for(tag <- tags) ServerChannelClassificationEventBus.subscribe(self, """/group/"""+tag.as[JsString].value)
    connection ! Write(RESPONSE_BINDTAGS)
    log.info("bind tags ->"+tags)
  }
  
  /***
   * Unbind tag when not interested anymore
   */
  def unbindTag(tag: JsValue) {
    ServerChannelClassificationEventBus.unsubscribe(self, """/group/"""+tag.as[JsString].value)
    connection ! Write(RESPONSE_UNBINDTAG)
    log.info("unbind tag ->"+tag)
  }
  
}