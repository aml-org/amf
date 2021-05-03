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

  override def toOasNext: ShapeParserContext = copy(toOas(ctx))

  override def findExample(key: String, scope: SearchScope.Scope): Option[Example] =
    ctx.declarations.findExample(key, scope)

  override def futureDeclarations: FutureDeclarations = ctx.futureDeclarations

  override def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit]): Option[AnyShape] =
    ctx.declarations.findType(key, scope, error)

  override def link(node: YNode): Either[String, YNode] = ctx.link(node)

  override def loc: String = ctx.rootContextDocument

  override def shapes: Map[String, Shape] = ctx.declarations.shapes

  override def closedShape(node: String, ast: YMap, shape: String): Unit = ctx.closedShape(node, ast, shape)

  override def registerJsonSchema(url: String, shape: AnyShape): Unit = ctx.registerJsonSchema(url, shape)

  override def isMainFileContext: Boolean = ctx match {
    case oasCtx: OasLikeWebApiContext => oasCtx.isMainFileContext
    case _                            => throw new Exception("Parser - Can only be used from OAS!")
  }

  override def findNamedExampleOrError(ast: YPart)(key: String): Example =
    ctx.declarations.findNamedExampleOrError(ast)(key)

  override def findLocalJSONPath(path: String): Option[YMapEntryLike] = ctx.findLocalJSONPath(path)

  override def linkTypes: Boolean = ctx match {
    case oasCtx: OasLikeWebApiContext => oasCtx.linkTypes
    case _                            => throw new Exception("Parser - Can only be used from OAS!")
  }

  override def findJsonSchema(url: String): Option[AnyShape] = ctx.findJsonSchema(url)

  override def findNamedExample(key: String, error: Option[String => Unit]): Option[Example] =
    ctx.declarations.findNamedExample(key, error)

  override def isOasLikeContext: Boolean = ctx match {
    case oasCtx: OasLikeWebApiContext => true
    case _                            => false
  }

  override def isOas2Context: Boolean = ctx match {
    case oasCtx: Oas2WebApiContext => true
    case _                         => false
  }

  override def isOas3Context: Boolean = ctx match {
    case oasCtx: Oas3WebApiContext => true
    case _                         => false
  }

  override def isAsyncContext: Boolean = ctx match {
    case oasCtx: AsyncWebApiContext => true
    case _                          => false
  }

  override def isRamlContext: Boolean = ctx match {
    case oasCtx: RamlWebApiContext => true
    case _                         => false
  }

  override def isOas3Syntax: Boolean = ctx.syntax == Oas3Syntax

  override def isOas2Syntax: Boolean = ctx.syntax == Oas2Syntax

  override def ramlContextType: RamlWebApiContextType = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.contextType
    case _                          => throw new Exception("Parser - Can only be used from RAML!")
  }

  override def promoteExternaltoDataTypeFragment(text: String, fullRef: String, shape: Shape): Shape =
    ctx.declarations.promoteExternaltoDataTypeFragment(text, fullRef, shape)

  override def parseRemoteJSONPath(ref: String): Option[AnyShape] = ctx match {
    case oasCtx: OasLikeWebApiContext => oasCtx.parseRemoteJSONPath(ref)
    case _                            => throw new Exception("Parser - Can only be used from OAS!")
  }

  override def findDocumentations(key: String,
                                  scope: SearchScope.Scope,
                                  error: Option[String => Unit]): Option[CreativeWork] =
    ctx.declarations.findDocumentations(key, scope, error)

  override def obtainRemoteYNode(ref: String, refAnnotations: Annotations): Option[YNode] =
    ctx.obtainRemoteYNode(ref, refAnnotations)(ctx)

  override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] =
    ctx.declarations.findAnnotation(key, scope)

  override def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
    ctx.violation(violationId, node, message)
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
