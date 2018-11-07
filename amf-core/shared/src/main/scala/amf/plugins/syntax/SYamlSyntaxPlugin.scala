package amf.plugins.syntax

import amf.client.plugins.{AMFPlugin, AMFSyntaxPlugin}
import amf.core.benchmark.ExecutionLog
import amf.core.client.ParsingOptions
import amf.core.parser.{ParsedDocument, ParserContext, SyamlParsedDocument}
import amf.core.rdf.RdfModelDocument
import amf.core.unsafe.PlatformSecrets
import org.mulesoft.common.io.Output
import org.yaml.model.{YComment, YDocument, YMap, YNode}
import org.yaml.parser.{JsonParser, YamlParser}
import org.yaml.render.{JsonRender, YamlRender}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SYamlSyntaxPlugin extends AMFSyntaxPlugin with PlatformSecrets {

  override val ID = "SYaml"

  override def init(): Future[AMFPlugin] = Future { this }

  override def dependencies(): Seq[AMFPlugin] = Nil

  override def supportedMediaTypes() = Seq(
    "application/yaml",
    "application/x-yaml",
    "text/yaml",
    "text/x-yaml",
    "application/json",
    "text/json",
    "application/raml",
    "text/vnd.yaml"
  )

  override def parse(mediaType: String,
                     text: CharSequence,
                     ctx: ParserContext,
                     options: ParsingOptions): Option[ParsedDocument] = {

    if ((mediaType == "application/ld+json" || mediaType == "application/json") && !options.isAmfJsonLdSerilization && platform.rdfFramework.isDefined) {
      platform.rdfFramework.get.syntaxToRdfModel(mediaType, text)
    } else {
      val parser = getFormat(mediaType) match {
        case "json" =>
          JsonParser.withSource(text, ctx.currentFile)(ctx) //include tag only for raml right? so only for yaml
        case _ => YamlParser(text, ctx.currentFile)(ctx).withIncludeTag("!include")
      }
      val parts = parser.parse(true)

      if (parts.exists(v => v.isInstanceOf[YDocument])) {
        parts collectFirst { case d: YDocument => d } map { document =>
          val comment = parts collectFirst { case c: YComment => c }
          SyamlParsedDocument(document, comment)
        }
      } else {
        parts collectFirst { case d: YComment => d } map { comment =>
          SyamlParsedDocument(YDocument(IndexedSeq(YNode(YMap.empty)), ctx.currentFile), Some(comment))
        }
      }
    }
  }

  override def unparse[W: Output](mediaType: String, doc: ParsedDocument, writer: W): Option[W] = {
    doc match {
      case input: SyamlParsedDocument =>
        val ast = input.document
        render(mediaType, ast) { (format, ast) =>
          if (format == "yaml") YamlRender.render(writer, ast, expandReferences = false)
          else JsonRender.render(ast, writer)
          Some(writer)
        }
      case input: RdfModelDocument if platform.rdfFramework.isDefined =>
        platform.rdfFramework.get.rdfModelToSyntaxWriter(mediaType, input, writer)
      case _ => None
    }
  }

  private def render[T](mediaType: String, ast: YDocument)(render: (String, YDocument) => T): T = {
    val format = getFormat(mediaType)

    ExecutionLog.log(s"Serialising to format $format")

    val result: T = render(format, ast)

    ExecutionLog.log(s"Got the serialisation $format")

    result
  }

  private def getFormat(mediaType: String) = if (mediaType.contains("json")) "json" else "yaml"
}
