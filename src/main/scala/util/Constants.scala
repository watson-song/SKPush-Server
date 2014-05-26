package util

import com.typesafe.config.ConfigFactory

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
}
