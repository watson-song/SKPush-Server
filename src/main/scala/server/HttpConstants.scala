package server

import akka.util.ByteString

object HttpConstants {
  val SP = ByteString(" ")
  val HT = ByteString("\t")
  val CRLF = ByteString("\r\n")
  val COLON = ByteString(":")
  val PERCENT = ByteString("%")
  val PATH = ByteString("/")
  val QUERY = ByteString("?")
}

case class Request(meth: String, path: List[String], query: Option[String], httpver: String, headers: List[Header], body: Option[ByteString])
case class Header(name: String, value: String)