package amf.shapes.internal.spec.jsonschema.semanticjsonschema.context

import amf.aml.internal.semantic.{SemanticExtensionsFacade, SemanticExtensionsFacadeBuilder}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.{Annotations, Declarations, Fields, FragmentRef, FutureDeclarations, SearchScope}
import amf.core.internal.remote.{JsonSchemaDialect, Spec}
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import amf.core.internal.validation.core.ValidationSpecification
import amf.shapes.client.scala.model.domain.{AnyShape, CreativeWork, Example}
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.{RamlExternalSchemaExpressionFactory, ShapeParserContext}
import amf.shapes.internal.spec.common.{JSONSchemaDraft4SchemaVersion, JSONSchemaVersion}
import amf.shapes.internal.spec.common.parser.SpecSyntax
import amf.shapes.internal.spec.jsonschema.ref.AstIndex
import amf.shapes.internal.spec.raml.parser.{DefaultType, RamlTypeParser, TypeInfo}
import org.yaml.model.{YMap, YMapEntry, YNode, YPart}

import scala.collection.mutable

object JsonLdSchemaContext {
  def apply(ctx: ParserContext, schemaVersion: Option[JSONSchemaVersion]): ShapeParserContext = {
    new JsonLdSchemaContext(ctx) {
      override var jsonSchemaIndex: Option[AstIndex]         = None
      override var globalSpace: mutable.Map[String, Any]     = mutable.Map()
      override var localJSONSchemaContext: Option[YNode]     = None
      override var indexCache: mutable.Map[String, AstIndex] = mutable.Map()
      override def extensionsFacadeBuilder: SemanticExtensionsFacadeBuilder =
        (name: String) => SemanticExtensionsFacade.apply(name, ctx.config)
      override val defaultSchemaVersion: JSONSchemaVersion = schemaVersion.getOrElse(defaultSchemaVersion)

      override def makeJsonSchemaContextForParsing(
          url: String,
          document: Root,
          options: ParsingOptions
      ): ShapeParserContext =
        JsonLdSchemaContext(ctx, schemaVersion)
    }
  }

  def apply(ctx: ParserContext): ShapeParserContext = this.apply(ctx, None)
}

abstract class JsonLdSchemaContext(ctx: ParserContext) extends ShapeParserContext(ctx.eh) with JsonSchemaLikeContext {

  override def spec: Spec = JsonSchemaDialect

  override def syntax: SpecSyntax = JsonSchemaSyntax

  override def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo): Unit =
    throw new Exception("Parser - not in RAML!")

  override def rootContextDocument: String = ctx.rootContextDocument

  override def refs: Seq[ParsedReference] = ctx.refs

  override def getMaxYamlReferences: Option[Int] = None

  override def fragments: Map[String, FragmentRef] = Map()

  override def toOasNext: ShapeParserContext = this

  override def parsingOptions: ParsingOptions = ctx.parsingOptions

  override def findExample(key: String, scope: SearchScope.Scope): Option[Example] = None

  override def futureDeclarations: FutureDeclarations = ctx.futureDeclarations

  override def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit]): Option[AnyShape] = None

  override def loc: String = ctx.rootContextDocument

  override def shapes: Map[String, Shape] = Map()

  override def closedShape(node: AmfObject, ast: YMap, shape: String): Unit = {}

  override def registerJsonSchema(url: String, shape: AnyShape): Unit =
    ctx.globalSpace.update(normalizedJsonPointer(url), shape)

  override def isMainFileContext: Boolean = false

  override def findNamedExampleOrError(ast: YPart)(key: String): Example = {
    eh.violation(DeclarationNotFound, "", s"NamedExample '$key' not found", ast.location)
    Example(Fields(), Annotations(ast))
  }

  override def linkTypes: Boolean = false

  override def findNamedExample(key: String, error: Option[String => Unit]): Option[Example] = None

  override def isOasLikeContext: Boolean = false

  override def isOas2Context: Boolean = false

  override def isOas3Context: Boolean = false

  override def isAsyncContext: Boolean = false

  override def isRamlContext: Boolean = false

  override def isOas3Syntax: Boolean = false

  override def isOas2Syntax: Boolean = false

  override def ramlContextType: RamlWebApiContextType =
    throw new Exception("Parser - Can only be used from JSON Schema")

  override def promoteExternalToDataTypeFragment(text: String, fullRef: String, shape: Shape): Unit =
    throw new Exception("Parser - Can only be used from JSON Schema")

  override def findDocumentations(
      key: String,
      scope: SearchScope.Scope,
      error: Option[String => Unit]
  ): Option[CreativeWork] = None

  override def obtainRemoteYNode(ref: String, refAnnotations: Annotations): Option[YNode] =
    jsonSchemaRefGuide.obtainRemoteYNode(ref)

  override def addDeclaredShape(shape: Shape): Unit = {}

  override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] = None

  override def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
    ctx.violation(violationId, node, message)

  override def violation(violationId: ValidationSpecification, node: AmfObject, message: String): Unit =
    ctx.violation(violationId, node, message)

  override def addNodeRefIds(ids: mutable.Map[YNode, String]): Unit = {}

  override def nodeRefIds: mutable.Map[YNode, String] = mutable.Map()

  override def raml10createContextFromRaml: ShapeParserContext = this

  override def raml08createContextFromRaml: ShapeParserContext = this

  override def libraries: Map[String, Declarations] = Map()

  override def typeParser: (YMapEntry, Shape => Unit, Boolean, DefaultType) => RamlTypeParser =
    throw new Exception("Parser - Cann called only from JSON Schema")

  override def ramlExternalSchemaParserFactory: RamlExternalSchemaExpressionFactory =
    throw new Exception("Parser - Cann called only from JSON Schema")

  override def validateRefFormatWithError(ref: String): Boolean = true

  override val defaultSchemaVersion: JSONSchemaVersion = JSONSchemaDraft4SchemaVersion

  override def parseRemoteJSONPath(ref: String): Option[AnyShape] = None

  override def getInheritedDeclarations: Option[Declarations] = None

}
