package amf.remote

import amf.common.AMFToken
import amf.json.JsonLexer
import amf.lexer.{AbstractLexer, CharStream}
import amf.yaml.YamlLexer

case class Content(stream: CharStream, mime: Option[String] = None)
