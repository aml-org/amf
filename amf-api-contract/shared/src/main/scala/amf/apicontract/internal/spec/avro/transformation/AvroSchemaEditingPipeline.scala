package amf.apicontract.internal.spec.avro.transformation

import amf.core.client.common.transform._
import amf.core.client.common.validation.ProfileNames.AVROSCHEMA
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{ReferenceResolutionStage, SourceInformationStage, UrlShortenerStage}
import amf.shapes.internal.domain.resolution.ShapeNormalizationForUnitStage

class AvroSchemaEditingPipeline private (urlShortening: Boolean = true, val name: String)
    extends TransformationPipeline {

  private def url: Option[UrlShortenerStage] = if (urlShortening) Some(new UrlShortenerStage()) else None

  override def steps: Seq[TransformationStep] = Seq()
//    Seq(
//      new ReferenceResolutionStage(true),
//      new ShapeNormalizationForUnitStage(AVROSCHEMA, keepEditingInfo = true)
//    ) ++ url :+ SourceInformationStage
}

object AvroSchemaEditingPipeline {
  def apply()                    = new AvroSchemaEditingPipeline(true, name)
  private[amf] def cachePipeline = new AvroSchemaEditingPipeline(false, PipelineId.Editing)
  val name: String               = PipelineId.Editing
}

object AvroSchemaCachePipeline {
  val name: String                                    = PipelineId.Cache
  private[amf] def apply(): AvroSchemaEditingPipeline = AvroSchemaEditingPipeline.cachePipeline
}
