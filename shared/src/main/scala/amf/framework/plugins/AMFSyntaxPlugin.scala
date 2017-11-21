package amf.framework.plugins

import amf.compiler.ParsedDocument
import org.yaml.model.YDocument

abstract class AMFSyntaxPlugin {
  val ID: String
  def supportedMediaTypes(): Seq[String]
  def parse(mediaType: String, text: CharSequence): Option[ParsedDocument]
  def unparse(mediaType: String, ast: YDocument): Option[CharSequence]
}
