package server

import akka.actor.ActorLogging
import akka.actor.IO
import akka.actor.IOManager
import akka.actor.Actor
import java.net.InetSocketAddress
import akka.util.ByteString
import play.api.libs.json.JsString
import akka.actor.ActorContext
import handler.PushEvent
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import java.util.UUID
import server.http.Request
import server.http.Header

object HttpServer {
  
  val pushFormHtml = """<form method="get" action="/push" name="myform">
		  <tr>
		  <td colspan="2" align="center">
		  1. push msg to all online users, please use tag /group	<br/>
		  2. push msg to group tag online users, please use tag /group/XXX -> xxx is the tag	<br/>
		  3. push msg to specify online user, please use tag /private/bindid	-> bindid is the id binded to the tcp connection, call bindid api first <br/>
		  <br/>
		  <br/>
		  <br/>
		  </td>
		  </tr>
					<table width="40%" height="20%" align="center" border="0" bordercolor="red" cellspacing="0" cellpadding="0">
						<tr>
						  <td align="right">tag&nbsp;</td>
						  <td><input type="text" name="tag" size="40" maxlength="50"/></td>
						</tr>
						<tr>
							<td align="right">message&nbsp;</td>
							<td><textarea cols="30" rows="4" name="msg"></textarea></td>
						</tr>
						<tr>
							<td colspan="2" align="center">
							    <input type="submit" value="Sumbit"/>
							</td>
						</tr>
					</table>
				</form>"""
  import HttpIteratees._
 
  def processRequest(socket: IO.SocketHandle, context: ActorContext): IO.Iteratee[Unit] = IO repeat {
      for {
        request <- readRequest
      } yield {
        val rsp = request match {
          //meth: String, path: List[String], query: Option[String], httpver: String, headers: List[Header], body: Option[ByteString]
          case Request("GET", "api" :: Nil, queries, _, headers, body) => {
        	  OKResponse(ByteString("""{"queries":""""+queries+""""}"""),
        			  request.headers.exists {
        			  case Header(n, v) =>
        			  n.toLowerCase == "connection" && v.toLowerCase == "keep-alive"
        	  })
          }
          case Request("GET", "push" :: Nil, queries, _, headers, body) => {
        	  var tag = ""
			  var content = ""
              queries.map(query => {
            	  val queryString = query.split("&")
            	  if(queryString.length==2) {
            	    for(s <- queryString) {
            	    	if(s.startsWith("tag=")) {
            	    		tag = s.replace("tag=", "")
            	    	}
            	    	if(s.startsWith("msg=")) {
            	    		content = s.replace("msg=", "")
            	    	}
            	    }
            	    ServerChannelClassificationEventBus.publish(MessageDispatchEvent(tag, Message(UUID.randomUUID().toString(), "web", Json.parse("""{"data":""""+content+"""", "tag":""""+tag+"""", "from":"web", "timestamp":""""+System.currentTimeMillis()+""""}"""))))
            	  }
              	}
    		  )
        	  OKResponse(ByteString("<p>Push tag="+tag+", msg="+content+"</p><hr/>"+pushFormHtml),
        			  request.headers.exists {
        			  case Header(n, v) =>
        			  n.toLowerCase == "connection" && v.toLowerCase == "keep-alive"
        	  })
          }
          case req =>
            OKResponse(ByteString("""<h1>Welcome SPush Board!!!</h1><p>Powered by Watson Song</p><hr/>"""+pushFormHtml),
              request.headers.exists {
                case Header(n, v) =>
                  n.toLowerCase == "connection" && v.toLowerCase == "keep-alive"
              })
        }
        socket write OKResponse.bytes(rsp).compact
        if (!rsp.keepAlive) socket.close()
      }
    }
}

class HttpServer(port: Int) extends Actor {
 
  val state = IO.IterateeRef.Map.async[IO.Handle]()(context.dispatcher)
 
  override def preStart {
    IOManager(context.system) listen new InetSocketAddress(port)
  }
 
  def receive = {
 
    case IO.NewClient(server) =>
      val socket = server.accept()
      state(socket) flatMap (_ => HttpServer.processRequest(socket, context))
 
    case IO.Read(socket, bytes) =>
      state(socket)(IO Chunk bytes)
 
    case IO.Closed(socket, cause) =>
      state(socket)(IO EOF)
      state -= socket
 
  }
 
}