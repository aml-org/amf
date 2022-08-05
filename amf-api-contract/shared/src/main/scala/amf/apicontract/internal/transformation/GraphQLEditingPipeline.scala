package amf.apicontract.internal.transformation

import amf.aml.internal.transform.steps.SemanticExtensionFlatteningStage
import amf.apicontract.internal.spec.common.transformation.stage.{
  AnnotationRemovalStage,
  PathDescriptionNormalizationStage
}
import amf.apicontract.internal.transformation.stages.{
  GraphQLDirectiveRecursionDetectionStage,
  GraphQLTypeRecursionDetectionStage
}
import amf.core.client.common.transform._
import amf.core.client.common.validation.{GraphQLProfile, ProfileName}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.transform.stages.SourceInformationStage

class GraphQLEditingPipeline private (urlShortening: Boolean, override val name: String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = GraphQLProfile

  override def steps: Seq[TransformationStep] = {
    Seq(
      references,
      GraphQLTypeRecursionDetectionStage(),
      GraphQLDirectiveRecursionDetectionStage(),
      new PathDescriptionNormalizationStage(profileName, keepEditingInfo = true),
      new AnnotationRemovalStage(),
      new SemanticExtensionFlatteningStage
    ) ++ url :+ SourceInformationStage // source info stage must be invoked after url shortening
  }

}

object GraphQLEditingPipeline {
  def apply()                    = new GraphQLEditingPipeline(true, name)
  private[amf] def cachePipeline = new GraphQLEditingPipeline(false, GraphQLCachePipeline.name)
  val name: String               = PipelineId.Editing
}

object GraphQLCachePipeline {
  def apply(): GraphQLEditingPipeline = GraphQLEditingPipeline.cachePipeline
  val name: String                    = PipelineId.Cache
}
