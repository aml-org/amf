package amf.shapes.internal.entities

import amf.core.internal.entities.Entities
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.shapes.internal.domain.metamodel._

private[amf] object ShapeEntities extends Entities {

  override val innerEntities: Seq[ModelDefaultBuilder] = Seq(
    AnyShapeModel,
    ArrayShapeModel,
    TupleShapeModel,
    MatrixShapeModel,
    FileShapeModel,
    NilShapeModel,
    NodeShapeModel,
    PropertyShapeModel,
    PropertyDependenciesModel,
    ScalarShapeModel,
    SchemaShapeModel,
    UnionShapeModel,
    XMLSerializerModel,
    ShapeExtensionModel,
    ExampleModel,
    SchemaDependenciesModel,
    CreativeWorkModel,
    IriTemplateMappingModel,
    DiscriminatorValueMappingModel,
    DefaultVocabularyModel,
    SemanticContextModel,
    CuriePrefixModel,
    ContextMappingModel,
    BaseIRIModel
  )
}
