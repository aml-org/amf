package amf.shapes.client.scala.model.domain.core

import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{CreativeWork, Example, NodeShape}
import amf.shapes.internal.domain.metamodel.core.ShapeResponseModel
import amf.shapes.internal.domain.metamodel.core.ShapeResponseModel.{Name, Payloads, StatusCode}
import org.yaml.model.YMapEntry

class ShapeResponse(override val fields: Fields, override val annotations: Annotations)
    extends NamedDomainElement
    with Linkable {

  def statusCode: StrField = fields.field(StatusCode)

  def withStatusCode(statusCode: String): this.type = set(StatusCode, statusCode)

  def payloads: Seq[ShapePayload] = fields.field(Payloads)

  def withPayloads(payloads: Seq[ShapePayload]): this.type = setArray(Payloads, payloads)
  def withPayload(mediaType: Option[String] = None): ShapePayload = {
    val result = ShapePayload()
    mediaType.map(result.withMediaType)
    add(Payloads, result)
    result
  }

  override def meta: ShapeResponseModel.type = ShapeResponseModel

  override def linkCopy(): ShapeResponse = ShapeResponse().withId(id)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/" + name.option().getOrElse("default-response").urlComponentEncoded

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = ShapeResponse.apply
  override def nameField: Field                                                                 = Name
}

object ShapeResponse {
  def apply(): ShapeResponse = apply(Annotations())

  def apply(entry: YMapEntry): ShapeResponse = apply(Annotations(entry))

  def apply(annotations: Annotations): ShapeResponse = new ShapeResponse(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): ShapeResponse = new ShapeResponse(fields, annotations)
}
