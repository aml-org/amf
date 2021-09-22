package amf.apicontract.internal.spec.raml

import amf.apicontract.internal.spec.common.{RamlWebApiDeclarations, WebApiDeclarations}
import amf.apicontract.internal.spec.raml.RamlFragmentHeader._
import amf.apicontract.internal.spec.raml.RamlHeader.{Raml10, Raml10Extension, Raml10Library, Raml10Overlay}
import amf.apicontract.internal.spec.raml.parser.context.{Raml10WebApiContext, RamlWebApiContext}
import amf.apicontract.internal.spec.raml.parser.document.{ExtensionLikeParser, Raml10DocumentParser, RamlModuleParser}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.exception.InvalidDocumentHeaderException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{EmptyFutureDeclarations, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Mimes, Spec}

object Raml10ParsePlugin extends RamlParsePlugin {

  override def spec: Spec = Spec.RAML10

  override def applies(element: Root): Boolean = RamlHeader(element) exists {
    case Raml10 | Raml10Overlay | Raml10Extension | Raml10Library => true
    case Raml10DocumentationItem | Raml10NamedExample | Raml10DataType | Raml10ResourceType | Raml10Trait |
        Raml10AnnotationTypeDeclaration | Raml10SecurityScheme =>
      true
    case _ => false
  }

  override def mediaTypes: Seq[String] = Seq(Mimes.`application/yaml`)

  override def context(wrapped: ParserContext,
                       root: Root,
                       options: ParsingOptions,
                       ds: Option[WebApiDeclarations]): RamlWebApiContext =
    new Raml10WebApiContext(root.location,
                            root.references ++ wrapped.refs,
                            wrapped,
                            ds.map(d => RamlWebApiDeclarations(d)),
                            options = options)

  override protected def parseSpecificVersion(root: Root, ctx: RamlWebApiContext, header: RamlHeader): BaseUnit = {
    val clean = cleanContext(ctx, root, ctx.parsingOptions)
    header match {
      case Raml10          => Raml10DocumentParser(root)(ctx).parseDocument()
      case Raml10Overlay   => ExtensionLikeParser(root, Spec.RAML10, ctx).parseOverlay()
      case Raml10Extension => ExtensionLikeParser(root, Spec.RAML10, ctx).parseExtension()
      case Raml10Library   => RamlModuleParser(root, Spec.RAML10)(clean).parseModule()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(spec.id)
    }
  }

  // context that opens a new context for declarations and copies the global JSON Schema declarations
  private def cleanContext(wrapped: ParserContext, root: Root, options: ParsingOptions): RamlWebApiContext = {
    val cleanNested = ParserContext(root.location, root.references, EmptyFutureDeclarations(), wrapped.config)
    val clean       = context(cleanNested, root, options)
    clean.globalSpace = wrapped.globalSpace
    clean
  }
}
