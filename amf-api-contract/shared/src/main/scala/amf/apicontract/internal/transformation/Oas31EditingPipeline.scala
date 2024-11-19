package amf.apicontract.internal.transformation

import amf.core.client.common.transform._
import amf.core.client.common.validation.{Oas31Profile, ProfileName}
import amf.core.client.scala.transform.TransformationStep

class Oas31EditingPipeline private[amf] (urlShortening: Boolean, override val name: String)
    extends Oas3EditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Oas31Profile

  override def steps: Seq[TransformationStep] = super.steps
}

object Oas31EditingPipeline {
  val name: String = PipelineId.Editing
  def apply()      = new Oas31EditingPipeline(true, name = name)

  private[amf] def cachePipeline() = new Oas31EditingPipeline(false, Oas31CachePipeline.name)
}

object Oas31CachePipeline {
  val name: String                  = PipelineId.Cache
  def apply(): Oas31EditingPipeline = Oas31EditingPipeline.cachePipeline()
}
