package amf.domain

import amf.framework.parser.Annotations
import amf.metadata.domain.ResponseModel._
import amf.model.AmfArray

/**
  * Response internal model.
  */
case class Response(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: String            = fields(Name)
  def description: String     = fields(Description)
  def statusCode: String      = fields(StatusCode)
  def headers: Seq[Parameter] = fields(Headers)
  def payloads: Seq[Payload]  = fields(Payloads)
  def examples: Seq[Example]  = fields(Examples)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withStatusCode(statusCode: String): this.type   = set(StatusCode, statusCode)
  def withHeaders(headers: Seq[Parameter]): this.type = setArray(Headers, headers)
  def withPayloads(payloads: Seq[Payload]): this.type = setArray(Payloads, payloads)
  def withExamples(examples: Seq[Example]): this.type = setArray(Examples, examples)

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

  override def adopted(parent: String): this.type = withId(parent + "/" + name)

  def cloneResponse(parent: String): Response = {
    val cloned = Response(annotations).withName(name).adopted(parent)

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
}

object Response {
  def apply(): Response = apply(Annotations())

  def apply(annotations: Annotations): Response = new Response(Fields(), annotations)
}
