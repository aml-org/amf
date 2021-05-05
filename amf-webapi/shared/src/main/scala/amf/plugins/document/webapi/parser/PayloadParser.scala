package amf.plugins.document.webapi.parser

import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.{Annotations, FragmentRef, FutureDeclarations, ParsedReference, ParserContext, SearchScope}
import amf.core.remote.Vendor
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.{Oas2WebApiContext, Oas3WebApiContext}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.RamlWebApiContextType.RamlWebApiContextType
import amf.plugins.document.webapi.parser.spec.{SpecSyntax, toOas}
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import amf.plugins.document.webapi.parser.spec.declaration.TypeInfo
import amf.plugins.document.webapi.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.oas.{Oas2Syntax, Oas3Syntax}
import amf.plugins.domain.shapes.models.{AnyShape, CreativeWork, Example}
import org.mulesoft.lexer.SourceLocation
import org.yaml.model.{SyamlException, YDocument, YError, YMap, YNode, YPart}

class PayloadParser(document: YDocument, location: String, mediaType: String)(implicit ctx: WebApiContext) {

  def parseUnit(): PayloadFragment = {
    val payload        = parseNode(location, document.node)
    val parsedDocument = PayloadFragment(payload, mediaType).adopted(location)
    parsedDocument
  }

  private def parseNode(parent: String, node: YNode) =
    DataNodeParser(node, parent = Some(parent))(WebApiShapeParserContextAdapter(ctx)).parse()
}

object PayloadParser {
  def apply(document: YDocument, location: String, mediaType: String)(implicit ctx: WebApiContext) =
    new PayloadParser(document, location, mediaType)
}
