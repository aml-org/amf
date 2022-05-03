package amf.shapes.internal.entities

import amf.core.internal.entities.Entities
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.shapes.internal.domain.metamodel.{operations, _}
import amf.shapes.internal.domain.metamodel.operations.{AbstractOperationModel, AbstractParameterModel, AbstractPayloadModel, AbstractRequestModel, AbstractResponseModel, ShapeOperationModel, ShapeParameterModel, ShapePayloadModel, ShapeRequestModel, ShapeResponseModel}

private[amf] object ShapeEntities extends Entities {

  override val innerEntities: Seq[ModelDefaultBuilder] = Seq(
    AnyShapeModel,
    ArrayShapeModel,
    TupleShapeModel,
    MatrixShapeModel,
    FileShapeModel,
    NilShapeModel,
    NodeShapeModel,
    ShapeOperationModel,
    ShapeParameterModel,
    ShapePayloadModel,
    ShapeRequestModel,
    ShapeResponseModel,
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
    BaseIRIModel,
    AbstractOperationModel,
    AbstractParameterModel,
    AbstractPayloadModel,
    AbstractRequestModel,
    AbstractResponseModel
  )
}
