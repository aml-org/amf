package amf.client.model.domain

import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.{WebApi => WebApiObject}
import org.scalatest.{FunSuite, Matchers}

class ShapesTest extends FunSuite with Matchers with PlatformSecrets {
  WebApiObject.register()
  CoreRegister.register(platform)

  val s                                        = "test string"
  val clientStringList: ClientList[String]     = Seq(s).asClient
  val shape                                    = new AnyShape()
  val creativeWork                             = new CreativeWork()
  val shapes: Seq[amf.core.model.domain.Shape] = Seq(shape._internal)

  test("test ScalarNode") {
    val scalarNode = new ScalarNode()
      .withName(s)
      .withValue(s)
      .withDataType(s)
    scalarNode.name.value() shouldBe s
    scalarNode.value.value() shouldBe s
    scalarNode.dataType.value() shouldBe s
  }

  test("test ScalarShape") {
    val scalarShape = new ScalarShape()
      .withDataType(s)
      .withPattern(s)
      .withMinLength(0)
      .withMaxLength(1)
      .withMinimum(0)
      .withMaximum(1)
      .withExclusiveMinimum(true)
      .withExclusiveMaximum(true)
      .withFormat(s)
      .withMultipleOf(1)
    scalarShape.dataType.value() shouldBe s
    scalarShape.pattern.value() shouldBe s
    scalarShape.minLength.value() shouldBe 0
    scalarShape.maxLength.value() shouldBe 1
    scalarShape.minimum.value() shouldBe 0
    scalarShape.maximum.value() shouldBe 1
    scalarShape.exclusiveMinimum.value() shouldBe true
    scalarShape.exclusiveMaximum.value() shouldBe true
    scalarShape.format.value() shouldBe s
    scalarShape.multipleOf.value() shouldBe 1
  }

  test("test SchemaShape") {
    val schemaShape = new SchemaShape()
      .withMediatype(s)
      .withRaw(s)
    schemaShape.mediaType.value() shouldBe s
    schemaShape.raw.value() shouldBe s
  }

  test("test AnyShape") {
    val xmlSerialization = new XMLSerializer()

    val anyShape: AnyShape = new AnyShape()
      .withDocumentation(creativeWork)
      .withXMLSerialization(xmlSerialization)
      .withComment(s)
    anyShape.documentation._internal should be(creativeWork._internal)
    anyShape.xmlSerialization._internal should be(xmlSerialization._internal)
    anyShape.comment.value() shouldBe s
  }

  test("test ArrayShape") {
    val arrayShape = new ArrayShape()
      .withItems(shape)
      .withContains(shape)
    arrayShape.items._internal shouldBe shape._internal
    arrayShape.contains._internal shouldBe shape._internal
  }

  test("test TupleShape") {
    val tuple = new TupleShape()
      .withItems(shapes.asClient)
      .withClosedItems(false)
    tuple.items.asInternal shouldBe shapes
    tuple.closedItems.value() shouldBe false
  }

  test("test UnionShape") {
    val unionShape = new UnionShape()
      .withAnyOf(shapes.asClient)
    unionShape.anyOf.asInternal shouldBe shapes
  }

  test("test FileShape") {
    val fileShape = new FileShape()
      .withFileTypes(clientStringList)
      .withPattern(s)
      .withMinLength(0)
      .withMaxLength(1)
      .withMinimum(0)
      .withMaximum(1)
      .withExclusiveMinimum(true)
      .withExclusiveMaximum(true)
      .withFormat(s)
      .withMultipleOf(1)
    fileShape.fileTypes.toString shouldBe clientStringList.toString
    fileShape.pattern.value() shouldBe s
    fileShape.minLength.value() shouldBe 0
    fileShape.maxLength.value() shouldBe 1
    fileShape.minimum.value() shouldBe 0
    fileShape.maximum.value() shouldBe 1
    fileShape.exclusiveMinimum.value() shouldBe true
    fileShape.exclusiveMaximum.value() shouldBe true
    fileShape.format.value() shouldBe s
    fileShape.multipleOf.value() shouldBe 1
  }

  test("test MatrixShape") {
    val arrayShape = new ArrayShape()

    val matrix = new MatrixShape()
      .withItems(arrayShape)
    matrix.items shouldBe arrayShape
  }

  test("test NodeShape") {
    val mapping      = Seq(new IriTemplateMapping()._internal)
    val dependencies = Seq(new PropertyDependencies()._internal)
    val properties   = Seq(new PropertyShape()._internal)

    val node = new NodeShape()
      .withMinProperties(0)
      .withMaxProperties(1)
      .withClosed(true)
      .withDiscriminator(s)
      .withDiscriminatorValue(s)
      .withDiscriminatorMapping(mapping.asClient)
      .withProperties(properties.asClient)
      .withAdditionalPropertiesSchema(shape)
      .withDependencies(dependencies.asClient)
      .withPropertyNames(shape)
    node.minProperties.value() shouldBe 0
    node.maxProperties.value() shouldBe 1
    node.closed.value() shouldBe true
    node.discriminator.value() shouldBe s
    node.discriminatorValue.value() shouldBe s
    node.discriminatorMapping.asInternal shouldBe mapping
    node.properties.asInternal shouldBe properties
    node.additionalPropertiesSchema._internal shouldBe shape._internal
    node.dependencies.asInternal shouldBe dependencies
    node.propertyNames._internal shouldBe shape._internal
  }
}
