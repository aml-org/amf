package amf.shapes.internal.transformation

import amf.core.client.common.transform._
import amf.core.client.common.validation.ProfileNames.JSONSCHEMA
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{ReferenceResolutionStage, SourceInformationStage, UrlShortenerStage}
import amf.shapes.internal.domain.resolution.ShapeNormalizationStage
import amf.shapes.internal.transformation.stages.ContextTransformationStage

class JsonLDSchemaEditingPipeline private (val name: String) extends TransformationPipeline {

  override def steps: Seq[TransformationStep] =
    Seq(
      new ReferenceResolutionStage(true),
      new ContextTransformationStage()
    ) :+ SourceInformationStage
}
