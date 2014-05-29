package util

import com.typesafe.config.ConfigFactory
import akka.util.ByteString

object Constants {

  val COMMAND_HEARTBEAT_IDLE = "hb0"
  val COMMAND_HEARTBEAT_BUSY = "hb1"
    
  val COMMAND_BIND_ID = "bind_id"
  val COMMAND_BIND_TAGS = "bind_tags"
  val COMMAND_UNBIND_TAG = "unbind_tag"
  val COMMAND_TRANSFER_MSG = "transfer_msg"
  val COMMAND_API_CALL = "api_call"
  val COMMAND_CLOSE = "close"
    
  val COMMAND_ADMINISTRATOR_PUSH_MSG = "push_msg"
    
  /*** Scala Akka Exchange Protocol Commands - start*/
	
  /*** Normal command message 100-299*/
  val SKEP_COMMAND_MSG_PUSH_OUT: Int = 100; 
  val SKEP_COMMAND_MSG_PUSH_IN: Int = 101;
  val SKEP_COMMAND_MSG_TRANSFER: Int = 102;
	
  /*** Request command message 300-599*/
  val SKEP_COMMAND_REQUEST_HEARTBEAT_IDLE: Int = 300;
  val SKEP_COMMAND_REQUEST_HEARTBEAT_BUSY: Int = 301;
  val SKEP_COMMAND_REQUEST_BIND_ID: Int = 302;
  val SKEP_COMMAND_REQUEST_BIND_TAGS: Int = 303;
  val SKEP_COMMAND_REQUEST_UNBIND_TAG: Int = 304;
  val SKEP_COMMAND_REQUEST_API_CALL: Int = 305;
	
  /*** Response command message 600-899*/
  val SKEP_COMMAND_RESPONSE_HEARTBEAT: Int = 600;
  val SKEP_COMMAND_RESPONSE_BIND_ID: Int = 601;
  val SKEP_COMMAND_RESPONSE_BIND_TAGS: Int = 602;
  val SKEP_COMMAND_RESPONSE_UNBIND_TAG: Int = 603;
  val SKEP_COMMAND_RESPONSE_API_CALL: Int = 604;
  
  /*** Scala Akka Exchange Protocol Commands - end*/
  
  val MESSAGE_PREFIX_DATA = List[Byte]('%', '%', 1.toByte, 0.toByte,  (Constants.SKEP_COMMAND_MSG_PUSH_IN >>> 24).toByte, (Constants.SKEP_COMMAND_MSG_PUSH_IN >>> 16).toByte, (Constants.SKEP_COMMAND_MSG_PUSH_IN >>> 8).toByte, Constants.SKEP_COMMAND_MSG_PUSH_IN.toByte)
  val RESPONSE_HEARTBEAT = ByteString(Array[Byte]('%', '%', 1.toByte, 2.toByte, (Constants.SKEP_COMMAND_RESPONSE_HEARTBEAT >>> 24).toByte, (Constants.SKEP_COMMAND_RESPONSE_HEARTBEAT >>> 16).toByte, (Constants.SKEP_COMMAND_RESPONSE_HEARTBEAT >>> 8).toByte, Constants.SKEP_COMMAND_RESPONSE_HEARTBEAT.toByte, 0.toByte,0.toByte, 0.toByte, 0.toByte, '$', '$', '\n'));
  val RESPONSE_BINDID = ByteString(Array[Byte]('%', '%', 1.toByte, 2.toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_ID >>> 24).toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_ID >>> 16).toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_ID >>> 8).toByte, Constants.SKEP_COMMAND_RESPONSE_BIND_ID.toByte, 0.toByte,0.toByte, 0.toByte, 0.toByte, '$', '$', '\n'));
  val RESPONSE_BINDTAGS = ByteString(Array[Byte]('%', '%', 1.toByte, 2.toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_TAGS >>> 24).toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_TAGS >>> 16).toByte, (Constants.SKEP_COMMAND_RESPONSE_BIND_TAGS >>> 8).toByte, Constants.SKEP_COMMAND_RESPONSE_BIND_TAGS.toByte, 0.toByte,0.toByte, 0.toByte, 0.toByte, '$', '$', '\n'));
  val RESPONSE_UNBINDTAG = ByteString(Array[Byte]('%', '%', 1.toByte, 2.toByte, (Constants.SKEP_COMMAND_RESPONSE_UNBIND_TAG >>> 24).toByte, (Constants.SKEP_COMMAND_RESPONSE_UNBIND_TAG >>> 16).toByte, (Constants.SKEP_COMMAND_RESPONSE_UNBIND_TAG >>> 8).toByte, Constants.SKEP_COMMAND_RESPONSE_UNBIND_TAG.toByte, 0.toByte,0.toByte, 0.toByte, 0.toByte, '$', '$', '\n'));

}
