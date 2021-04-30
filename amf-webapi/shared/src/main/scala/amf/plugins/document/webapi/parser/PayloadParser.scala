package amf.plugins.document.webapi.parser

import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.parser.{FragmentRef, ParsedReference, ParserContext}
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.SpecSyntax
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import amf.plugins.document.webapi.parser.spec.declaration.TypeInfo
import org.mulesoft.lexer.SourceLocation
import org.yaml.model.{SyamlException, YDocument, YError, YMap, YNode}

case class WebApiShapeParserContextAdapter(ctx: WebApiContext) extends ShapeParserContext(ctx.eh) {
  override def vendor: Vendor = ctx.vendor

  override def syntax: SpecSyntax = ctx.syntax

  override def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo): Unit = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.closedRamlTypeShape(shape, ast, shapeType, typeInfo)
    case _                          => throw new Exception("Parser - not in RAML!")
  }

  override def rootContextDocument: String = ctx.rootContextDocument

  override def refs: Seq[ParsedReference] = ctx.refs

  override def getMaxYamlReferences: Option[Long] = ctx.options.getMaxYamlReferences

  override def fragments: Map[String, FragmentRef] = ctx.declarations.fragments
}

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
