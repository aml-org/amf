package amf.spec.domain

import amf.domain.{Annotations, Parameter, Payload, Response}
import amf.metadata.domain.{RequestModel, ResponseModel}
import amf.model.AmfArray
import amf.parser.{YMapOps, YValueOps}
import amf.spec.common.{AnnotationParser, ValueNode}
import amf.spec.declaration.RamlTypeParser
import amf.spec.{Declarations, ParserContext}
import org.yaml.model.{YMap, YMapEntry}

import scala.collection.mutable

/**
  *
  */
case class RamlResponseParser(entry: YMapEntry, producer: (String) => Response, declarations: Declarations)(
    implicit ctx: ParserContext) {
  def parse(): Response = {

    val node = ValueNode(entry.key).text()

    val response = producer(node.value.toString).add(Annotations(entry))
    val map      = entry.value.value.toMap

    response.set(ResponseModel.StatusCode, node)

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      response.set(ResponseModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] =
          RamlParametersParser(entry.value.value.toMap, response.withHeader, declarations)
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
        payload.adopted(response.id) // TODO review

        RamlTypeParser(entry, shape => shape.withName("default").adopted(payload.id), declarations)
          .parse()
          .foreach(payloads += payload.withSchema(_))

        entry.value.value match {
          case map: YMap =>
            map.regex(
              ".*/.*",
              entries => {
                entries.foreach(entry => {
                  payloads += RamlPayloadParser(entry, response.withPayload, declarations).parse()
                })
              }
            )
          case _ =>
        }
        if (payloads.nonEmpty)
          response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )

    val examples = OasResponseExamplesParser("(examples)", map).parse()
    if (examples.nonEmpty) response.set(ResponseModel.Examples, AmfArray(examples))

    ctx.closedShape(response.id, map, "response")

    AnnotationParser(() => response, map).parse()

    response
  }
}
