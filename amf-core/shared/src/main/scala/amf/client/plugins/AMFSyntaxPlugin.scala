package amf.client.plugins

import amf.core.parser.{ParsedDocument, ParserContext}
import org.yaml.model.YDocument

abstract class AMFSyntaxPlugin extends AMFPlugin {
  def supportedMediaTypes(): Seq[String]
  def parse(mediaType: String, text: CharSequence, ctx: ParserContext): Option[ParsedDocument]
  def unparse(mediaType: String, ast: YDocument): Option[CharSequence]
}
