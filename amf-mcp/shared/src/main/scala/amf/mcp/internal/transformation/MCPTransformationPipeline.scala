package amf.mcp.internal.transformation

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}

class MCPTransformationPipeline private(override val name: String) extends TransformationPipeline() {

  override def steps: Seq[TransformationStep] = Nil
}

object MCPTransformationPipeline {
  def apply()      = new MCPTransformationPipeline(name)
  val name: String = PipelineId.Default
}
