package amf.framework.plugins

import amf.compiler.ParsedDocument

abstract class AMFSyntaxPlugin {
  val ID: String
  def supportedMediaTypes(): Seq[String]
  def parse(text: CharSequence): Option[ParsedDocument]
}
