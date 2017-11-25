package amf.core.plugins

import amf.core.parser.ParsedDocument
import org.yaml.model.YDocument

abstract class AMFSyntaxPlugin extends AMFPlugin {
  def supportedMediaTypes(): Seq[String]
  def parse(mediaType: String, text: CharSequence): Option[ParsedDocument]
  def unparse(mediaType: String, ast: YDocument): Option[CharSequence]
}
