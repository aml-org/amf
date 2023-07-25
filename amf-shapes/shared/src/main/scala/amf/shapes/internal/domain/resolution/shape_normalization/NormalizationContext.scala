package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.common.validation.{ProfileName, Raml08Profile}
import amf.core.client.scala.errorhandling.AMFErrorHandler

private[resolution] class NormalizationContext(
    final val errorHandler: AMFErrorHandler,
    final val keepEditingInfo: Boolean,
    final val profile: ProfileName,
    val resolvedInheritanceIndex: ResolvedInheritanceIndex = ResolvedInheritanceIndex(),
    final val logger: ShapeNormalizationLogger = ShapeNormalizationLogger()
) {
  val isRaml08: Boolean = profile.equals(Raml08Profile)
}
