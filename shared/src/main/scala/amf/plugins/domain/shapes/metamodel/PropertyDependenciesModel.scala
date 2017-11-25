package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type._
import amf.core.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.PropertyDependencies
import amf.core.vocabulary.Namespace.Shapes
import amf.core.vocabulary.ValueType

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
