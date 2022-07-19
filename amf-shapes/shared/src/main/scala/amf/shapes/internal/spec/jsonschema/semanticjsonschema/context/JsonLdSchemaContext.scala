package amf.shapes.internal.spec.jsonschema.semanticjsonschema.context

import amf.aml.internal.semantic.{SemanticExtensionsFacade, SemanticExtensionsFacadeBuilder}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.Fragment
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.{Annotations, Declarations, Fields, FragmentRef, FutureDeclarations, SearchScope}
import amf.core.internal.remote.{JsonSchemaDialect, Spec}
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import amf.core.internal.validation.core.ValidationSpecification
import amf.shapes.client.scala.model.domain.{AnyShape, CreativeWork, Example}
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.{JSONSchemaDraft4SchemaVersion, JSONSchemaVersion}
import amf.shapes.internal.spec.common.parser.{
  IgnoreAllCriteria,
  IgnoreCriteria,
  ShapeDeclarations,
  ShapeParserContext,
  SpecSettings,
  SpecSyntax
}
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaSettings
import amf.shapes.internal.spec.jsonschema.ref.AstIndex
import amf.shapes.internal.spec.raml.parser.{DefaultType, RamlTypeParser, TypeInfo}
import org.yaml.model.{YMap, YMapEntry, YNode, YPart}

import scala.collection.mutable

object JsonLdSchemaContext {
  def apply(ctx: ParserContext, schemaVersion: Option[JSONSchemaVersion]): ShapeParserContext = {
    new JsonLdSchemaContext(
      ctx,
      JsonSchemaSettings(JsonSchemaSyntax, schemaVersion.getOrElse(JSONSchemaDraft4SchemaVersion))
    ) {
      override def extensionsFacadeBuilder: SemanticExtensionsFacadeBuilder =
        (name: String) => SemanticExtensionsFacade.apply(name, ctx.config)
      override val defaultSchemaVersion: JSONSchemaVersion = schemaVersion.getOrElse(defaultSchemaVersion)

      override def ignoreCriteria: IgnoreCriteria = IgnoreAllCriteria

      override def makeJsonSchemaContextForParsing(
          url: String,
          document: Root,
          options: ParsingOptions
      ): ShapeParserContext =
        JsonLdSchemaContext(ctx, schemaVersion)

      override def addDeclaredShape(shape: Shape): Unit = Unit

      override def promotedFragments: Seq[Fragment] = Seq.empty

      override def registerExternalRef(external: (String, AnyShape)): Unit = Unit

      override def addPromotedFragments(fragments: Seq[Fragment]): Unit = Unit

      override def findInExternalsLibs(lib: String, name: String): Option[AnyShape] = None

      override def findInExternals(url: String): Option[AnyShape] = None

      override def removeLocalJsonSchemaContext: Unit = Unit

      override def toJsonSchema(): ShapeParserContext = this

      override def toJsonSchema(root: String, refs: Seq[ParsedReference]): ShapeParserContext = this

      override def registerExternalLib(url: String, content: Map[String, AnyShape]): Unit = Unit
    }

  }

  def apply(ctx: ParserContext): ShapeParserContext = this.apply(ctx, None)
}

abstract class JsonLdSchemaContext(ctx: ParserContext, settings: SpecSettings)
    extends ShapeParserContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      None,
      mutable.Map.empty,
      settings
    ) {

  override val eh: AMFErrorHandler = ctx.eh

  override def getMaxYamlReferences: Option[Int] = None

  override def fragments: Map[String, FragmentRef] = Map()

  override def toOas: ShapeParserContext = this

  override def parsingOptions: ParsingOptions = ctx.parsingOptions

  override def findExample(key: String, scope: SearchScope.Scope): Option[Example] = None

  override def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit]): Option[AnyShape] = None

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

  override def ramlContextType: Option[RamlWebApiContextType] = None

  override def promoteExternalToDataTypeFragment(text: String, fullRef: String, shape: Shape): Unit =
    throw new Exception("Parser - Can only be used from JSON Schema")

  override def findDocumentations(
      key: String,
      scope: SearchScope.Scope,
      error: Option[String => Unit]
  ): Option[CreativeWork] = None

  def obtainRemoteYNode(ref: String, refAnnotations: Annotations): Option[YNode] =
    jsonSchemaRefGuide.obtainRemoteYNode(ref)

  override def addDeclaredShape(shape: Shape): Unit = {}

  override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] = None

  override def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
    ctx.violation(violationId, node, message)

  override def violation(violationId: ValidationSpecification, node: AmfObject, message: String): Unit =
    ctx.violation(violationId, node, message)

  override def addNodeRefIds(ids: mutable.Map[YNode, String]): Unit = {}

  override def toRaml10: ShapeParserContext = this

  override def toRaml08: ShapeParserContext = this

  override def libraries: Map[String, Declarations] = Map()

  override def validateRefFormatWithError(ref: String): Boolean = true

  override val defaultSchemaVersion: JSONSchemaVersion = JSONSchemaDraft4SchemaVersion

  override def getInheritedDeclarations: Option[ShapeDeclarations] = None

}
