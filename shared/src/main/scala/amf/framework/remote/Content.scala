package amf.framework.remote

import amf.framework.lexer.CharStream

case class Content(stream: CharStream, url: String, mime: Option[String] = None)
