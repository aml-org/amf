package amf.apicontract.internal.spec.common.parser

import amf.aml.internal.semantic.{SemanticExtensionsFacade, SemanticExtensionsFacadeBuilder}
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.apicontract.internal.spec.jsonschema.JsonSchemaWebApiContext
import amf.apicontract.internal.spec.oas.parser.context.{
  Oas2Syntax,
  Oas2WebApiContext,
  Oas3Syntax,
  Oas3WebApiContext,
  OasLikeWebApiContext
}
import amf.apicontract.internal.spec.raml.parser.context.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.apicontract.internal.spec.raml.parser.external.DefaultRamlExternalSchemaExpressionFactory
import amf.apicontract.internal.spec.spec.{toOas, toOasDeclarations}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.parse.document.{EmptyFutureDeclarations, ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain._
import amf.core.internal.remote.Spec
import amf.core.internal.validation.core.ValidationSpecification
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.client.scala.model.domain.{AnyShape, CreativeWork, Example}
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.{SpecSyntax, YMapEntryLike}
import amf.shapes.internal.spec.contexts.JsonSchemaRefGuide
import amf.shapes.internal.spec.raml.parser.{DefaultType, RamlTypeParser, TypeInfo}
import amf.shapes.internal.spec.{RamlExternalSchemaExpressionFactory, ShapeParserContext}
import org.yaml.model.{YMap, YMapEntry, YNode, YPart}

import scala.collection.mutable

case class WebApiShapeParserContextAdapter(ctx: WebApiContext) extends ShapeParserContext(ctx.eh) {
  override def spec: Spec = ctx.spec

  override def syntax: SpecSyntax = ctx.syntax

  override def extensionsFacadeBuilder: SemanticExtensionsFacadeBuilder = ctx.extensionsFacadeBuilder

  override def parsingOptions: ParsingOptions = ctx.parsingOptions

  override def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo): Unit = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.closedRamlTypeShape(shape, ast, shapeType, typeInfo)
    case _                          => throw new Exception("Parser - not in RAML!")
  }

  override def rootContextDocument: String = ctx.rootContextDocument

  override def refs: Seq[ParsedReference] = ctx.refs

  override def getMaxYamlReferences: Option[Int] = ctx.options.getMaxYamlReferences

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

  override def closedShape(node: AmfObject, ast: YMap, shape: String): Unit = ctx.closedShape(node, ast, shape)

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
    case _: OasLikeWebApiContext => true
    case _                       => false
  }

  override def isOas2Context: Boolean = ctx match {
    case _: Oas2WebApiContext => true
    case _                    => false
  }

  override def isOas3Context: Boolean = ctx match {
    case _: Oas3WebApiContext => true
    case _                    => false
  }

  override def isAsyncContext: Boolean = ctx match {
    case _: AsyncWebApiContext => true
    case _                     => false
  }

  override def isRamlContext: Boolean = ctx match {
    case _: RamlWebApiContext => true
    case _                    => false
  }

  override def isOas3Syntax: Boolean = ctx.syntax == Oas3Syntax

  override def isOas2Syntax: Boolean = ctx.syntax == Oas2Syntax

  override def ramlContextType: RamlWebApiContextType = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.contextType
    case _                          => throw new Exception("Parser - Can only be used from RAML!")
  }

  override def promoteExternalToDataTypeFragment(text: String, fullRef: String, shape: Shape): Unit =
    ctx.declarations.promoteExternalToDataTypeFragment(text, fullRef, shape)

  override def parseRemoteJSONPath(ref: String): Option[AnyShape] = ctx match {
    case oasCtx: OasLikeWebApiContext => oasCtx.parseRemoteJSONPath(ref)
    case _                            => throw new Exception("Parser - Can only be used from OAS!")
  }

  override def findDocumentations(
      key: String,
      scope: SearchScope.Scope,
      error: Option[String => Unit]
  ): Option[CreativeWork] =
    ctx.declarations.findDocumentations(key, scope, error)

  def addDeclaredShape(shape: Shape): Unit = ctx.declarations += shape

  override def obtainRemoteYNode(ref: String, refAnnotations: Annotations): Option[YNode] =
    ctx.obtainRemoteYNode(ref)(ctx)

  override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] =
    ctx.declarations.findAnnotation(key, scope)

  override def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
    ctx.violation(violationId, node, message)

  override def violation(violationId: ValidationSpecification, node: AmfObject, message: String): Unit =
    ctx.violation(violationId, node, message)

  override def addNodeRefIds(ids: mutable.Map[YNode, String]): Unit = ctx.nodeRefIds ++= ids

  override def nodeRefIds: mutable.Map[YNode, String] = ctx.nodeRefIds

  override def raml10createContextFromRaml: ShapeParserContext = ctx match {
    case ramlCtx: RamlWebApiContext =>
      val context = new Raml10WebApiContext(
        ramlCtx.rootContextDocument,
        ramlCtx.refs,
        ramlCtx,
        Some(ramlCtx.declarations),
        ramlCtx.contextType,
        ramlCtx.options
      )
      WebApiShapeParserContextAdapter(context)
    case _ => throw new Exception("Parser - Can be called only from RAML!")
  }

  override def raml08createContextFromRaml: ShapeParserContext = ctx match {
    case ramlCtx: RamlWebApiContext =>
      val nextCtx = new Raml08WebApiContext(
        ramlCtx.rootContextDocument,
        ramlCtx.refs,
        ramlCtx,
        Some(ramlCtx.declarations),
        contextType = ramlCtx.contextType,
        options = ramlCtx.options
      )
      WebApiShapeParserContextAdapter(nextCtx)
    case _ => throw new Exception("Parser - Can be called only from RAML!")
  }

  override def libraries: Map[String, Declarations] = ctx.declarations.libraries

  override def typeParser: (YMapEntry, Shape => Unit, Boolean, DefaultType) => RamlTypeParser = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.factory.typeParser
    case _                          => throw new Exception("Parser - Can be called only from RAML!")
  }

  override def ramlExternalSchemaParserFactory: RamlExternalSchemaExpressionFactory = ctx match {
    case ramlCtx: RamlWebApiContext => DefaultRamlExternalSchemaExpressionFactory()(ramlCtx)
    case _                          => throw new Exception("Parser - Can be called only from RAML!")
  }

  override def getInheritedDeclarations: Option[OasWebApiDeclarations] = ctx match {
    case ramlContext: Raml08WebApiContext => Some(toOasDeclarations(ramlContext.declarations))
    case _                                => None
  }

  override def makeJsonSchemaContextForParsing(
      url: String,
      document: Root,
      options: ParsingOptions
  ): ShapeParserContext = {
    val cleanNested = ParserContext(url, document.references, EmptyFutureDeclarations(), ctx.config)
    cleanNested.globalSpace = ctx.globalSpace

    // Apparently, in a RAML 0.8 API spec the JSON Schema has a closure over the schemas declared in the spec...
    val inheritedDeclarations = getInheritedDeclarations

    val schemaContext = new JsonSchemaWebApiContext(
      url,
      document.references,
      cleanNested,
      inheritedDeclarations,
      options,
      ctx.defaultSchemaVersion
    )
    schemaContext.indexCache = ctx.indexCache
    WebApiShapeParserContextAdapter(schemaContext)
  }

  override def computeJsonSchemaVersion(ast: YNode): SchemaVersion = ctx.computeJsonSchemaVersion(ast)

  override def setJsonSchemaAST(value: YNode): Unit = ctx.setJsonSchemaAST(value)

  override def jsonSchemaRefGuide: JsonSchemaRefGuide = ctx.getJsonSchemaRefGuide

  override def validateRefFormatWithError(ref: String): Boolean = ctx.validateRefFormatWithError(ref)
}
