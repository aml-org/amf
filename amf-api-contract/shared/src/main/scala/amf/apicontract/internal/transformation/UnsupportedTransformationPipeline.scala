package amf.apicontract.internal.transformation

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}

case class UnsupportedTransformationPipeline(name: String, configName: String) extends TransformationPipeline {
  override def steps: Seq[TransformationStep] = {
    throw new Exception(s"$name transformation is not supported for the $configName composite configuration")
  }
}

object UnsupportedTransformationPipeline {
  def editing(configName: String): TransformationPipeline =
    UnsupportedTransformationPipeline(PipelineId.Editing, configName)
  def default(configName: String): TransformationPipeline =
    UnsupportedTransformationPipeline(PipelineId.Default, configName)
  def cache(configName: String): TransformationPipeline =
    UnsupportedTransformationPipeline(PipelineId.Cache, configName)
}
