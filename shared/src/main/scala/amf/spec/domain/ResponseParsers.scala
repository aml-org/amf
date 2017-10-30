package amf.spec.domain

import amf.domain.{Annotations, Parameter, Payload, Response}
import amf.metadata.domain.{RequestModel, ResponseModel}
import amf.model.AmfArray
import amf.spec.Declarations
import amf.spec.common.{AnnotationParser, ValueNode}
import amf.spec.declaration.RamlTypeParser
import amf.spec.raml.RamlSyntax
import org.yaml.model.{YMap, YMapEntry}
import amf.parser.{YMapOps, YValueOps}
import scala.collection.mutable

/**
  *
  */
case class RamlResponseParser(entry: YMapEntry, producer: (String) => Response, declarations: Declarations)
    extends RamlSyntax {
  def parse(): Response = {

    val node = ValueNode(entry.key)

    val response = producer(node.string().value.toString).add(Annotations(entry))
    val map      = entry.value.value.toMap

    response.set(ResponseModel.StatusCode, node.string())

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

    validateClosedShape(response.id, map, "response")

    AnnotationParser(() => response, map).parse()

    response
  }
}
