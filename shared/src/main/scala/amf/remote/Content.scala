package amf.remote

import amf.lexer.CharStream

case class Content(stream: CharStream, mime: Option[String] = None)
