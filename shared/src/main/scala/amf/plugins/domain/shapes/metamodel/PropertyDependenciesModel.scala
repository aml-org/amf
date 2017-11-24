package amf.plugins.domain.shapes.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type._
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.PropertyDependencies
import amf.vocabulary.Namespace.Shapes
import amf.vocabulary.ValueType

/**
  *
  */
object PropertyDependenciesModel extends DomainElementModel {

  val PropertySource = Field(Iri, Shapes + "propertySource")

  val PropertyTarget = Field(Array(Iri), Shapes + "propertyTarget")

  override def fields: List[Field] = List(PropertySource, PropertyTarget) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "PropertyDependencies") ++ DomainElementModel.`type`

  override def modelInstance = PropertyDependencies()
}
