package amf.shapes.internal.spec

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.document.{ErrorHandlingContext, UnresolvedComponents}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.{Annotations, Declarations, FutureDeclarations, SearchScope}
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Spec
import amf.shapes.client.scala.model.domain.{AnyShape, CreativeWork, Example}
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.{SpecSyntax, YMapEntryLike}
import amf.shapes.internal.spec.contexts.JsonSchemaRefGuide
import amf.shapes.internal.spec.datanode.DataNodeParserContext
import amf.shapes.internal.spec.raml.parser.external.RamlExternalTypesParser
import amf.shapes.internal.spec.raml.parser.{DefaultType, RamlTypeParser, TypeInfo}
import org.mulesoft.lexer.SourceLocation
import org.yaml.model._

import scala.collection.mutable

abstract class ShapeParserContext(eh: AMFErrorHandler)
    extends ErrorHandlingContext()(eh)
    with ParseErrorHandler
    with IllegalTypeHandler
    with DataNodeParserContext
    with UnresolvedComponents {

  val syamleh                                                            = new SyamlAMFErrorHandler(eh)
  override def handle[T](error: YError, defaultValue: T): T              = syamleh.handle(error, defaultValue)
  override def handle(location: SourceLocation, e: SyamlException): Unit = syamleh.handle(location, e)

  def toOasNext: ShapeParserContext
  def findExample(key: String, scope: SearchScope.Scope): Option[Example]
  def rootContextDocument: String
  def futureDeclarations: FutureDeclarations
  def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit] = None): Option[AnyShape]
  def link(node: YNode): Either[String, YNode]
  def loc: String
  def spec: Spec
  def syntax: SpecSyntax
  def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo)
  def shapes: Map[String, Shape]
  def closedShape(node: String, ast: YMap, shape: String): Unit
  def registerJsonSchema(url: String, shape: AnyShape)
  def isMainFileContext: Boolean
  def findNamedExampleOrError(ast: YPart)(key: String): Example
  def findLocalJSONPath(path: String): Option[YMapEntryLike]
  def linkTypes: Boolean
  def findJsonSchema(url: String): Option[AnyShape]
  def findNamedExample(key: String, error: Option[String => Unit] = None): Option[Example]
  def isOasLikeContext: Boolean
  def isOas2Context: Boolean
  def isOas3Context: Boolean
  def isAsyncContext: Boolean
  def isRamlContext: Boolean
  def isOas3Syntax: Boolean
  def isOas2Syntax: Boolean
  def ramlContextType: RamlWebApiContextType
  def promoteExternaltoDataTypeFragment(text: String, fullRef: String, shape: Shape): Shape
  def parseRemoteJSONPath(ref: String): Option[AnyShape]
  def findDocumentations(key: String,
                         scope: SearchScope.Scope,
                         error: Option[String => Unit] = None): Option[CreativeWork]

  def obtainRemoteYNode(ref: String, refAnnotations: Annotations = Annotations()): Option[YNode]
  def addNodeRefIds(ids: mutable.Map[YNode, String])
  def nodeRefIds: mutable.Map[YNode, String]
  def raml10createContextFromRaml: ShapeParserContext
  def raml08createContextFromRaml: ShapeParserContext
  def libraries: Map[String, Declarations]
  def typeParser: (YMapEntry, Shape => Unit, Boolean, DefaultType) => RamlTypeParser
  def ramlExternalSchemaParserFactory: RamlExternalSchemaExpressionFactory
  def getInheritedDeclarations: Option[Declarations]
  def makeJsonSchemaContextForParsing(url: String, document: Root, options: ParsingOptions): ShapeParserContext
  def computeJsonSchemaVersion(ast: YNode): SchemaVersion
  def setJsonSchemaAST(value: YNode): Unit
  def jsonSchemaRefGuide: JsonSchemaRefGuide
  def validateRefFormatWithError(ref: String): Boolean
}

trait RamlExternalSchemaExpressionFactory {
  def createXml(key: YNode, value: YNode, adopt: Shape => Unit, parseExample: Boolean = false): RamlExternalTypesParser
  def createJson(key: YNode,
                 value: YNode,
                 adopt: Shape => Unit,
                 parseExample: Boolean = false): RamlExternalTypesParser
}

object RamlWebApiContextType extends Enumeration {
  type RamlWebApiContextType = Value
  val DEFAULT, RESOURCE_TYPE, TRAIT, EXTENSION, OVERLAY, LIBRARY = Value
}
