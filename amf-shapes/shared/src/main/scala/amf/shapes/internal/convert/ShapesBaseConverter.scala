package amf.shapes.internal.convert

import amf.aml.internal.convert.VocabulariesBaseConverter
import amf.core.internal.convert.BidirectionalMatcher
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.platform.config.{
  JsonLDSchemaConfiguration,
  JsonLDSchemaConfigurationClient,
  AMFSemanticSchemaResult => ClientAMFSemanticSchemaResult,
  SemanticJsonSchemaConfiguration => ClientSemanticJsonSchemaConfiguration
}
import amf.shapes.client.platform.model.document.{JsonSchemaDocument => ClientJsonSchemaDocument}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.platform.model.domain
import amf.shapes.client.platform.{
  JsonLDInstanceResult,
  JsonLDSchemaElementClient,
  JsonLDSchemaResult,
  ShapesConfiguration => ClientShapesConfiguration
}
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.config.{AMFSemanticSchemaResult, SemanticJsonSchemaConfiguration, JsonLDSchemaConfiguration => InternalJsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient => InternalJsonLDSchemaConfigurationClient}
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.federation._
import amf.shapes.client.platform.model.domain.jsonldinstance.{JsonLDArray, JsonLDElement, JsonLDObject, JsonLDScalar}
import amf.shapes.client.scala.model.domain.jsonldinstance
import amf.shapes.client.scala.model.domain.operations._
import amf.shapes.client.scala.{
  JsonLDSchemaElementClient => InternalJsonLDSchemaElementClient,
  JsonLDInstanceResult => InternalJsonLDInstanceResult,
  JsonLDSchemaResult => InternalJsonLDSchemaResult
}

trait ShapesBaseConverter
    extends VocabulariesBaseConverter
    with NilShapeConverter
    with SchemaShapeConverter
    with NodeShapeConverter
    with ScalarShapeConverter
    with FileShapeConverter
    with AnyShapeConverter
    with ArrayShapeConverter
    with TupleShapeConverter
    with XMLSerializerConverter
    with ExampleConverter
    with UnionShapeConverter
    with PropertyDependenciesConverter
    with SchemaDependenciesConverter
    with CreativeWorkConverter
    with IriTemplateMappingConverter
    with DiscriminatorValueMappingConverter
    with ShapesConfigurationConverter
    with SemanticSchemaConfigurationConverter
    with AMFSemanticSchemaResultConverter
    with ShapeOperationConverter
    with ShapeRequestConverter
    with ShapeResponseConverter
    with ShapeParameterConverter
    with ShapePayloadConverter
    with PropertyKeyMappingConverter
    with ExternalPropertyShapeConverter
    with KeyConverter
    with JsonSchemaDocumentConverter
    with BaseIriConverter
    with DefaultVocabularyConverter
    with CuriePrefixConverter
    with ContextMappingConverter
    with JsonLDObjectConverter
    with JsonLDArrayConverter
    with JsonLDElementConverter
    with JsonLDSchemaConfigurationConverter
    with JsonLDSchemaResultConverter
    with JsonLDInstanceResultConverter
    with JsonLDSchemaConfigurationClientConverter

trait NilShapeConverter extends PlatformSecrets {

  implicit object NilShapeMatcher extends BidirectionalMatcher[NilShape, domain.NilShape] {
    override def asClient(from: NilShape): domain.NilShape   = platform.wrap[domain.NilShape](from)
    override def asInternal(from: domain.NilShape): NilShape = from._internal
  }
}

trait SchemaShapeConverter extends PlatformSecrets {

  implicit object SchemaShapeMatcher extends BidirectionalMatcher[SchemaShape, domain.SchemaShape] {
    override def asClient(from: SchemaShape): domain.SchemaShape   = platform.wrap[domain.SchemaShape](from)
    override def asInternal(from: domain.SchemaShape): SchemaShape = from._internal
  }
}

trait NodeShapeConverter extends PlatformSecrets {

  implicit object NodeShapeMatcher extends BidirectionalMatcher[NodeShape, domain.NodeShape] {
    override def asClient(from: NodeShape): domain.NodeShape   = platform.wrap[domain.NodeShape](from)
    override def asInternal(from: domain.NodeShape): NodeShape = from._internal
  }
}

trait ScalarShapeConverter extends PlatformSecrets {

  implicit object ScalarShapeMatcher extends BidirectionalMatcher[ScalarShape, domain.ScalarShape] {
    override def asClient(from: ScalarShape): domain.ScalarShape   = platform.wrap[domain.ScalarShape](from)
    override def asInternal(from: domain.ScalarShape): ScalarShape = from._internal
  }
}

trait FileShapeConverter extends PlatformSecrets {

  implicit object FileShapeMatcher extends BidirectionalMatcher[FileShape, domain.FileShape] {
    override def asClient(from: FileShape): domain.FileShape   = platform.wrap[domain.FileShape](from)
    override def asInternal(from: domain.FileShape): FileShape = from._internal
  }
}

trait AnyShapeConverter extends PlatformSecrets {

  implicit object AnyShapeMatcher extends BidirectionalMatcher[AnyShape, domain.AnyShape] {
    override def asClient(from: AnyShape): domain.AnyShape   = platform.wrap[domain.AnyShape](from)
    override def asInternal(from: domain.AnyShape): AnyShape = from._internal
  }
}

trait ArrayShapeConverter extends PlatformSecrets {

  implicit object ArrayShapeMatcher extends BidirectionalMatcher[ArrayShape, domain.ArrayShape] {
    override def asClient(from: ArrayShape): domain.ArrayShape   = platform.wrap[domain.ArrayShape](from)
    override def asInternal(from: domain.ArrayShape): ArrayShape = from._internal
  }
}

trait TupleShapeConverter extends PlatformSecrets {

  implicit object TupleShapeMatcher extends BidirectionalMatcher[TupleShape, domain.TupleShape] {
    override def asClient(from: TupleShape): domain.TupleShape   = platform.wrap[domain.TupleShape](from)
    override def asInternal(from: domain.TupleShape): TupleShape = from._internal
  }
}

trait XMLSerializerConverter extends PlatformSecrets {

  implicit object XMLSerializerMatcher extends BidirectionalMatcher[XMLSerializer, domain.XMLSerializer] {
    override def asClient(from: XMLSerializer): domain.XMLSerializer   = platform.wrap[domain.XMLSerializer](from)
    override def asInternal(from: domain.XMLSerializer): XMLSerializer = from._internal
  }
}

trait ExampleConverter extends PlatformSecrets {

  implicit object ExampleMatcher extends BidirectionalMatcher[Example, domain.Example] {
    override def asClient(from: Example): domain.Example   = platform.wrap[domain.Example](from)
    override def asInternal(from: domain.Example): Example = from._internal
  }
}

trait UnionShapeConverter extends PlatformSecrets {
  implicit object UnionShapeMatcher extends BidirectionalMatcher[UnionShape, domain.UnionShape] {
    override def asClient(from: UnionShape): domain.UnionShape   = platform.wrap[domain.UnionShape](from)
    override def asInternal(from: domain.UnionShape): UnionShape = from._internal
  }
}

trait PropertyDependenciesConverter extends PlatformSecrets {

  implicit object PropertyDependenciesMatcher
      extends BidirectionalMatcher[PropertyDependencies, domain.PropertyDependencies] {
    override def asClient(from: PropertyDependencies): domain.PropertyDependencies =
      platform.wrap[domain.PropertyDependencies](from)
    override def asInternal(from: domain.PropertyDependencies): PropertyDependencies = from._internal
  }
}

trait SchemaDependenciesConverter extends PlatformSecrets {

  implicit object SchemaDependenciesMatcher
      extends BidirectionalMatcher[SchemaDependencies, domain.SchemaDependencies] {
    override def asClient(from: SchemaDependencies): domain.SchemaDependencies =
      platform.wrap[domain.SchemaDependencies](from)
    override def asInternal(from: domain.SchemaDependencies): SchemaDependencies = from._internal
  }
}

trait CreativeWorkConverter extends PlatformSecrets {

  implicit object CreativeWorkMatcher extends BidirectionalMatcher[CreativeWork, domain.CreativeWork] {
    override def asClient(from: CreativeWork): domain.CreativeWork   = platform.wrap[domain.CreativeWork](from)
    override def asInternal(from: domain.CreativeWork): CreativeWork = from._internal
  }
}

trait IriTemplateMappingConverter extends PlatformSecrets {

  implicit object IriTemplateMappingConverter
      extends BidirectionalMatcher[IriTemplateMapping, domain.IriTemplateMapping] {
    override def asClient(from: IriTemplateMapping): domain.IriTemplateMapping =
      platform.wrap[domain.IriTemplateMapping](from)
    override def asInternal(from: domain.IriTemplateMapping): IriTemplateMapping = from._internal
  }
}

trait DiscriminatorValueMappingConverter extends PlatformSecrets {

  implicit object DiscriminatorValueMappingConverter
      extends BidirectionalMatcher[DiscriminatorValueMapping, domain.DiscriminatorValueMapping] {
    override def asClient(from: DiscriminatorValueMapping): domain.DiscriminatorValueMapping =
      platform.wrap[domain.DiscriminatorValueMapping](from)
    override def asInternal(from: domain.DiscriminatorValueMapping): DiscriminatorValueMapping = from._internal
  }
}

trait ShapesConfigurationConverter {
  implicit object ShapesConfigurationMatcher
      extends BidirectionalMatcher[ShapesConfiguration, ClientShapesConfiguration] {
    override def asClient(from: ShapesConfiguration): ClientShapesConfiguration = new ClientShapesConfiguration(from)

    override def asInternal(from: ClientShapesConfiguration): ShapesConfiguration = from._internal
  }
}

trait SemanticSchemaConfigurationConverter {
  implicit object SemanticSchemaConfigurationMatcher
      extends BidirectionalMatcher[SemanticJsonSchemaConfiguration, ClientSemanticJsonSchemaConfiguration] {
    override def asClient(from: SemanticJsonSchemaConfiguration): ClientSemanticJsonSchemaConfiguration =
      new ClientSemanticJsonSchemaConfiguration(from)

    override def asInternal(from: ClientSemanticJsonSchemaConfiguration): SemanticJsonSchemaConfiguration =
      from._internal
  }
}

trait AMFSemanticSchemaResultConverter {
  implicit object AMFSemanticSchemaResultMatcher
      extends BidirectionalMatcher[AMFSemanticSchemaResult, ClientAMFSemanticSchemaResult] {
    override def asInternal(from: ClientAMFSemanticSchemaResult): AMFSemanticSchemaResult = from._internal

    override def asClient(from: AMFSemanticSchemaResult): ClientAMFSemanticSchemaResult =
      new ClientAMFSemanticSchemaResult(from)
  }
}

trait ShapeOperationConverter extends PlatformSecrets {
  implicit object ShapeOperationMatcher extends BidirectionalMatcher[ShapeOperation, domain.operations.ShapeOperation] {
    override def asClient(from: ShapeOperation): domain.operations.ShapeOperation =
      platform.wrap[domain.operations.ShapeOperation](from)
    override def asInternal(from: domain.operations.ShapeOperation): ShapeOperation = from._internal
  }
}

trait ShapeRequestConverter extends PlatformSecrets {
  implicit object ShapeRequestMatcher extends BidirectionalMatcher[ShapeRequest, domain.operations.ShapeRequest] {
    override def asClient(from: ShapeRequest): domain.operations.ShapeRequest =
      platform.wrap[domain.operations.ShapeRequest](from)
    override def asInternal(from: domain.operations.ShapeRequest): ShapeRequest = from._internal
  }
}

trait ShapeResponseConverter extends PlatformSecrets {
  implicit object ShapeResponseMatcher extends BidirectionalMatcher[ShapeResponse, domain.operations.ShapeResponse] {
    override def asClient(from: ShapeResponse): domain.operations.ShapeResponse =
      platform.wrap[domain.operations.ShapeResponse](from)
    override def asInternal(from: domain.operations.ShapeResponse): ShapeResponse = from._internal
  }
}

trait ShapeParameterConverter extends PlatformSecrets {
  implicit object ShapeParameterMatcher extends BidirectionalMatcher[ShapeParameter, domain.operations.ShapeParameter] {
    override def asClient(from: ShapeParameter): domain.operations.ShapeParameter =
      platform.wrap[domain.operations.ShapeParameter](from)
    override def asInternal(from: domain.operations.ShapeParameter): ShapeParameter = from._internal
  }
}

trait ShapePayloadConverter extends PlatformSecrets {
  implicit object ShapePayloadMatcher extends BidirectionalMatcher[ShapePayload, domain.operations.ShapePayload] {
    override def asClient(from: ShapePayload): domain.operations.ShapePayload =
      platform.wrap[domain.operations.ShapePayload](from)
    override def asInternal(from: domain.operations.ShapePayload): ShapePayload = from._internal
  }
}

trait PropertyKeyMappingConverter extends PlatformSecrets {
  implicit object PropertyKeyMappingMatcher
      extends BidirectionalMatcher[PropertyKeyMapping, domain.federation.PropertyKeyMapping] {
    override def asClient(from: PropertyKeyMapping): domain.federation.PropertyKeyMapping =
      platform.wrap[domain.federation.PropertyKeyMapping](from)
    override def asInternal(from: domain.federation.PropertyKeyMapping): PropertyKeyMapping = from._internal
  }
}

trait ExternalPropertyShapeConverter extends PlatformSecrets {
  implicit object ExternalPropertyShapeMatcher
      extends BidirectionalMatcher[ExternalPropertyShape, domain.federation.ExternalPropertyShape] {
    override def asClient(from: ExternalPropertyShape): domain.federation.ExternalPropertyShape =
      platform.wrap[domain.federation.ExternalPropertyShape](from)
    override def asInternal(from: domain.federation.ExternalPropertyShape): ExternalPropertyShape = from._internal
  }
}

trait KeyConverter extends PlatformSecrets {
  implicit object KeyMatcher extends BidirectionalMatcher[federation.Key, domain.federation.Key] {
    override def asClient(from: federation.Key): domain.federation.Key   = platform.wrap[domain.federation.Key](from)
    override def asInternal(from: domain.federation.Key): federation.Key = from._internal
  }
}

trait JsonSchemaDocumentConverter extends PlatformSecrets {
  implicit object JsonSchemaDocumentMatcher extends BidirectionalMatcher[JsonSchemaDocument, ClientJsonSchemaDocument] {
    override def asClient(from: JsonSchemaDocument): ClientJsonSchemaDocument   = ClientJsonSchemaDocument(from)
    override def asInternal(from: ClientJsonSchemaDocument): JsonSchemaDocument = from._internal
  }
}

trait BaseIriConverter extends PlatformSecrets {
  implicit object BaseIriMatcher extends BidirectionalMatcher[BaseIri, domain.BaseIri] {
    override def asInternal(from: domain.BaseIri): BaseIri = from._internal

    override def asClient(from: BaseIri): domain.BaseIri = domain.BaseIri(from)
  }
}

trait DefaultVocabularyConverter extends PlatformSecrets {
  implicit object DefaultVocabularyMatcher extends BidirectionalMatcher[DefaultVocabulary, domain.DefaultVocabulary] {
    override def asInternal(from: domain.DefaultVocabulary): DefaultVocabulary = from._internal

    override def asClient(from: DefaultVocabulary): domain.DefaultVocabulary = domain.DefaultVocabulary(from)
  }
}

trait CuriePrefixConverter extends PlatformSecrets {
  implicit object CuriePrefixMatcher extends BidirectionalMatcher[CuriePrefix, domain.CuriePrefix] {
    override def asInternal(from: domain.CuriePrefix): CuriePrefix = from._internal

    override def asClient(from: CuriePrefix): domain.CuriePrefix = domain.CuriePrefix(from)
  }
}

trait ContextMappingConverter extends PlatformSecrets {
  implicit object ContextMappingMatcher extends BidirectionalMatcher[ContextMapping, domain.ContextMapping] {
    override def asInternal(from: domain.ContextMapping): ContextMapping = from._internal

    override def asClient(from: ContextMapping): domain.ContextMapping = domain.ContextMapping(from)
  }
}

trait SemanticContextConverter extends PlatformSecrets {
  implicit object SemanticContextMatcher extends BidirectionalMatcher[SemanticContext, domain.SemanticContext] {
    override def asInternal(from: domain.SemanticContext): SemanticContext = from._internal

    override def asClient(from: SemanticContext): domain.SemanticContext = domain.SemanticContext(from)
  }
}

trait JsonLDArrayConverter extends PlatformSecrets {
  implicit object JsonLDArrayConverter extends BidirectionalMatcher[jsonldinstance.JsonLDArray, JsonLDArray] {
    override def asInternal(from: JsonLDArray): jsonldinstance.JsonLDArray = from._internal

    override def asClient(from: jsonldinstance.JsonLDArray): JsonLDArray = new JsonLDArray(from)
  }
}

trait JsonLDObjectConverter extends PlatformSecrets {
  implicit object JsonLDObjectConverter extends BidirectionalMatcher[jsonldinstance.JsonLDObject, JsonLDObject] {
    override def asInternal(from: JsonLDObject): jsonldinstance.JsonLDObject = from._internal

    override def asClient(from: jsonldinstance.JsonLDObject): JsonLDObject = new JsonLDObject(from)
  }
}

trait JsonLDScalarConverter extends PlatformSecrets {
  implicit object JsonLDScalarConverter extends BidirectionalMatcher[jsonldinstance.JsonLDScalar, JsonLDScalar] {
    override def asInternal(from: JsonLDScalar): jsonldinstance.JsonLDScalar = from._internal

    override def asClient(from: jsonldinstance.JsonLDScalar): JsonLDScalar = new JsonLDScalar(from)
  }
}

trait JsonLDElementConverter extends PlatformSecrets {
  implicit object JsonLDElementConverter extends BidirectionalMatcher[jsonldinstance.JsonLDElement, JsonLDElement] {
    override def asInternal(from: JsonLDElement): jsonldinstance.JsonLDElement = from._internal

    override def asClient(from: jsonldinstance.JsonLDElement): JsonLDElement = fromMatch(from)

    private def fromMatch(from: jsonldinstance.JsonLDElement): JsonLDElement = from match {
      case obj: jsonldinstance.JsonLDObject    => new JsonLDObject(obj)
      case array: jsonldinstance.JsonLDArray   => new JsonLDArray(array)
      case scalar: jsonldinstance.JsonLDScalar => new JsonLDScalar(scalar)
    }

  }

}

trait JsonLDSchemaResultConverter extends PlatformSecrets {
  implicit object JsonLDSchemaResultConverter
      extends BidirectionalMatcher[InternalJsonLDSchemaResult, JsonLDSchemaResult] {
    override def asInternal(from: JsonLDSchemaResult): InternalJsonLDSchemaResult = from._internal

    override def asClient(from: InternalJsonLDSchemaResult): JsonLDSchemaResult = new JsonLDSchemaResult(from)
  }
}

trait JsonLDInstanceResultConverter extends PlatformSecrets {
  implicit object JsonLDInstanceResultConverter
      extends BidirectionalMatcher[InternalJsonLDInstanceResult, JsonLDInstanceResult] {
    override def asInternal(from: JsonLDInstanceResult): InternalJsonLDInstanceResult = from._internal

    override def asClient(from: InternalJsonLDInstanceResult): JsonLDInstanceResult = new JsonLDInstanceResult(from)
  }
}

trait JsonLDSchemaConfigurationConverter extends PlatformSecrets{
  implicit object JsonLDSchemaConfigurationConverter extends BidirectionalMatcher[InternalJsonLDSchemaConfiguration, JsonLDSchemaConfiguration]{
    override def asInternal(from: JsonLDSchemaConfiguration): InternalJsonLDSchemaConfiguration = from._internal

    override def asClient(from: InternalJsonLDSchemaConfiguration): JsonLDSchemaConfiguration =
      new JsonLDSchemaConfiguration(from)
  }

  implicit object JsonLDSchemaElementClientConverter
      extends BidirectionalMatcher[InternalJsonLDSchemaElementClient, JsonLDSchemaElementClient] {
    override def asInternal(from: JsonLDSchemaElementClient): InternalJsonLDSchemaElementClient = from._internal

    override def asClient(from: InternalJsonLDSchemaElementClient): JsonLDSchemaElementClient =
      new JsonLDSchemaElementClient(from)
  }
}

trait JsonLDSchemaConfigurationClientConverter extends PlatformSecrets{
  implicit object JsonLDSchemaConfigurationClientConverter extends BidirectionalMatcher[InternalJsonLDSchemaConfigurationClient, JsonLDSchemaConfigurationClient]{
    override def asInternal(from: JsonLDSchemaConfigurationClient): InternalJsonLDSchemaConfigurationClient = from._internal

    override def asClient(from: InternalJsonLDSchemaConfigurationClient): JsonLDSchemaConfigurationClient = new JsonLDSchemaConfigurationClient(from)
  }
}
