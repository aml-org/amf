package amf.spec.domain

import amf.domain.Annotation.SynthesizedField
import amf.domain.{Annotations, Parameter, Payload, Response}
import amf.metadata.domain.{RequestModel, ResponseModel}
import amf.model.AmfArray
import amf.parser.YMapOps
import amf.plugins.domain.webapi.contexts.WebApiContext
import amf.spec.ParserContext
import amf.spec.common.{AnnotationParser, ValueNode}
import amf.spec.declaration.{AnyDefaultType, RamlTypeParser}
import org.yaml.model.{YMap, YMapEntry, YType}

import scala.collection.mutable

/**
  *
  */
case class RamlResponseParser(entry: YMapEntry, producer: (String) => Response)(implicit ctx: WebApiContext) {
  def parse(): Response = {

    val node = ValueNode(entry.key).text()

    val response = producer(node.value.toString).add(Annotations(entry)).set(ResponseModel.StatusCode, node)

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
              RamlParametersParser(entry.value.as[YMap], response.withHeader)
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
                RamlTypeParser(entry, shape => shape.withName("default").adopted(payload.id), isAnnotation = false, AnyDefaultType)
                  .parse()
                  .foreach { schema =>
                    schema.annotations += SynthesizedField()
                    payloads += payload.withSchema(schema)
                  }
                response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))

              case _ =>
                RamlTypeParser(entry, shape => shape.withName("default").adopted(payload.id), isAnnotation = false, AnyDefaultType)
                  .parse()
                  .foreach(payloads += payload.withSchema(_))

                entry.value.to[YMap] match {
                  case Right(m) =>
                    m.regex(
                      ".*/.*",
                      entries => {
                        entries.foreach(entry => {
                          payloads += RamlPayloadParser(entry, response.withPayload).parse()
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

        ctx.closedShape(response.id, map, "response")

        AnnotationParser(() => response, map).parse()
    }

    response
  }
}
