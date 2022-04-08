package amf.shapes.internal.convert

import amf.aml.internal.convert.VocabulariesBaseConverter
import amf.core.internal.convert.{BidirectionalMatcher, CoreBaseConverter}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.platform.model.domain
import amf.shapes.client.platform.{ShapesConfiguration => ClientShapesConfiguration}
import amf.shapes.client.platform.config.{SemanticJsonSchemaConfiguration => ClientSemanticJsonSchemaConfiguration}
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.config.{AMFSemanticSchemaResult, SemanticJsonSchemaConfiguration}
import amf.shapes.client.platform.config.{AMFSemanticSchemaResult => ClientAMFSemanticSchemaResult}
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter, ShapePayload, ShapeRequest, ShapeResponse}

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