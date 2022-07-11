package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec.OAS30
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.SchemaVersion
import org.yaml.model.YNode

trait SpecSettings {
  val spec: Spec
  val syntax: SpecSyntax
  def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode]
  def ignoreCriteria: IgnoreCriteria
  def isOasLikeContext: Boolean = isOas3Context || isOas2Context || isAsyncContext
  def isOas2Context: Boolean    = spec == Spec.OAS20
  def isOas3Context: Boolean    = spec == OAS30
  def isAsyncContext: Boolean   = spec == Spec.ASYNC20
  def isRamlContext: Boolean    = spec == Spec.RAML10 || spec == Spec.RAML08
  def ramlContextType: Option[RamlWebApiContextType]
  val defaultSchemaVersion: SchemaVersion
  def closedShapeValidator: ClosedShapeValidator = DefaultClosedShapeValidator(ignoreCriteria, spec, syntax)
  def shouldLinkTypes(parent: ParserContext)     = false
  val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder
}
