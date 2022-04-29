package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.{Parameter, Payload, Response}
import amf.apicontract.internal.annotations.DefaultPayload
import amf.apicontract.internal.metamodel.domain.ResponseModel.Headers
import amf.apicontract.internal.metamodel.domain.{PayloadModel, RequestModel, ResponseModel}
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorResponse
import amf.apicontract.internal.spec.common.parser.{SpecParserOps, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.oas.parser.context.{Oas2Syntax, Oas3Syntax, OasWebApiContext}
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.annotations.TrackedElement
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.annotations.ExternalReferenceUrl
import amf.shapes.internal.domain.resolution.ExampleTracking.tracking
import amf.shapes.internal.spec.common.parser.AnnotationParser
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import org.yaml.model.YMap

import scala.collection.mutable
import amf.core.internal.utils._
import amf.shapes.client.scala.model.domain.AnyShape

case class OasResponseParser(map: YMap, adopted: Response => Unit)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {
  def parse(): Response = {

    ctx.link(map) match {
      case Left(url) =>
        val name = OasDefinitions.stripResponsesDefinitionsPrefix(url)

        val annotations = map
          .key("$ref")
          .map(v => v.value)
          .map(Annotations(_))
          .getOrElse(Annotations.synthesized())
        ctx.declarations
          .findResponse(name, SearchScope.Named)
          .map { res =>
            val resLink: Response = res.link(AmfScalar(name), annotations, Annotations.synthesized())
            adopted(resLink)
            resLink
          }
          .getOrElse {
            ctx.navigateToRemoteYNode(url) match {
              case Some(result) =>
                OasResponseParser(result.remoteNode.as[YMap], adopted)(result.context)
                  .parse()
                  .add(ExternalReferenceUrl(url))
              case None =>
                ctx.eh.violation(
                  CoreValidations.UnresolvedReference,
                  "",
                  s"Cannot find response reference $url",
                  map.location
                )
                val errorRes: Response = ErrorResponse(url, map).link(name)
                adopted(errorRes)
                errorRes
            }
          }
      case Right(_) =>
        val res = Response()
        adopted(res)

        ctx.closedShape(res, map, "response")

        map.key("description", ResponseModel.Description in res)

        map.key(
          "headers",
          entry => {
            val parameters: Seq[Parameter] =
              OasHeaderParametersParser(
                entry.value.as[YMap],
                { header =>
                  res.add(Headers, header)
                }
              ).parse()
            res.setWithoutId(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
          }
        )

        val payloads = mutable.ListBuffer[Payload]()

        // RAML 1.0 extensions
        map.key(
          "responsePayloads".asOasExtension,
          { entry =>
            entry.value
              .as[Seq[YMap]]
              .map(value => payloads += OasPayloadParser(value, res.withPayload).parse())
            res.setWithoutId(ResponseModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
          }
        )

        // OAS 2.0
        if (ctx.syntax == Oas2Syntax) {

          defaultPayload(map, res.id).foreach({ dp =>
            res.setWithoutId(
              ResponseModel.Payloads,
              AmfArray(payloads :+ dp, Annotations.virtual()),
              Annotations.inferred()
            )
          })

          map.key(
            "examples",
            entry => {
              val examples = ExamplesByMediaTypeParser(entry, res.id).parse()
              if (examples.nonEmpty) {
                examples.foreach(_.annotations += TrackedElement.fromInstance(res))
              }
              res.setWithoutId(ResponseModel.Examples, AmfArray(examples, Annotations(entry.value)), Annotations(entry))
            }
          )

          ctx.closedShape(res, map, "response")
        }

        // OAS 3.0.0
        if (ctx.syntax == Oas3Syntax) {
          map.key(
            "content",
            { entry =>
              payloads ++= OasContentsParser(entry, res.withPayload).parse()
              res.setWithoutId(ResponseModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
            }
          )

          map.key(
            "links",
            entry => {
              val links = entry.value
                .as[YMap]
                .entries
                .map(e => OasLinkParser(res.id, e).parse())
              res.setWithoutId(ResponseModel.Links, AmfArray(links, Annotations(entry.value)), Annotations(entry))
            }
          )
        }

        AnnotationParser(res, map)(WebApiShapeParserContextAdapter(ctx)).parse()

        res
    }
  }

  private def defaultPayload(entries: YMap, parentId: String): Option[Payload] = {
    val payload = Payload(Annotations.virtual()).add(DefaultPayload())

    entries.key(
      "mediaType".asOasExtension,
      entry => payload.setWithoutId(PayloadModel.MediaType, ScalarNode(entry.value).string(), Annotations(entry))
    )

    entries.key(
      "schema",
      entry => {
        val shape = OasTypeParser(entry, shape => shape.withName("default"))(WebApiShapeParserContextAdapter(ctx))
          .parse()
          .map { s =>
            ctx.autoGeneratedAnnotation(s)
            tracking(s, payload)
          }
        payload.setWithoutId(PayloadModel.Schema, shape.getOrElse(AnyShape(entry.value)), Annotations(entry))
      }
    )

    if (payload.fields.nonEmpty) Some(payload) else None
  }
}
