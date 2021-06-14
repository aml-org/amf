package amf.plugins.domain.apicontract.models

import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.parser.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.plugins.domain.apicontract.metamodel.EncodingModel
import amf.plugins.domain.apicontract.metamodel.EncodingModel._
import org.yaml.model.YMap

/**
  * Encoding internal model
  */
case class Encoding(fields: Fields, annotations: Annotations) extends DomainElement {

  def propertyName: StrField   = fields.field(PropertyName)
  def contentType: StrField    = fields.field(ContentType)
  def headers: Seq[Parameter]  = fields.field(Headers)
  def style: StrField          = fields.field(Style)
  def explode: BoolField       = fields.field(Explode)
  def allowReserved: BoolField = fields.field(AllowReserved)

  def withPropertyName(propertyName: String): this.type    = set(PropertyName, propertyName)
  def withContentType(contentType: String): this.type      = set(ContentType, contentType)
  def withHeaders(headers: Seq[Parameter]): this.type      = setArray(Headers, headers)
  def withStyle(style: String): this.type                  = set(Style, style)
  def withExplode(explode: Boolean): this.type             = set(Explode, explode)
  def withAllowReserved(allowReserved: Boolean): this.type = set(AllowReserved, allowReserved)

  def withHeader(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(Headers, result)
    result
  }

  override def meta: EncodingModel.type = EncodingModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + propertyName.option().getOrElse("default-encoding").urlComponentEncoded
}

object Encoding {

  def apply(): Encoding = apply(Annotations())

  def apply(ast: YMap): Encoding = apply(Annotations(ast))

  def apply(annotations: Annotations): Encoding = new Encoding(Fields(), annotations)
}
