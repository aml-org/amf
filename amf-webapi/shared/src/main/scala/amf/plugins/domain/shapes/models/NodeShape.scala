package amf.plugins.domain.shapes.models

import amf.core.model.{BoolField, IntField, StrField}
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
import amf.plugins.domain.shapes.metamodel.NodeShapeModel._
import org.yaml.model.YPart
import amf.core.utils.Strings

/**
  * Node shape.
  */
case class NodeShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {

  def minProperties: IntField                 = fields.field(MinProperties)
  def maxProperties: IntField                 = fields.field(MaxProperties)
  def closed: BoolField                       = fields.field(Closed)
  def discriminator: StrField                 = fields.field(Discriminator)
  def discriminatorValue: StrField            = fields.field(DiscriminatorValue)
  def properties: Seq[PropertyShape]          = fields.field(Properties)
  def dependencies: Seq[PropertyDependencies] = fields.field(Dependencies)

  def withMinProperties(min: Int): this.type                               = set(MinProperties, min)
  def withMaxProperties(max: Int): this.type                               = set(MaxProperties, max)
  def withClosed(closed: Boolean): this.type                               = set(Closed, closed)
  def withDiscriminator(discriminator: String): this.type                  = set(Discriminator, discriminator)
  def withDiscriminatorValue(value: String): this.type                     = set(DiscriminatorValue, value)
  def withProperties(properties: Seq[PropertyShape]): this.type            = setArray(Properties, properties)
  def withDependencies(dependencies: Seq[PropertyDependencies]): this.type = setArray(Dependencies, dependencies)

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

  override def adopted(parent: String): this.type = {
    simpleAdoption(parent)
    properties.foreach(_.adopted(id))
    this
  }

  override def linkCopy(): NodeShape = NodeShape().withId(id)

  override def meta = NodeShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.option().getOrElse("default-node").urlComponentEncoded

  override def copyShape(): NodeShape = NodeShape(fields, annotations).withId(id)
}

object NodeShape {

  def apply(): NodeShape = apply(Annotations())

  def apply(ast: YPart): NodeShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NodeShape = NodeShape(Fields(), annotations)
}
