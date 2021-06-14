package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.annotations.TrackedElement
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.utils.AmfStrings
import amf.core.internal.validation.CoreValidations
import amf.plugins.document.apicontract.annotations.{DefaultPayload, ExternalReferenceUrl}
import amf.plugins.document.apicontract.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.OasDefinitions
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorResponse
import amf.plugins.document.apicontract.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.apicontract.parser.spec.declaration.OasTypeParser
import amf.plugins.document.apicontract.parser.spec.oas.{Oas2Syntax, Oas3Syntax}
import amf.plugins.domain.apicontract.metamodel.ResponseModel.Headers
import amf.plugins.domain.apicontract.metamodel.{PayloadModel, RequestModel, ResponseModel}
import amf.plugins.domain.apicontract.models.{Parameter, Payload, Response}
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import org.yaml.model.YMap

import scala.Console.in
import scala.collection.mutable

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
                ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find response reference $url", map)
                val errorRes: Response = ErrorResponse(url, map).link(name)
                adopted(errorRes)
                errorRes
            }
          }
      case Right(_) =>
        val res = Response()
        adopted(res)

        ctx.closedShape(res.id, map, "response")

        map.key("description", ResponseModel.Description in res)

        map.key(
          "headers",
          entry => {
            val parameters: Seq[Parameter] =
              OasHeaderParametersParser(entry.value.as[YMap], { header =>
                header.adopted(res.id)
                res.add(Headers, header)
              }).parse()
            res.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
          }
        )

        val payloads = mutable.ListBuffer[Payload]()

        // RAML 1.0 extensions
        map.key(
          "responsePayloads".asOasExtension, { entry =>
            entry.value
              .as[Seq[YMap]]
              .map(value => payloads += OasPayloadParser(value, res.withPayload).parse())
            res.set(ResponseModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
          }
        )

        // OAS 2.0
        if (ctx.syntax == Oas2Syntax) {

          defaultPayload(map, res.id).foreach({ dp =>
            res.set(ResponseModel.Payloads, AmfArray(payloads :+ dp, Annotations.virtual()), Annotations.inferred())
          })

          map.key(
            "examples",
            entry => {
              val examples = ExamplesByMediaTypeParser(entry, res.id).parse()
              if (examples.nonEmpty) {
                examples.foreach(_.annotations += TrackedElement(res.id))
              }
              res.set(ResponseModel.Examples, AmfArray(examples, Annotations(entry.value)), Annotations(entry))
            }
          )

          ctx.closedShape(res.id, map, "response")
        }

        // OAS 3.0.0
        if (ctx.syntax == Oas3Syntax) {
          map.key(
            "content", { entry =>
              payloads ++= OasContentsParser(entry, res.withPayload).parse()
              res.set(ResponseModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
            }
          )

          map.key(
            "links",
            entry => {
              val links = entry.value
                .as[YMap]
                .entries
                .map(e => OasLinkParser(res.id, e).parse())
              res.set(ResponseModel.Links, AmfArray(links, Annotations(entry.value)), Annotations(entry))
            }
          )
        }

        AnnotationParser(res, map)(WebApiShapeParserContextAdapter(ctx)).parse()

        res
    }
  }

  private def defaultPayload(entries: YMap, parentId: String): Option[Payload] = {
    val payload = Payload(Annotations.virtual()).add(DefaultPayload())

    entries.key("mediaType".asOasExtension,
                entry => payload.set(PayloadModel.MediaType, ScalarNode(entry.value).string(), Annotations(entry)))
    // TODO add parent id to payload?
    payload.adopted(parentId)

    entries.key(
      "schema",
      entry => {
        val shape = OasTypeParser(entry, shape => shape.withName("default").adopted(payload.id))(
          WebApiShapeParserContextAdapter(ctx))
          .parse()
          .map { s =>
            ctx.autoGeneratedAnnotation(s)
            tracking(s, payload.id)
          }
        payload.set(PayloadModel.Schema, shape.getOrElse(AnyShape(entry.value)), Annotations(entry))
      }
    )

    if (payload.fields.nonEmpty) Some(payload) else None
  }
}
