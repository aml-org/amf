package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain.Payload
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain.ResponseModel
import amf.apicontract.internal.validation.definitions.ResolutionSideValidations.{
  ExamplesWithInvalidMimeType,
  ExamplesWithNoSchemaDefined
}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.TransformationStep
import amf.shapes.client.scala.model.domain.{AnyShape, Example}
import amf.shapes.internal.domain.resolution.ExampleTracking

/** Apply response examples to payloads schemas matching by media type
  *
  * MediaTypeResolution and Shape Normalization stages must already been run
  * for mutate each payload schema
  */
class ResponseExamplesResolutionStage() extends TransformationStep() {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = model match {
    case d: Document if d.encodes.isInstanceOf[Api] =>
      d.withEncodes(resolveApi(d.encodes.asInstanceOf[Api])(errorHandler))
    case _ => model
  }

  def resolveApi(webApi: Api)(implicit errorHandler: AMFErrorHandler): Api = {
    val allResponses = webApi.endPoints.flatMap(e => e.operations).flatMap(o => o.responses)

    allResponses.zipWithIndex.foreach {
      case (response, index) =>
        val examplesByMediaType = response.examples.map(e => e.mediaType.value() -> e).toMap
        response.fields.removeField(ResponseModel.Examples)
        examplesByMediaType.foreach {
          case (mediaType, example) =>
            val mediaTypeIn = (mediaTypes: Seq[String]) =>
              (p: Payload) => {
                mediaTypes.contains(p.mediaType.value())
            }
            response.payloads.find(mediaTypeIn(Seq(mediaType, "*/*"))) match {
              case Some(p) =>
                p.schema match {
                  case shape: AnyShape =>
                    example.add(ExampleTracking.tracked(p, example, Some(response.id)))
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

  private def violationForUnmappedExample(example: Example, mediaType: String, payloads: Seq[Payload])(
      implicit errorHandler: AMFErrorHandler): Unit = {
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
