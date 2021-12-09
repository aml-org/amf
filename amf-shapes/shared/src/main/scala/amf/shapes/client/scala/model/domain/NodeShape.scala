package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.operations.ShapeOperation
import amf.shapes.internal.domain.metamodel.NodeShapeModel._
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, NodeShapeModel}
import org.yaml.model.YPart

/**
  * Node shape.
  */
case class NodeShape private[amf] (override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {

  def isAbstract: BoolField                         = fields.field(IsAbstract)
  def minProperties: IntField                       = fields.field(MinProperties)
  def maxProperties: IntField                       = fields.field(MaxProperties)
  def closed: BoolField                             = fields.field(Closed)
  def discriminator: StrField                       = fields.field(Discriminator)
  def discriminatorValue: StrField                  = fields.field(DiscriminatorValue)
  def discriminatorMapping: Seq[IriTemplateMapping] = fields.field(DiscriminatorMapping)
  def discriminatorValueMapping: Seq[DiscriminatorValueMapping] =
    fields.field(NodeShapeModel.DiscriminatorValueMapping)
  def properties: Seq[PropertyShape]              = fields.field(Properties)
  def operations: Seq[ShapeOperation]              = fields.field(Operations)
  def dependencies: Seq[PropertyDependencies]     = fields.field(Dependencies)
  def schemaDependencies: Seq[SchemaDependencies] = fields.field(NodeShapeModel.SchemaDependencies)
  def additionalPropertiesSchema: Shape           = fields.field(AdditionalPropertiesSchema)
  def additionalPropertiesKeySchema: Shape        = fields.field(AdditionalPropertiesKeySchema)
  def propertyNames: Shape                        = fields.field(PropertyNames)
  def unevaluatedProperties: Boolean              = fields.field(UnevaluatedProperties)
  def unevaluatedPropertiesSchema: Shape          = fields.field(UnevaluatedPropertiesSchema)

  def withIsAbstract(isAbstract: Boolean): this.type                         = set(IsAbstract, isAbstract)
  def withMinProperties(min: Int): this.type                                 = set(MinProperties, min)
  def withUnevaluatedProperties(value: Boolean): this.type                   = set(UnevaluatedProperties, value)
  def withUnevaluatedPropertiesSchema(shape: Shape): this.type               = set(UnevaluatedPropertiesSchema, shape)
  def withMaxProperties(max: Int): this.type                                 = set(MaxProperties, max)
  def withClosed(closed: Boolean): this.type                                 = set(Closed, closed)
  def withDiscriminator(discriminator: String): this.type                    = set(Discriminator, discriminator)
  def withDiscriminatorValue(value: String): this.type                       = set(DiscriminatorValue, value)
  def withDiscriminatorMapping(mappings: Seq[IriTemplateMapping]): this.type = setArray(DiscriminatorMapping, mappings)
  def discriminatorValueMapping(mappings: Seq[DiscriminatorValueMapping]): NodeShape.this.type =
    setArray(NodeShapeModel.DiscriminatorValueMapping, mappings)
  def withProperties(properties: Seq[PropertyShape]): this.type            = setArray(Properties, properties)
  def withOperations(operations: Seq[ShapeOperation]): this.type            = setArray(Operations, operations)
  def withDependencies(dependencies: Seq[PropertyDependencies]): this.type = setArray(Dependencies, dependencies)
  def withSchemaDependencies(dependencies: Seq[SchemaDependencies]): this.type =
    setArray(NodeShapeModel.SchemaDependencies, dependencies)
  def withPropertyNames(shape: Shape): this.type                 = set(PropertyNames, shape)
  def withAdditionalPropertiesSchema(shape: Shape): this.type    = set(AdditionalPropertiesSchema, shape)
  def withAdditionalPropertiesKeySchema(shape: Shape): this.type = set(AdditionalPropertiesKeySchema, shape)

  def withDependency(): PropertyDependencies = {
    val result = PropertyDependencies()
    add(Dependencies, result)
    result
  }

  def withProperty(name: String): PropertyShape = {
    val result = PropertyShape().withName(name)
    add(Properties, result)
    result
  }

  def withInheritsObject(name: String): NodeShape = {
    val result = NodeShape().withName(name)
    add(Inherits, result)
    result
  }

  def withInheritsScalar(name: String): ScalarShape = {
    val result = ScalarShape().withName(name)
    add(Inherits, result)
    result
  }

  def withOperation(name: String): ShapeOperation = {
    val result = ShapeOperation().withName(name)
    add(NodeShapeModel.Operations, result)
    result
  }

  override def adopted(parent: String, cycle: Seq[String] = Seq()): this.type = {
    val isCycle = cycle.contains(id)
    if (Option(parent).exists(_.contains("#")))
      simpleAdoption(parent)
    else
      simpleAdoption(parent + "#/")
    if (!isCycle) {
      val newCycle: Seq[String] = cycle :+ id
      reAdoptPropertiesAndDependencies(newCycle)
      examples.foreach(_.adopted(id, newCycle))
      Option(additionalPropertiesSchema).foreach { shape =>
        shape.adopted(id, newCycle)
      }
    }
    this
  }

  private def reAdoptPropertiesAndDependencies(cycle: Seq[String]): Unit = {
    properties.foreach(_.adopted(id, cycle))
    (schemaDependencies ++ dependencies).foreach { dep =>
      dep.adopted(id, cycle)
    }
  }

  override def linkCopy(): NodeShape = NodeShape().withId(id)

  override val meta: AnyShapeModel = NodeShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/shape/" + name.option().getOrElse("default-node").urlComponentEncoded

  private[amf] override val ramlSyntaxKey: String = "nodeShape"

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = NodeShape.apply
}

object NodeShape {

  def apply(): NodeShape = apply(Annotations())

  def apply(ast: YPart): NodeShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NodeShape = NodeShape(Fields(), annotations)
}
