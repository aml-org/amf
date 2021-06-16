package amf.apicontract.client.scala.model.domain

import amf.apicontract.internal.metamodel.domain.ResponseModel
import amf.apicontract.internal.metamodel.domain.ResponseModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain._
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import org.yaml.model.YMapEntry

/**
  * Response internal model.
  */
class Response(override val fields: Fields, override val annotations: Annotations)
    extends Message(fields: Fields, annotations: Annotations) {

  def statusCode: StrField      = fields.field(StatusCode)
  def links: Seq[TemplatedLink] = fields.field(Links)

  def withStatusCode(statusCode: String): this.type   = set(StatusCode, statusCode)
  def withLinks(links: Seq[TemplatedLink]): this.type = setArray(Links, links)

  def withHeader(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(Headers, result)
    result
  }

  def withLink(name: String): TemplatedLink = {
    val result = TemplatedLink()
    result.withName(name)
    add(Links, result)
    result
  }

  def withExample(mediaType: String): Example = {
    val example = Example().withMediaType(mediaType)
    add(Examples, example)
    example
  }

  def cloneResponse(parent: String): Response = {
    val response: Response = Response(annotations)
    val cloned             = response.withName(name.value()).adopted(parent)

    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case a: AmfArray =>
            AmfArray(a.values.map {
              case p: Parameter => p.cloneParameter(cloned.id)
              case p: Payload   => p.clonePayload(cloned.id)
              case o            => o
            }, a.annotations)
          case o => o
        }

        cloned.set(f, clonedValue, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  override def meta: ResponseModel.type = ResponseModel

  override def linkCopy(): Response = Response().withId(id)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.option().getOrElse("default-response").urlComponentEncoded

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Response.apply
  override def nameField: Field                                                                 = Name
}

object Response {
  def apply(): Response = apply(Annotations())

  def apply(entry: YMapEntry): Response = apply(Annotations(entry))

  def apply(annotations: Annotations): Response = new Response(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Response = new Response(fields, annotations)
}
