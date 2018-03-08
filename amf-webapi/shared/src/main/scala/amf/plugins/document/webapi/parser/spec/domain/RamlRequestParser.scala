package amf.plugins.document.webapi.parser.spec.domain

import amf.core.model.domain.{AmfArray, DomainElement}
import amf.core.parser.{Annotations, _}
import amf.core.utils.Lazy
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.SpecParserOps
import amf.plugins.document.webapi.parser.spec.declaration.{AnyDefaultType, Raml10TypeParser}
import amf.plugins.domain.webapi.metamodel.RequestModel
import amf.plugins.domain.webapi.models.{Payload, Request}
import org.yaml.model.{YMap, YType}

import scala.collection.mutable

/**
  *
  */
case class Raml10RequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlRequestParser(map, producer, parseOptional) {

  override protected val baseUriParameterKey: String = "(baseUriParameters)"

  override def parse(request: Lazy[Request], target: Target): Unit = {

    map.key(
      "queryString",
      queryEntry => {
        Raml10TypeParser(queryEntry, (shape) => shape.adopted(request.getOrCreate.id))
          .parse()
          .map(q => request.getOrCreate.withQueryString(q))
      }
    )
  }
}

case class Raml08RequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlRequestParser(map, producer, parseOptional) {

  override protected val baseUriParameterKey: String = "baseUriParameters"

  override def parse(request: Lazy[Request], target: Target): Unit = Unit
}

abstract class RamlRequestParser(map: YMap, producer: () => Request, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends SpecParserOps {
  protected val request = new Lazy[Request](producer)

  protected val baseUriParameterKey: String

  def parse(request: Lazy[Request], target: Target): Unit

  def parse(): Option[Request] = {

    val target = new Target {
      override def foreach(fn: DomainElement => Unit): Unit = fn(request.getOrCreate)
    }

    map.key(
      "queryParameters",
      (RequestModel.QueryParameters in target using RamlQueryParameterParser
        .parse((name: String) => request.getOrCreate.withQueryParameter(name), parseOptional)).treatMapAsArray.optional
    )
    map.key(
      "headers",
      (RequestModel.Headers in target using RamlHeaderParser
        .parse((name: String) => request.getOrCreate.withHeader(name), parseOptional)).treatMapAsArray.optional
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

    map.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        entry.value.tagType match {
          case YType.Null =>
            ctx.factory
              .typeParser(entry,
                          shape => shape.withName("default").adopted(request.getOrCreate.id),
                          false,
                          AnyDefaultType)
              .parse()
              .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) // todo

          case YType.Str =>
            ctx.factory
              .typeParser(entry,
                          shape => shape.withName("default").adopted(request.getOrCreate.id),
                          false,
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
                      payloads += ctx.factory.payloadParser(entry, request.getOrCreate.withPayload, false).parse()
                    })
                  }
                )
                val others = YMap(m.entries.filter(e => !e.key.toString().matches(".*/.*")))
                if (others.entries.nonEmpty) {
                  if (payloads.isEmpty) {
                    ctx.factory
                      .typeParser(entry,
                                  shape => shape.withName("default").adopted(request.getOrCreate.id),
                                  false,
                                  AnyDefaultType)
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
    parse(request, target)

    request.option
  }
}
