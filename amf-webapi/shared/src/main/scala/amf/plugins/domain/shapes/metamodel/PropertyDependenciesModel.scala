package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type._
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.plugins.domain.shapes.models.{PropertyDependencies, SchemaDependencies}
import amf.core.vocabulary.Namespace.Shapes
import amf.core.vocabulary.ValueType

/**
  *
  */

trait DependenciesModel {
  val PropertySource = Field(
    Iri,
    Shapes + "propertySource",
    ModelDoc(ModelVocabularies.Shapes, "propertySource", "Source property shape in the dependency"))
}

object DependenciesModel extends DependenciesModel

object PropertyDependenciesModel extends DomainElementModel with DependenciesModel {

  val PropertyTarget = Field(
    Array(Iri),
    Shapes + "propertyTarget",
    ModelDoc(ModelVocabularies.Shapes, "propertyTarget", "Target property shape in the dependency"))

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

object SchemaDependenciesModel extends DomainElementModel with  DependenciesModel {

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

