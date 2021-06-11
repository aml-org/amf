package amf.plugins.domain.apicontract.models

import amf.core.metamodel.Field
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.shapes.models.{ArrayShape, ExemplifiedDomainElement, NodeShape, ScalarShape}
import amf.plugins.domain.apicontract.metamodel.PayloadModel.{Encoding => EncodingModel, _}
import amf.plugins.domain.apicontract.metamodel.{ParameterModel, PayloadModel}
import org.yaml.model.YPart

/**
  * Payload internal model.
  */
case class Payload(fields: Fields, annotations: Annotations)
    extends NamedDomainElement
    with Linkable
    with SchemaContainer
    with ExemplifiedDomainElement {

  def mediaType: StrField       = fields.field(MediaType)
  def schemaMediaType: StrField = fields.field(SchemaMediaType)
  def schema: Shape             = fields.field(Schema)
  def encodings: Seq[Encoding]  = fields.field(EncodingModel)

  def withMediaType(mediaType: String): this.type       = set(MediaType, mediaType)
  def withSchemaMediaType(mediaType: String): this.type = set(SchemaMediaType, mediaType)
  def withSchema(schema: Shape): this.type              = set(Schema, schema)
  def withEncodings(encoding: Seq[Encoding]): this.type = setArray(EncodingModel, encoding)

  override def setSchema(shape: Shape): Shape = {
    set(ParameterModel.Schema, shape)
    shape
  }

  def withObjectSchema(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    set(PayloadModel.Schema, node)
    node
  }

  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    set(PayloadModel.Schema, scalar)
    scalar
  }

  def withArraySchema(name: String): ArrayShape = {
    val array = ArrayShape().withName(name)
    set(PayloadModel.Schema, array)
    array
  }

  def withEncoding(name: String): Encoding = {
    val result = Encoding().withPropertyName(name)
    add(EncodingModel, result)
    result
  }

  override def linkCopy(): Payload = Payload().withId(id)

  def clonePayload(parent: String): Payload = {
    val cloned = Payload(annotations).withMediaType(mediaType.value()).adopted(parent)

    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape => s.cloneShape(None)
          case o        => o
        }

        cloned.set(f, clonedValue, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  override def meta: PayloadModel.type = PayloadModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    "/" + mediaType
      .option()
      .getOrElse(name.option().getOrElse("default"))
      .urlComponentEncoded // todo: / char of media type should be encoded?
  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Payload.apply
  override def nameField: Field                                                                 = Name
}

object Payload {
  def apply(): Payload = apply(Annotations())

  def apply(ast: YPart): Payload = apply(Annotations(ast))

  def apply(annotations: Annotations): Payload = new Payload(Fields(), annotations)
}
