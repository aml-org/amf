package amf.client.`new`.builder

import amf.ProfileName
import amf.client.`new`.amfcore.{AmfResolutionPipeline, AmfResolutionStep}
import amf.core.model.document.BaseUnit

import scala.collection.mutable.ListBuffer

class AmfResolutionPipelineFactory(profile: ProfileName, steps: ListBuffer[AmfResolutionStep] = ListBuffer.empty) {

  def addStep(step: AmfResolutionStep): AmfResolutionPipelineFactory = {
    steps += step
    this
  }

  def addTransformation(fn: (BaseUnit) => BaseUnit): AmfResolutionPipelineFactory = {
    steps += new AmfResolutionStep {
      override def apply(bu: BaseUnit): Boolean = true

      override def resolve(model: BaseUnit): BaseUnit = fn(model)
    }
    this
  }
  // prepend??
  def build() = {
    new AmfResolutionPipeline(profile, steps.toList)
  }
}
