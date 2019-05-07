package amf.plugins.domain.webapi.resolution.stages

import amf.core.annotations.TrackedElement
import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.metamodel.ResponseModel
import amf.plugins.domain.webapi.models.WebApi

/** Apply response examples to payloads schemas matching by media type
  *
  * MediaTypeResolution and Shape Normalization stages must already been run
  * for mutate each payload schema
  */
class ExamplesResolutionStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {
  override def resolve[T <: BaseUnit](model: T): T = model match {
    case d: Document if d.encodes.isInstanceOf[WebApi] =>
      d.withEncodes(resolveWebApi(d.encodes.asInstanceOf[WebApi])).asInstanceOf[T]
    case _ => model
  }

  def resolveWebApi(webApi: WebApi): WebApi = {
    val allResponses = webApi.endPoints.flatMap(e => e.operations).flatMap(o => o.responses)

    allResponses.zipWithIndex.foreach {
      case (response, index) =>
        val mappedExamples = response.examples.map(e => e.mediaType.value() -> e).toMap
        response.fields.removeField(ResponseModel.Examples)
        mappedExamples.foreach {
          case (mediaType, example) =>
            response.payloads.find(_.mediaType.value() == mediaType) match {
              case Some(p) =>
                p.schema match {
                  case shape: AnyShape =>
                    example.withName(example.mediaType.value() + index)
                    example.add(TrackedElement(p.id))
                    shape.withExamples(shape.examples ++ Seq(example))
                  case _ => response.withExamples(response.examples ++ Seq(example))
                }
              case _ => response.withExamples(response.examples ++ Seq(example))
            }
        }
    }
    webApi
  }
}
