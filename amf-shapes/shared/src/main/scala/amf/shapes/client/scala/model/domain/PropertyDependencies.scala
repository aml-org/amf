package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.PropertyDependenciesModel
import amf.shapes.internal.domain.metamodel.PropertyDependenciesModel._
import org.yaml.model.YMapEntry

/** Property Dependency
  */
trait Dependencies extends DomainElement {
  def propertySource: StrField                              = fields.field(PropertySource)
  def withPropertySource(propertySource: String): this.type = set(PropertySource, propertySource)

  private[amf] override def componentId: String = {
    val propertySourceName = propertySource.option().map(x => x).getOrElse("unknown").split("/").last
    s"/dependency/$propertySourceName"
  }
}

case class PropertyDependencies private[amf] (fields: Fields, annotations: Annotations) extends Dependencies {

  def propertyTarget: Seq[StrField]                              = fields.field(PropertyTarget)
  def withPropertyTarget(propertyTarget: Seq[String]): this.type = set(PropertyTarget, propertyTarget)

  override def meta: PropertyDependenciesModel.type = PropertyDependenciesModel
}

object PropertyDependencies {
  def apply(): PropertyDependencies = apply(Annotations())

  def apply(ast: YMapEntry): PropertyDependencies = apply(Annotations(ast))

  def apply(annotations: Annotations): PropertyDependencies = PropertyDependencies(Fields(), annotations)
}
