package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.transform.stages.elements.resolution.ElementStageTransformer
import amf.shapes.internal.domain.resolution.shape_normalization2.{NormalizationContext2, ReferencesFixer, ShapeInheritanceResolver}

class ShapeTransformer2(context: NormalizationContext2) extends ElementStageTransformer[Shape] {

  override def transform(element: Shape, configuration: AMFGraphConfiguration): Option[Shape] = {
    val resolvedInheritance = ReferencesFixer(element, context)
    Some(resolvedInheritance)
  }
}

object ShapeTransformer2 {
  def apply(errorHandler: AMFErrorHandler, keepEditingInfo: Boolean, profileName: ProfileName): ShapeTransformer2 = {
    val context = new NormalizationContext2(errorHandler, keepEditingInfo, profileName)
    new ShapeTransformer2(context)
  }
}
