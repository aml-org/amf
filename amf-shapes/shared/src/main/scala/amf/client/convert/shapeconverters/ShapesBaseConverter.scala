package amf.client.convert.shapeconverters

import amf.client.convert.{BidirectionalMatcher, CoreBaseConverter}
import amf.core.unsafe.PlatformSecrets
import amf.client.model.domain.{AnyShape => ClientAnyShape, ArrayShape => ClientArrayShape, DiscriminatorValueMapping => ClientDiscriminatorValueMapping, Example => ClientExample, FileShape => ClientFileShape, IriTemplateMapping => ClientIriTemplateMapping, NilShape => ClientNilShape, NodeShape => ClientNodeShape, PropertyDependencies => ClientPropertyDependencies, ScalarShape => ClientScalarShape, SchemaDependencies => ClientSchemaDependencies, SchemaShape => ClientSchemaShape, TupleShape => ClientTupleShape, UnionShape => ClientUnionShape, XMLSerializer => ClientXMLSerializer}
import amf.client.model.domain.{CreativeWork => ClientCreativeWork}
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.webapi.models.{DiscriminatorValueMapping, IriTemplateMapping}


trait ShapesBaseConverter
  extends CoreBaseConverter
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

trait NilShapeConverter extends PlatformSecrets {

  implicit object NilShapeMatcher extends BidirectionalMatcher[NilShape, ClientNilShape] {
    override def asClient(from: NilShape): ClientNilShape   = platform.wrap[ClientNilShape](from)
    override def asInternal(from: ClientNilShape): NilShape = from._internal
  }
}

trait SchemaShapeConverter extends PlatformSecrets {

  implicit object SchemaShapeMatcher extends BidirectionalMatcher[SchemaShape, ClientSchemaShape] {
    override def asClient(from: SchemaShape): ClientSchemaShape   = platform.wrap[ClientSchemaShape](from)
    override def asInternal(from: ClientSchemaShape): SchemaShape = from._internal
  }
}

trait NodeShapeConverter extends PlatformSecrets {

  implicit object NodeShapeMatcher extends BidirectionalMatcher[NodeShape, ClientNodeShape] {
    override def asClient(from: NodeShape): ClientNodeShape   = platform.wrap[ClientNodeShape](from)
    override def asInternal(from: ClientNodeShape): NodeShape = from._internal
  }
}

trait ScalarShapeConverter extends PlatformSecrets {

  implicit object ScalarShapeMatcher extends BidirectionalMatcher[ScalarShape, ClientScalarShape] {
    override def asClient(from: ScalarShape): ClientScalarShape   = platform.wrap[ClientScalarShape](from)
    override def asInternal(from: ClientScalarShape): ScalarShape = from._internal
  }
}

trait FileShapeConverter extends PlatformSecrets {

  implicit object FileShapeMatcher extends BidirectionalMatcher[FileShape, ClientFileShape] {
    override def asClient(from: FileShape): ClientFileShape   = platform.wrap[ClientFileShape](from)
    override def asInternal(from: ClientFileShape): FileShape = from._internal
  }
}

trait AnyShapeConverter extends PlatformSecrets {

  implicit object AnyShapeMatcher extends BidirectionalMatcher[AnyShape, ClientAnyShape] {
    override def asClient(from: AnyShape): ClientAnyShape   = platform.wrap[ClientAnyShape](from)
    override def asInternal(from: ClientAnyShape): AnyShape = from._internal
  }
}

trait ArrayShapeConverter extends PlatformSecrets {

  implicit object ArrayShapeMatcher extends BidirectionalMatcher[ArrayShape, ClientArrayShape] {
    override def asClient(from: ArrayShape): ClientArrayShape   = platform.wrap[ClientArrayShape](from)
    override def asInternal(from: ClientArrayShape): ArrayShape = from._internal
  }
}

trait TupleShapeConverter extends PlatformSecrets {

  implicit object TupleShapeMatcher extends BidirectionalMatcher[TupleShape, ClientTupleShape] {
    override def asClient(from: TupleShape): ClientTupleShape   = platform.wrap[ClientTupleShape](from)
    override def asInternal(from: ClientTupleShape): TupleShape = from._internal
  }
}

trait XMLSerializerConverter extends PlatformSecrets {

  implicit object XMLSerializerMatcher extends BidirectionalMatcher[XMLSerializer, ClientXMLSerializer] {
    override def asClient(from: XMLSerializer): ClientXMLSerializer   = platform.wrap[ClientXMLSerializer](from)
    override def asInternal(from: ClientXMLSerializer): XMLSerializer = from._internal
  }
}

trait ExampleConverter extends PlatformSecrets {

  implicit object ExampleMatcher extends BidirectionalMatcher[Example, ClientExample] {
    override def asClient(from: Example): ClientExample   = platform.wrap[ClientExample](from)
    override def asInternal(from: ClientExample): Example = from._internal
  }
}

trait UnionShapeConverter extends PlatformSecrets {
  implicit object UnionShapeMatcher extends BidirectionalMatcher[UnionShape, ClientUnionShape] {
    override def asClient(from: UnionShape): ClientUnionShape   = platform.wrap[ClientUnionShape](from)
    override def asInternal(from: ClientUnionShape): UnionShape = from._internal
  }
}

trait PropertyDependenciesConverter extends PlatformSecrets {

  implicit object PropertyDependenciesMatcher
    extends BidirectionalMatcher[PropertyDependencies, ClientPropertyDependencies] {
    override def asClient(from: PropertyDependencies): ClientPropertyDependencies =
      platform.wrap[ClientPropertyDependencies](from)
    override def asInternal(from: ClientPropertyDependencies): PropertyDependencies = from._internal
  }
}

trait SchemaDependenciesConverter extends PlatformSecrets {

  implicit object SchemaDependenciesMatcher
    extends BidirectionalMatcher[SchemaDependencies, ClientSchemaDependencies] {
    override def asClient(from: SchemaDependencies): ClientSchemaDependencies =
      platform.wrap[ClientSchemaDependencies](from)
    override def asInternal(from: ClientSchemaDependencies): SchemaDependencies = from._internal
  }
}

trait CreativeWorkConverter extends PlatformSecrets {

  implicit object CreativeWorkMatcher extends BidirectionalMatcher[CreativeWork, ClientCreativeWork] {
    override def asClient(from: CreativeWork): ClientCreativeWork   = platform.wrap[ClientCreativeWork](from)
    override def asInternal(from: ClientCreativeWork): CreativeWork = from._internal
  }
}

trait IriTemplateMappingConverter extends PlatformSecrets {

  implicit object IriTemplateMappingConverter
    extends BidirectionalMatcher[IriTemplateMapping, ClientIriTemplateMapping] {
    override def asClient(from: IriTemplateMapping): ClientIriTemplateMapping =
      platform.wrap[ClientIriTemplateMapping](from)
    override def asInternal(from: ClientIriTemplateMapping): IriTemplateMapping = from._internal
  }
}

trait DiscriminatorValueMappingConverter extends PlatformSecrets {

  implicit object DiscriminatorValueMappingConverter
    extends BidirectionalMatcher[DiscriminatorValueMapping, ClientDiscriminatorValueMapping] {
    override def asClient(from: DiscriminatorValueMapping): ClientDiscriminatorValueMapping =
      platform.wrap[ClientDiscriminatorValueMapping](from)
    override def asInternal(from: ClientDiscriminatorValueMapping): DiscriminatorValueMapping = from._internal
  }
}