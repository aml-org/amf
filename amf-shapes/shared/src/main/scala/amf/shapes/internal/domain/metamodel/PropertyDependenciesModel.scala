package amf.shapes.internal.domain.metamodel

import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type._
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.shapes.client.scala.domain.models.SchemaDependencies
import amf.shapes.client.scala.model.domain.PropertyDependencies

/**
  *
  */
trait DependenciesModel {
  val PropertySource = Field(
    Str,
    Shapes + "propertySource",
    ModelDoc(ModelVocabularies.Shapes, "propertySource", "Source property name in the dependency"))
}

object DependenciesModel extends DependenciesModel

object PropertyDependenciesModel extends DomainElementModel with DependenciesModel {

  val PropertyTarget = Field(
    Array(Str),
    Shapes + "propertyTarget",
    ModelDoc(ModelVocabularies.Shapes, "propertyTarget", "Target property name in the dependency"))

  override def fields: List[Field] =
    List(PropertySource, PropertyTarget) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "PropertyDependencies") ++ DomainElementModel.`type`

  override def modelInstance = PropertyDependencies()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "PropertyDependencies",
    "Dependency between sets of property shapes"
  )
}

object SchemaDependenciesModel extends DomainElementModel with DependenciesModel {

  val SchemaTarget = Field(
    ShapeModel,
    Shapes + "schemaTarget",
    ModelDoc(ModelVocabularies.Shapes, "schemaTarget", "Target applied shape in the dependency"))

  override def fields: List[Field] =
    List(PropertySource, SchemaTarget) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "SchemaDependencies") ++ DomainElementModel.`type`

  override def modelInstance = SchemaDependencies()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "SchemaDependencies",
    "Dependency between a property shape and a schema"
  )
}
