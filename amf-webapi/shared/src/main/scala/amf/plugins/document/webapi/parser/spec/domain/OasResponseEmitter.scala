package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfScalar
import amf.core.parser.{Annotations, FieldEntry, Fields, Position, Value}
import amf.plugins.document.webapi.annotations.{DefaultPayload, EndPointBodyParameter}
import amf.plugins.domain.webapi.metamodel.{PayloadModel, RequestModel, ResponseModel}
import amf.plugins.domain.webapi.models.{Payload, Response, TemplatedLink}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas2SpecEmitterFactory,
  Oas3SpecEmitterFactory,
  OasSpecEmitterContext
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.common.ExternalReferenceUrlEmitter.handleInlinedRefOr
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasSchemaEmitter

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
                  if (!f.value.value.annotations.contains(classOf[SynthesizedField])) {
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
