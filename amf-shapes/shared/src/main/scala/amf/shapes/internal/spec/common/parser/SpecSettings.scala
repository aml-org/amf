package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec._
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType.RamlWebApiContextType
import org.yaml.model.YNode

trait SpecSettings {
  val spec: Spec
  val syntax: SpecSyntax
  def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode]
  def ignoreCriteria: IgnoreCriteria
  def isOasLikeContext: Boolean = isOas31Context || isOas3Context || isOas2Context || isAsyncContext
  def isOas2Context: Boolean    = spec == OAS20
  def isOas3Context: Boolean    = spec == OAS30
  def isOas31Context: Boolean   = spec == OAS31
  def isAsyncContext: Boolean =
    spec == ASYNC20 || spec == ASYNC21 || spec == ASYNC22 || spec == ASYNC23 || spec == ASYNC24 || spec == ASYNC25 || spec == ASYNC26
  def isRamlContext: Boolean = spec == RAML10 || spec == RAML08
  def ramlContextType: Option[RamlWebApiContextType]
  val defaultSchemaVersion: SchemaVersion
  def closedShapeValidator: ClosedShapeValidator = DefaultClosedShapeValidator(ignoreCriteria, spec, syntax)
  def shouldLinkTypes(parent: ParserContext)     = false
  val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder
}
