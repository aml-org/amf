package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.AnyDefaultType
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
    AnnotationParser(response, map).parse()

  }
}

case class Raml08ResponseParser(entry: YMapEntry, producer: (String) => Response, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlResponseParser(entry, producer, parseOptional) {
  override protected def parseMap(response: Response, map: YMap): Unit = Unit
}

abstract class RamlResponseParser(entry: YMapEntry, producer: (String) => Response, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends SpecParserOps {

  protected def parseMap(response: Response, map: YMap)

  def parse(): Response = {
    val node     = ScalarNode(entry.key).text()
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
        map.key("description", (ResponseModel.Description in response).allowingAnnotations)

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

        map.key(
          "body",
          entry => {
            val payloads = mutable.ListBuffer[Payload]()

            val payload = Payload()
            payload.adopted(response.id)

            entry.value.tagType match {
              case YType.Null =>
                ctx.factory
                  .typeParser(entry, shape => shape.withName("default").adopted(payload.id), false, AnyDefaultType)
                  .parse()
                  .foreach { schema =>
                    schema.annotations += SynthesizedField()
                    payloads += payload.withSchema(schema)
                  }
                response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))

              case YType.Str =>
                ctx.factory
                  .typeParser(entry, shape => shape.withName("default").adopted(payload.id), false, AnyDefaultType)
                  .parse()
                  .foreach(payloads += payload.withSchema(_))
                response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))

              case _ =>
                // Now we parsed potentially nested shapes for different data types
                entry.value.to[YMap] match {
                  case Right(m) =>
                    m.regex(
                      ".*/.*",
                      entries => {
                        entries.foreach(entry => {
                          payloads += ctx.factory.payloadParser(entry, response.withPayload, false).parse()
                        })
                      }
                    )
                    val others = YMap(m.entries.filter(e => !e.key.toString().matches(".*/.*")))
                    if (others.entries.nonEmpty) {
                      if (payloads.isEmpty) {
                        ctx.factory
                          .typeParser(entry,
                                      shape => shape.withName("default").adopted(response.id),
                                      false,
                                      AnyDefaultType)
                          .parse()
                          .foreach(payloads += response.withPayload(None).withSchema(_)) // todo
                      } else {
                        others.entries.foreach(e =>
                          ctx.violation(s"Unexpected key '${e.key}'. Expecting valid media types.", Some(e)))
                      }
                    }
                  case _ =>
                }
                if (payloads.nonEmpty)
                  response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
            }
          }
        )

        val examples = OasResponseExamplesParser("(examples)", map).parse()
        if (examples.nonEmpty) response.set(ResponseModel.Examples, AmfArray(examples))

        ctx.closedShape(response.id, map, "response")

        parseMap(response, map)
    }

    response
  }
}
