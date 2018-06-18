package amf.plugins.domain.webapi.resolution.stages

import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.metamodel.ResponseModel
import amf.plugins.domain.webapi.models.WebApi

/** Apply response examples to payloads schemas matching by media type
  *
  * MediaTypeResolution and Shape Normalization stages must already been run
  * for mutate each payload schema
  */
class ExamplesResolutionStage(profile: String) extends ResolutionStage(profile) {
  override def resolve(model: BaseUnit): BaseUnit = model match {
    case d: Document if d.encodes.isInstanceOf[WebApi] =>
      d.withEncodes(resolveWebApi(d.encodes.asInstanceOf[WebApi]))
    case _ => model
  }

  def resolveWebApi(webApi: WebApi): WebApi = {
    val allResponses = webApi.endPoints.flatMap(e => e.operations).flatMap(o => o.responses)

    allResponses.foreach { response =>
      val mappedExamples = response.examples.map(e => e.mediaType.value() -> e).toMap
      response.fields.removeField(ResponseModel.Examples)
      response.payloads.foreach(p => {
        p.schema match {
          case shape: AnyShape =>
            val exampleOption = mappedExamples.get(p.mediaType.value())
            exampleOption.foreach(e => { shape.withExamples(shape.examples ++ Seq(e)) })
          case _ => // ignore
        }
      })
    }
    webApi
  }
}
