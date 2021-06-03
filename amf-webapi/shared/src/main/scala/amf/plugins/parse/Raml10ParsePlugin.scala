package amf.plugins.parse
import amf.client.remod.amfcore.config.ParsingOptions
import amf.core.Root
import amf.core.exception.InvalidDocumentHeaderException
import amf.core.model.document.BaseUnit
import amf.core.parser.{EmptyFutureDeclarations, ParserContext}
import amf.core.remote.{Vendor}
import amf.plugins.document.webapi.contexts.parser.raml.{Raml10WebApiContext, RamlWebApiContext}
import amf.plugins.document.webapi.parser.RamlFragmentHeader._
import amf.plugins.document.webapi.parser.RamlHeader
import amf.plugins.document.webapi.parser.RamlHeader.{Raml10, Raml10Extension, Raml10Library, Raml10Overlay}
import amf.plugins.document.webapi.parser.spec.raml.{ExtensionLikeParser, Raml10DocumentParser, RamlModuleParser}
import amf.plugins.document.webapi.parser.spec.{RamlWebApiDeclarations, WebApiDeclarations}

object Raml10ParsePlugin extends RamlParsePlugin {

  override def vendor: Vendor = Vendor.RAML10

  override def applies(element: Root): Boolean = RamlHeader(element) exists {
    case Raml10 | Raml10Overlay | Raml10Extension | Raml10Library => true
    case Raml10DocumentationItem | Raml10NamedExample | Raml10DataType | Raml10ResourceType | Raml10Trait |
        Raml10AnnotationTypeDeclaration | Raml10SecurityScheme =>
      true
    case _ => false
  }

  override def mediaTypes: Seq[String] = Seq(
    "application/raml10",
    "application/raml10+yaml",
    "application/raml",
    "application/raml+yaml"
  )

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
      case Raml10Overlay   => ExtensionLikeParser(root, ctx).parseOverlay()
      case Raml10Extension => ExtensionLikeParser(root, ctx).parseExtension()
      case Raml10Library   => RamlModuleParser(root)(clean).parseModule()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(vendor.name)
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
