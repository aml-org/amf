package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.parse.document.ErrorHandlingContext
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.validation.core.ValidationSpecification
import org.mulesoft.common.client.lexical.SourceLocation
import org.yaml.model.{IllegalTypeHandler, ParseErrorHandler, SyamlException, YError}

class JsonLDParserContext(val eh: AMFErrorHandler)
    extends ErrorHandlingContext
    with ParseErrorHandler
    with IllegalTypeHandler {

  // TODO native-jsonld: unify with shape context (extract to abstract?)
  def syamleh = new SyamlAMFErrorHandler(eh)

  override def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
    eh.violation(violationId, node, message)

  override def violation(
      specification: ValidationSpecification,
      node: String,
      message: String,
      loc: SourceLocation
  ): Unit = eh.violation(specification, node, message, loc)

  override def violation(violationId: ValidationSpecification, node: AmfObject, message: String): Unit =
    eh.violation(violationId, node, message)

  override def handle(location: SourceLocation, e: SyamlException): Unit = syamleh.handle(location, e)

  override def handle[T](error: YError, defaultValue: T): T = syamleh.handle(error, defaultValue)
}
