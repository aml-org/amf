package amf.plugins.domain.shapes.models

import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.model.{BoolField, IntField, StrField}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.shapes.metamodel.NodeShapeModel._
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, NodeShapeModel}
import amf.plugins.domain.webapi.models.IriTemplateMapping
import org.yaml.model.YPart

/**
  * Node shape.
  */
case class NodeShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {

  def minProperties: IntField                       = fields.field(MinProperties)
  def maxProperties: IntField                       = fields.field(MaxProperties)
  def closed: BoolField                             = fields.field(Closed)
  def discriminator: StrField                       = fields.field(Discriminator)
  def discriminatorValue: StrField                  = fields.field(DiscriminatorValue)
  def discriminatorMapping: Seq[IriTemplateMapping] = fields.field(DiscriminatorMapping)
  def properties: Seq[PropertyShape]                = fields.field(Properties)
  def dependencies: Seq[PropertyDependencies]       = fields.field(Dependencies)
  def additionalPropertiesSchema: Shape             = fields.field(AdditionalPropertiesSchema)
  def propertyNames: Shape                          = fields.field(PropertyNames)

  def withMinProperties(min: Int): this.type                                 = set(MinProperties, min)
  def withMaxProperties(max: Int): this.type                                 = set(MaxProperties, max)
  def withClosed(closed: Boolean): this.type                                 = set(Closed, closed)
  def withDiscriminator(discriminator: String): this.type                    = set(Discriminator, discriminator)
  def withDiscriminatorValue(value: String): this.type                       = set(DiscriminatorValue, value)
  def withDiscriminatorMapping(mappings: Seq[IriTemplateMapping]): this.type = setArray(DiscriminatorMapping, mappings)
  def withProperties(properties: Seq[PropertyShape]): this.type              = setArray(Properties, properties)
  def withDependencies(dependencies: Seq[PropertyDependencies]): this.type   = setArray(Dependencies, dependencies)
  def withPropertyNames(shape: Shape): this.type                             = set(PropertyNames, shape)
  def withAdditionalPropertiesSchema(shape: Shape): this.type                = set(AdditionalPropertiesSchema, shape)

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

  override def adopted(parent: String, cycle: Seq[String] = Seq()): this.type = {
    val isCycle = cycle.contains(id)
    if (Option(parent).exists(_.contains("#")))
      simpleAdoption(parent)
    else
      simpleAdoption(parent + "#/")
    if (!isCycle) {
      val newCycle: Seq[String] = cycle :+ id
      properties.foreach(_.adopted(id, newCycle))
      examples.foreach(_.adopted(id, newCycle))
      Option(additionalPropertiesSchema).foreach { shape =>
        shape.adopted(id, newCycle)
      }
    }
    this
  }

  override def linkCopy(): NodeShape = NodeShape().withId(id)

  override val meta: AnyShapeModel = NodeShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.option().getOrElse("default-node").urlComponentEncoded

  override val ramlSyntaxKey: String = "nodeShape"

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = NodeShape.apply
}

object NodeShape {

  def apply(): NodeShape = apply(Annotations())

  def apply(ast: YPart): NodeShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NodeShape = NodeShape(Fields(), annotations)
}
