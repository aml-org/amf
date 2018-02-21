package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec.declaration.{AnyDefaultType, Raml10TypeParser}
import amf.plugins.domain.webapi.metamodel.{RequestModel, ResponseModel}
import amf.plugins.domain.webapi.models.{Parameter, Payload, Response}
import org.yaml.model.{YMap, YMapEntry, YType}

import scala.collection.mutable

/**
  *
  */
case class Raml10ResponseParser(entry: YMapEntry, producer: (String) => Response, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlResponseParser(entry, producer, parseOptional) {

  override def parseMap(response: Response, map: YMap): Unit = {
    map.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        val payload = Payload()
        payload.adopted(response.id)

        entry.value.tagType match {
          case YType.Null =>
            Raml10TypeParser(entry,
                             shape => shape.withName("default").adopted(payload.id),
                             isAnnotation = false,
                             AnyDefaultType)
              .parse()
              .foreach { schema =>
                schema.annotations += SynthesizedField()
                payloads += payload.withSchema(schema)
              }
            response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))

          case YType.Str =>
            Raml10TypeParser(entry,
                             shape => shape.withName("default").adopted(payload.id),
                             isAnnotation = false,
                             AnyDefaultType)
              .parse()
              .foreach(payloads += payload.withSchema(_))
            response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))

          case _ =>
            // This is the case where the body is directly a data type
            entry.value.to[YMap] match {
              case Right(bodyMap) =>
                val filterMap = YMap(bodyMap.entries.filter(e => !e.key.toString().matches(".*/.*")))
                if (filterMap.entries.nonEmpty) {
                  Raml10TypeParser(entry,
                                   shape => shape.withName("default").adopted(payload.id),
                                   isAnnotation = false,
                                   AnyDefaultType)
                    .parse()
                    .foreach(payloads += payload.withSchema(_))
                }
              case _ =>
            }

            // Now we parsed potentially nested shapes for different data types
            entry.value.to[YMap] match {
              case Right(m) =>
                m.regex(
                  ".*/.*",
                  entries => {
                    entries.foreach(entry => {
                      payloads += Raml10PayloadParser(entry, response.withPayload).parse()
                    })
                  }
                )
              case _ =>
            }
            if (payloads.nonEmpty)
              response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
        }
      }
    )

    val examples = OasResponseExamplesParser("(examples)", map).parse()
    if (examples.nonEmpty) response.set(ResponseModel.Examples, AmfArray(examples))

    AnnotationParser(response, map).parse()

  }
}

case class Raml08ResponseParser(entry: YMapEntry, producer: (String) => Response, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlResponseParser(entry, producer, parseOptional) {

  override protected def parseMap(response: Response, map: YMap): Unit = {
    Raml08BodyContentParser(map, (value: Option[String]) => response.withPayload(value), () => response, parseOptional)
      .parse()
  }
}

abstract class RamlResponseParser(entry: YMapEntry, producer: (String) => Response, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext) {

  protected def parseMap(response: Response, map: YMap)

  def parse(): Response = {
    val node     = ValueNode(entry.key).text()
    val response = producer(node.toString).add(Annotations(entry)).set(ResponseModel.StatusCode, node)

    if (parseOptional && node.toString.endsWith("?")) {
      response.set(ResponseModel.Optional, value = true)
      val name = node.toString.stripSuffix("?")
      response.set(ResponseModel.Name, name)
      response.set(ResponseModel.StatusCode, name)
    }

    entry.value.to[YMap] match {
      case Left(_) =>
      case Right(map) =>
        map.key("description", entry => {
          val value = ValueNode(entry.value)
          response.set(ResponseModel.Description, value.string(), Annotations(entry))
        })

        map.key(
          "headers",
          entry => {
            val parameters: Seq[Parameter] =
              RamlParametersParser(entry.value.as[YMap], response.withHeader, parseOptional)
                .parse()
                .map(_.withBinding("header"))
            response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
          }
        )

        ctx.closedShape(response.id, map, "response")

        parseMap(response, map)
    }

    response
  }
}
