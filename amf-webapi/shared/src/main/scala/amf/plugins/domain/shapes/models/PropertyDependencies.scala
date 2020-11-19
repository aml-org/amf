package amf.plugins.domain.shapes.models

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.PropertyDependenciesModel
import amf.plugins.domain.shapes.metamodel.PropertyDependenciesModel._
import org.yaml.model.YMapEntry

/**
  * Property Dependency
  */

trait Dependencies extends DomainElement {
  def propertySource: StrField      = fields.field(PropertySource)
  def withPropertySource(propertySource: String): this.type      = set(PropertySource, propertySource)

  override def componentId: String = {
    val propertySourceName = propertySource.option().map(x => x).getOrElse("unknown").split("/").last
    s"/dependency/$propertySourceName"
  }
}

case class PropertyDependencies(fields: Fields, annotations: Annotations) extends Dependencies {

  def propertyTarget: Seq[StrField] = fields.field(PropertyTarget)
  def withPropertyTarget(propertyTarget: Seq[String]): this.type = set(PropertyTarget, propertyTarget)

  override def meta: Obj = PropertyDependenciesModel
}

object PropertyDependencies {
  def apply(): PropertyDependencies = apply(Annotations())

  def apply(ast: YMapEntry): PropertyDependencies = apply(Annotations(ast))

  def apply(annotations: Annotations): PropertyDependencies = PropertyDependencies(Fields(), annotations)
}
