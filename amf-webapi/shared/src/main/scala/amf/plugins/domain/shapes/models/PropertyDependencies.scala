package amf.plugins.domain.shapes.models

import amf.client.model.StrField
import amf.core.metamodel.Obj
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.PropertyDependenciesModel
import amf.plugins.domain.shapes.metamodel.PropertyDependenciesModel._
import org.yaml.model.YMapEntry

/**
  * Property Dependency
  */
case class PropertyDependencies(fields: Fields, annotations: Annotations) extends DomainElement {

  def propertySource: StrField      = fields.field(PropertySource)
  def propertyTarget: Seq[StrField] = fields.field(PropertyTarget)

  def withPropertySource(propertySource: String): this.type      = set(PropertySource, propertySource)
  def withPropertyTarget(propertyTarget: Seq[String]): this.type = set(PropertyTarget, propertyTarget)

  /** Call after object has been adopted by specified parent. */
  override def adopted(parent: String): PropertyDependencies.this.type =
    withId(parent + "/dependency") // TODO check id for each dependency
  override def meta: Obj = PropertyDependenciesModel
}

object PropertyDependencies {
  def apply(): PropertyDependencies = apply(Annotations())

  def apply(ast: YMapEntry): PropertyDependencies = apply(Annotations(ast))

  def apply(annotations: Annotations): PropertyDependencies = PropertyDependencies(Fields(), annotations)
}
