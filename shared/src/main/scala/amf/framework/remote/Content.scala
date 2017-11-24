package amf.framework.remote

import amf.core.lexer.CharStream

case class Content(stream: CharStream, url: String, mime: Option[String] = None)
