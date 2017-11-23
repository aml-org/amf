package amf.metadata.shape

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type._
import amf.metadata.domain.DomainElementModel
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
}
