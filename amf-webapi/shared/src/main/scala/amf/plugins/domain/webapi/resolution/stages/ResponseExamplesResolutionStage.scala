package amf.plugins.domain.webapi.resolution.stages

import amf.core.annotations.TrackedElement
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.models.{AnyShape, Example}
import amf.plugins.domain.webapi.metamodel.ResponseModel
import amf.plugins.domain.webapi.models.{Payload, WebApi}
import amf.validations.ResolutionSideValidations.{ExamplesWithInvalidMimeType, ExamplesWithNoSchemaDefined}

/** Apply response examples to payloads schemas matching by media type
  *
  * MediaTypeResolution and Shape Normalization stages must already been run
  * for mutate each payload schema
  */
class ResponseExamplesResolutionStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {
  override def resolve[T <: BaseUnit](model: T): T = model match {
    case d: Document if d.encodes.isInstanceOf[WebApi] =>
      d.withEncodes(resolveWebApi(d.encodes.asInstanceOf[WebApi])).asInstanceOf[T]
    case _ => model
  }

  def resolveWebApi(webApi: WebApi): WebApi = {
    val allResponses = webApi.endPoints.flatMap(e => e.operations).flatMap(o => o.responses)

    allResponses.zipWithIndex.foreach {
      case (response, index) =>
        val examplesByMediaType = response.examples.map(e => e.mediaType.value() -> e).toMap
        response.fields.removeField(ResponseModel.Examples)
        examplesByMediaType.foreach {
          case (mediaType, example) =>
            response.payloads.find(_.mediaType.value() == mediaType) match {
              case Some(p) =>
                p.schema match {
                  case shape: AnyShape =>
                    example.add(TrackedElement(p.id))
                    if (!shape.examples.exists(_.id == example.id)) {
                      example.withName(example.mediaType.value() + index)
                      shape.withExamples(shape.examples ++ Seq(example))
                    }
                  case _ => response.withExamples(response.examples ++ Seq(example))
                }
              case _ =>
                violationForUnmappedExample(example, mediaType, response.payloads)
                response.withExamples(response.examples ++ Seq(example))
            }
        }
    }
    webApi
  }

  private def violationForUnmappedExample(example: Example, mediaType: String, payloads: Seq[Payload]): Unit = {
    if (payloads.isEmpty)
      errorHandler.violation(
        ExamplesWithNoSchemaDefined,
        example.id,
        "When schema is undefined, 'examples' facet is invalid as no content is returned as part of the response",
        example.annotations
      )
    else
      errorHandler.violation(ExamplesWithInvalidMimeType,
                             example.id,
                             s"Mime type '$mediaType' defined in examples must be present in a 'produces' property",
                             example.annotations)
  }
}
