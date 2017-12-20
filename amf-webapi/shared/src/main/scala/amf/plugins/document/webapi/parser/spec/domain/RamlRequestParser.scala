package amf.plugins.document.webapi.parser.spec.domain

import amf.core.model.domain.{AmfArray, DomainElement}
import amf.core.parser.{Annotations, _}
import amf.core.utils.Lazy
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.Raml10TypeParser
import amf.plugins.domain.webapi.metamodel.RequestModel
import amf.plugins.domain.webapi.models.{Parameter, Payload, Request}
import org.yaml.model.YMap

import scala.collection.mutable

/**
  *
  */
case class Raml10RequestParser(map: YMap, producer: () => Request)(implicit ctx: WebApiContext)
    extends RamlRequestParser(map, producer) {

  override def parse(): Option[Request] = {
    super.parse()

    map.key(
      "queryString",
      queryEntry => {
        Raml10TypeParser(queryEntry, (shape) => shape.adopted(request.getOrCreate.id))
          .parse()
          .map(q => request.getOrCreate.withQueryString(q))
      }
    )

    map.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        val bodyMap = entry.value.as[YMap]
        Raml10TypeParser(entry, shape => shape.withName("default").adopted(request.getOrCreate.id))
          .parse()
          .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) // todo

        entry.value
          .as[YMap]
          .regex(
            ".*/.*",
            entries => {
              entries.foreach(entry => {
                payloads += Raml10PayloadParser(entry, producer = request.getOrCreate.withPayload)
                  .parse()
              })
            }
          )
        if (payloads.nonEmpty)
          request.getOrCreate
            .set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )

    request.option
  }

  override def parameterParser: (YMap, (String) => Parameter) => RamlParametersParser = Raml10ParametersParser.apply
}

case class Raml08RequestParser(map: YMap, producer: () => Request)(implicit ctx: WebApiContext)
    extends RamlRequestParser(map, producer) {
  override def parse(): Option[Request] = {
    super.parse()
    Raml08BodyContentParser(map,
                            (value: Option[String]) => request.getOrCreate.withPayload(value),
                            () => request.getOrCreate).parse()

    request.option
  }

  override def parameterParser: (YMap, (String) => Parameter) => RamlParametersParser = Raml08ParametersParser.apply
}

case class Raml08BodyContentParser(map: YMap, producer: (Option[String] => Payload), accesor: () => DomainElement)(
    implicit ctx: WebApiContext) {
  def parse(): Unit = {
    val payloads = mutable.ListBuffer[Payload]()

    map.key(
      "body",
      entry => {
        entry.value
          .as[YMap]
          .regex(
            ".*/.*",
            entries => {
              entries.foreach(entry => {
                payloads += Raml08PayloadParser(entry, producer = producer)
                  .parse()
              })
            }
          )
        if (payloads.nonEmpty)
          accesor()
            .set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )
  }
}

abstract class RamlRequestParser(map: YMap, producer: () => Request)(implicit ctx: WebApiContext) {
  protected val request = new Lazy[Request](producer)

  def parameterParser: (YMap, (String) => Parameter) => RamlParametersParser

  def parse(): Option[Request] = {

    map.key(
      "queryParameters",
      entry => {

        val parameters: Seq[Parameter] =
          parameterParser(entry.value.as[YMap], request.getOrCreate.withQueryParameter)
            .parse()
            .map(_.withBinding("query"))
        request.getOrCreate.set(RequestModel.QueryParameters,
                                AmfArray(parameters, Annotations(entry.value)),
                                Annotations(entry))
      }
    )

    map.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] =
          parameterParser(entry.value.as[YMap], request.getOrCreate.withHeader)
            .parse()
            .map(_.withBinding("header"))
        request.getOrCreate.set(RequestModel.Headers,
                                AmfArray(parameters, Annotations(entry.value)),
                                Annotations(entry))
      }
    )

    // this has already being parsed in the endpoint
    // AnnotationParser(() => request.getOrCreate, map).parse()

    request.option
  }
}
