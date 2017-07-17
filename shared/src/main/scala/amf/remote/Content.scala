package amf.remote

import amf.lexer.CharStream

case class Content(stream: CharStream, url: String, mime: Option[String] = None)
