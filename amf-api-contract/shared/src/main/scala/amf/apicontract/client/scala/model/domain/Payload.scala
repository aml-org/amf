package amf.apicontract.client.scala.model.domain

import amf.apicontract.internal.metamodel.domain.PayloadModel.{Encoding => EncodingModel, _}
import amf.apicontract.internal.metamodel.domain.{ParameterModel, PayloadModel}
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.ExemplifiedDomainElement
import amf.shapes.client.scala.model.domain.operations.AbstractPayload
import amf.shapes.internal.domain.resolution.ExampleTracking
import org.yaml.model.YPart

/** Payload internal model.
  */
case class Payload(override val fields: Fields, override val annotations: Annotations)
    extends AbstractPayload(fields, annotations)
    with SchemaContainer
    with ExemplifiedDomainElement {

  def schemaMediaType: StrField = fields.field(SchemaMediaType)
  def encodings: Seq[Encoding]  = fields.field(EncodingModel)

  def withSchemaMediaType(mediaType: String): this.type = set(SchemaMediaType, mediaType)
  def withEncodings(encoding: Seq[Encoding]): this.type = setArray(EncodingModel, encoding)

  override def setSchema(shape: Shape): Shape = {
    set(ParameterModel.Schema, shape)
    shape
  }

  def withEncoding(name: String): Encoding = {
    val result = Encoding().withPropertyName(name)
    add(EncodingModel, result)
    result
  }

  override def linkCopy(): Payload = Payload().withId(id)

  def clonePayload(parent: String): Payload = {
    val cloned = Payload(annotations).withMediaType(mediaType.value()).adopted(parent)

    this.fields.foreach { case (f, v) =>
      val clonedValue = v.value match {
        case s: Shape => s.cloneShape(None)
        case o        => o
      }

      cloned.set(f, clonedValue, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  /** Call after object has been adopted by specified parent. */
  override def adopted(parent: String, cycle: Seq[String]): Payload.this.type = {
    val oldId = this.id
    super.adopted(parent, cycle)
    if (this.id != oldId)
      ExampleTracking.replaceTracking(this.schema, this, oldId)
    this
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
