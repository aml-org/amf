package amf.plugins.document.webapi.parser.spec.domain

import amf.core.model.domain.{AmfArray, DomainElement}
import amf.core.parser.{Annotations, _}
import amf.core.utils.Lazy
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.{AnyDefaultType, Raml10TypeParser}
import amf.plugins.domain.webapi.metamodel.RequestModel
import amf.plugins.domain.webapi.models.{Parameter, Payload, Request}
import org.yaml.model.{YMap, YType}

import scala.collection.mutable

/**
  *
  */
case class Raml10RequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlRequestParser(map, producer, parseOptional) {

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

        entry.value.tagType match {
          case YType.Null =>
            Raml10TypeParser(entry,
                             shape => shape.withName("default").adopted(request.getOrCreate.id),
                             isAnnotation = false,
                             AnyDefaultType)
              .parse()
              .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) // todo

          case YType.Str =>
            Raml10TypeParser(entry,
                             shape => shape.withName("default").adopted(request.getOrCreate.id),
                             isAnnotation = false,
                             AnyDefaultType)
              .parse()
              .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) // todo

          case _ =>
            // Now we parsed potentially nested shapes for different data types
            entry.value.to[YMap] match {
              case Right(m) =>
                m.regex(
                  ".*/.*",
                  entries => {
                    entries.foreach(entry => {
                      payloads += Raml10PayloadParser(entry, producer = request.getOrCreate.withPayload).parse()
                    })
                  }
                )
                val others = YMap(m.entries.filter(e => !e.key.toString().matches(".*/.*")))
                if (others.entries.nonEmpty) {
                  if (payloads.isEmpty) {
                    Raml10TypeParser(entry,
                                     shape => shape.withName("default").adopted(request.getOrCreate.id),
                                     defaultType = AnyDefaultType)
                      .parse()
                      .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) // todo
                  } else {
                    others.entries.foreach(e =>
                      ctx.violation(s"Unexpected key '${e.key}'. Expecting valid media types.", Some(e)))
                  }
                }
              case _ =>
            }
        }

        if (payloads.nonEmpty)
          request.getOrCreate.set(RequestModel.Payloads,
                                  AmfArray(payloads, Annotations(entry.value)),
                                  Annotations(entry))
      }
    )

    request.option
  }

  override protected val baseUriParameterKey: String = "(baseUriParameters)"
}

case class Raml08RequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlRequestParser(map, producer, parseOptional) {
  override def parse(): Option[Request] = {
    super.parse()
    Raml08BodyContentParser(map,
                            (value: Option[String]) => request.getOrCreate.withPayload(value),
                            () => request.getOrCreate).parse()

    request.option
  }

  override protected val baseUriParameterKey: String = "baseUriParameters"
}

case class Raml08BodyContentParser(map: YMap,
                                   producer: (Option[String] => Payload),
                                   accessor: () => DomainElement,
                                   parseOptional: Boolean = false)(implicit ctx: RamlWebApiContext) {
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
                payloads += Raml08PayloadParser(entry, producer = producer, parseOptional)
                  .parse()
              })
            }
          )
        if (payloads.nonEmpty)
          accessor()
            .set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )
  }
}

abstract class RamlRequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext) {
  protected val request = new Lazy[Request](producer)

  protected val baseUriParameterKey: String

  def parse(): Option[Request] = {

    map.key(
      "queryParameters",
      entry => {

        val parameters: Seq[Parameter] =
          RamlParametersParser(entry.value.as[YMap], request.getOrCreate.withQueryParameter, parseOptional)
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
          RamlParametersParser(entry.value.as[YMap], request.getOrCreate.withHeader, parseOptional)
            .parse()
            .map(_.withBinding("header"))
        request.getOrCreate.set(RequestModel.Headers,
                                AmfArray(parameters, Annotations(entry.value)),
                                Annotations(entry))
      }
    )

    // BaseUriParameters here are only valid for 0.8, must support the extention in RAml 1.0
    map.key(
      baseUriParameterKey,
      entry => {
        val parameters = entry.value.as[YMap].entries.map { paramEntry =>
          Raml08ParameterParser(paramEntry, request.getOrCreate.withBaseUriParameter, parseOptional)
            .parse()
            .withBinding("path")
        }

        request.getOrCreate.set(RequestModel.BaseUriParameters,
                                AmfArray(parameters, Annotations(entry.value)),
                                Annotations(entry))

      }
    )

    request.option
  }
}
