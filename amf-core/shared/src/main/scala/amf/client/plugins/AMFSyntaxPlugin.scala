package amf.client.plugins

import java.io.StringWriter

import amf.core.client.ParsingOptions
import amf.core.parser.{ParsedDocument, ParserContext}
import org.mulesoft.common.io.Output

abstract class AMFSyntaxPlugin extends AMFPlugin {

  def supportedMediaTypes(): Seq[String]

  def parse(mediaType: String,
            text: CharSequence,
            ctx: ParserContext,
            parsingOptions: ParsingOptions): Option[ParsedDocument]

  def unparse(mediaType: String, ast: ParsedDocument): Option[CharSequence] =
    unparse(mediaType, ast, new StringWriter).map(_.toString)

  def unparse[W: Output](mediaType: String, ast: ParsedDocument, writer: W): Option[W]
}
