package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.{Payload, Response, TemplatedLink}
import amf.apicontract.internal.annotations.{DefaultPayload, EndPointBodyParameter}
import amf.apicontract.internal.metamodel.domain.{PayloadModel, RequestModel, ResponseModel}
import amf.apicontract.internal.spec.common.emitter.RamlParametersEmitter
import amf.apicontract.internal.spec.oas.emitter.context.{Oas2SpecEmitterFactory, Oas3SpecEmitterFactory, OasLikeShapeEmitterContextAdapter, OasSpecEmitterContext}
import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.parser.domain.{Annotations, FieldEntry, Fields, Value}
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable

case class OasResponseEmitter(response: Response,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit],
                              isDeclaration: Boolean = false)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val fs = response.fields

    sourceOr(
      response.annotations,
      b.complexEntry(
        ScalarEmitter(statusCodeOrName(fs).getOrElse(AmfScalar("default"))).emit(_),
        OasResponsePartEmitter(response, ordering, references).emit(_)
      )
    )
  }

  private def statusCodeOrName(fs: Fields): Option[AmfScalar] = {
    val statusCode = if (!isDeclaration) fs.entry(ResponseModel.StatusCode) else None
    statusCode.orElse(fs.entry(ResponseModel.Name)).map(_.scalar)
  }

  override def position(): Position = pos(response.annotations)

}

case class OasResponsePartEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends PartEmitter {

  protected implicit val shapeCtx = OasLikeShapeEmitterContextAdapter(spec)

  override def emit(p: PartBuilder): Unit = {
    val fs = response.fields
    handleInlinedRefOr(p, response) {
      if (response.isLink) {
        spec.localReference(response).emit(p)
      } else {
        p.obj { b =>
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(ResponseModel.Description)
            .orElse(Some(FieldEntry(ResponseModel.Description, Value(AmfScalar(""), Annotations())))) // this is mandatory in OAS 2.0
            .map(f => result += ValueEmitter("description", f))
          fs.entry(RequestModel.Headers)
            .map(f => result += RamlParametersEmitter("headers", f, ordering, references)(spec))

          // OAS 3.0.0
          if (spec.factory.isInstanceOf[Oas3SpecEmitterFactory]) {
            response.fields.fields().find(_.field == ResponseModel.Payloads) foreach { f: FieldEntry =>
              val payloads: Seq[Payload] = f.arrayValues
              val annotations            = f.value.annotations
              result += EntryPartEmitter("content",
                                         OasContentPayloadsEmitter(payloads, ordering, references, annotations))
            }

            response.fields.fields().find(_.field == ResponseModel.Links) foreach { f: FieldEntry =>
              val links: Seq[TemplatedLink] = f.arrayValues
              val annotations               = f.value.annotations
              result += EntryPartEmitter("links", OasLinksEmitter(links, ordering, references, annotations))
            }
          }

          // OAS 2.0
          if (spec.factory.isInstanceOf[Oas2SpecEmitterFactory]) {
            val payloads = OasPayloads(response.payloads)

            payloads.default.foreach(payload => {
              payload.fields
                .entry(PayloadModel.MediaType)
                .map(f => result += ValueEmitter("mediaType".asOasExtension, f))
              payload.fields
                .entry(PayloadModel.Schema)
                .map { f =>
                  if (!f.value.annotations.contains(classOf[SynthesizedField])) {
                    result += oas.OasSchemaEmitter(f, ordering, references)
                  }
                }
            })

            if (payloads.other.nonEmpty)
              result += OasPayloadsEmitter("responsePayloads".asOasExtension, payloads.other, ordering, references)
          }

          fs.entry(ResponseModel.Examples)
            .map(f => result += OasResponseExamplesEmitter("examples", f, ordering))
          result ++= AnnotationsEmitter(response, ordering).emitters

          traverse(ordering.sorted(result), b)
        }
      }
    }
  }

  override def position(): Position = pos(response.annotations)
}

case class OasPayloads(default: Option[Payload], other: Seq[Payload])

object OasPayloads {
  def apply(payloads: Seq[Payload], endpointPayloadEmitted: Boolean = false): OasPayloads = {
    val clean = payloads.filter(!_.annotations.contains(classOf[EndPointBodyParameter]))

    var default = clean.find(_.annotations.contains(classOf[DefaultPayload]))

    default = if (endpointPayloadEmitted) default else default.orElse(defaultPayload(clean))

    OasPayloads(default, clean.filter(_ != default.orNull))
  }

  def defaultPayload(payloads: Seq[Payload]): Option[Payload] =
    payloads
      .find(p => p.mediaType.isNullOrEmpty)
      .orElse(payloads.find(_.mediaType.is("application/json")))
      .orElse(payloads.headOption)
}
