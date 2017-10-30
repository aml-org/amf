package amf.resolution.stages
import amf.document.{BaseUnit, Document}
import amf.domain.WebApi
import amf.metadata.domain.ResponseModel

/** Apply response examples to payloads schemas matching by media type
  *
  * MeditaTypeResolution and Shape Normalization stages must already been run
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
      val mappedExamples = response.examples.map(e => e.mediaType -> e).toMap
      response.fields.remove(ResponseModel.Examples)
      response.payloads.foreach(p => {
        val exampleOption = mappedExamples.get(p.mediaType)
        exampleOption.foreach(e => { p.schema.withExamples(p.schema.examples ++ Seq(e)) })
      })
    }
    webApi
  }
}
