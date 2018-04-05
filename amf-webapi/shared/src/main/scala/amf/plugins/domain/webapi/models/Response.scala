package amf.plugins.domain.webapi.models

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.{AmfArray, DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.metamodel.ResponseModel
import amf.plugins.domain.webapi.metamodel.ResponseModel._

/**
  * Response internal model.
  */
case class Response(fields: Fields, annotations: Annotations) extends DomainElement with Linkable {

  def name: StrField            = fields.field(Name)
  def description: StrField     = fields.field(Description)
  def statusCode: StrField      = fields.field(StatusCode)
  def headers: Seq[Parameter]   = fields.field(Headers)
  def payloads: Seq[Payload]    = fields.field(Payloads)
  def examples: Seq[Example]    = fields.field(Examples)
  def links: Seq[TemplatedLink] = fields.field(Links)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withStatusCode(statusCode: String): this.type   = set(StatusCode, statusCode)
  def withHeaders(headers: Seq[Parameter]): this.type = setArray(Headers, headers)
  def withPayloads(payloads: Seq[Payload]): this.type = setArray(Payloads, payloads)
  def withExamples(examples: Seq[Example]): this.type = setArray(Examples, examples)
  def withLinks(links: Seq[TemplatedLink]): this.type = setArray(Links, links)

  def withHeader(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(Headers, result)
    result
  }

  def withPayload(mediaType: Option[String] = None): Payload = {
    val result = Payload()
    mediaType.map(result.withMediaType)
    add(Payloads, result)
    result
  }

  def withExample(mediaType: String): Example = {
    val example = Example().withMediaType(mediaType)
    add(Examples, example)
    example
  }

  def cloneResponse(parent: String): Response = {
    val cloned = Response(annotations).withName(name.value()).adopted(parent)

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

  override def meta: Obj = ResponseModel

  override def linkCopy(): Linkable = Response().withId(id)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.value()
}

object Response {
  def apply(): Response = apply(Annotations())

  def apply(annotations: Annotations): Response = new Response(Fields(), annotations)
}
