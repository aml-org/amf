package amf.plugins.syntax

import amf.client.plugins.{AMFPlugin, AMFSyntaxPlugin}
import amf.core.client.ParsingOptions
import amf.core.parser.{ParsedDocument, ParserContext}
import amf.core.rdf.RdfModelDocument
import amf.core.unsafe.PlatformSecrets
import org.mulesoft.common.io.Output

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object RdfSyntaxPlugin extends AMFSyntaxPlugin with PlatformSecrets {

  override val ID = "Rdf"

  override def init(): Future[AMFPlugin] = Future { this }

  override def dependencies(): Seq[AMFPlugin] = Nil

  override def supportedMediaTypes() = Seq.empty[String]

  override def parse(mediaType: String,
                     text: CharSequence,
                     ctx: ParserContext,
                     options: ParsingOptions): Option[ParsedDocument] = {
    platform.rdfFramework match {
      case Some(r) if !options.isAmfJsonLdSerilization => r.syntaxToRdfModel(mediaType, text)
      case _                                           => None
    }
  }

  override def unparse[W: Output](mediaType: String, doc: ParsedDocument, writer: W): Option[W] =
    (doc, platform.rdfFramework) match {
      case (input: RdfModelDocument, Some(r)) => r.rdfModelToSyntaxWriter(mediaType, input, writer)
      case _                                  => None
    }

}
