package amf.plugins.document.webapi.parser

import amf.core.model.domain.Shape
import amf.core.parser.{FragmentRef, ParsedReference}
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.remote.Vendor
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.webapi.parser.spec.SpecSyntax
import amf.plugins.document.webapi.parser.spec.declaration.TypeInfo
import org.mulesoft.lexer.SourceLocation
import org.yaml.model.{IllegalTypeHandler, ParseErrorHandler, SyamlException, YError, YMap}

abstract class ErrorHandlingContext(implicit val eh: ParserErrorHandler)
    extends ParseErrorHandler
    with IllegalTypeHandler

abstract class ShapeParserContext(eh: ParserErrorHandler) extends ErrorHandlingContext()(eh) {

  def vendor: Vendor
  def syntax: SpecSyntax
  def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo)
  def rootContextDocument: String
  def refs: Seq[ParsedReference]
  def getMaxYamlReferences: Option[Long]
  def fragments: Map[String, FragmentRef]

  override def handle(location: SourceLocation, e: SyamlException): Unit = eh.handle(location, e)

  override def handle[T](error: YError, defaultValue: T): T = eh.handle(error, defaultValue)

  def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
    eh.violation(violationId, node, message, rootContextDocument)
}
