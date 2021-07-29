package amf.apicontract.internal.spec.raml

import amf.apicontract.internal.spec.common.{RamlWebApiDeclarations, WebApiDeclarations}
import amf.apicontract.internal.spec.raml.RamlHeader.Raml08
import amf.apicontract.internal.spec.raml.parser.context.{Raml08WebApiContext, RamlWebApiContext}
import amf.apicontract.internal.spec.raml.parser.document
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.exception.InvalidDocumentHeaderException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{LinkReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec

object Raml08ParsePlugin extends RamlParsePlugin {

  override def vendor: Spec = Spec.RAML08

  override def applies(element: Root): Boolean = RamlHeader(element) exists {
    // Partial raml0.8 fragment with RAML header but linked through !include
    // we need to generate an external fragment and inline it in the parent document
    case Raml08 if element.referenceKind != LinkReference => true
    case _: RamlFragment                                  => false
    case _                                                => false
  }

  override def mediaTypes: Seq[String] = Raml08MediaTypes.mediaTypes

  override def context(wrapped: ParserContext,
                       root: Root,
                       options: ParsingOptions,
                       ds: Option[WebApiDeclarations]): RamlWebApiContext =
    new Raml08WebApiContext(root.location,
                            root.references ++ wrapped.refs,
                            wrapped,
                            ds.map(d => RamlWebApiDeclarations(d)),
                            options = options)

  override protected def parseSpecificVersion(root: Root, ctx: RamlWebApiContext, header: RamlHeader): BaseUnit = {
    header match {
      case Raml08 => document.Raml08DocumentParser(root)(ctx).parseDocument()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(vendor.id)
    }
  }
}
