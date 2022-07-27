package amf.shapes.internal.spec.raml.parser

import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Spec
import RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.parser._
import amf.shapes.internal.spec.common.{JSONSchemaDraft4SchemaVersion, SchemaVersion}
import org.yaml.model.YNode

class Raml08Settings(val syntax: SpecSyntax, var contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT)
    extends SpecSettings {
  override val spec: Spec = Spec.RAML08

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = RamlLink.link(node)

  override def ignoreCriteria: IgnoreCriteria = Raml08IgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = Some(contextType)

  def setRamlContextType(ramlContextType: RamlWebApiContextType): Unit = {
    this.contextType = ramlContextType
  }

  override val defaultSchemaVersion: SchemaVersion                          = JSONSchemaDraft4SchemaVersion
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = DeclaredAnnotationSchemaValidatorBuilder
}
