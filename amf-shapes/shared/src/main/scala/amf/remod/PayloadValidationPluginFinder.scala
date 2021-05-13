package amf.remod

import amf.core.model.domain.Shape
import amf.core.registries.AMFPluginsRegistry
import amf.core.validation.{AMFPayloadValidationPlugin, SeverityLevels, ValidationCandidate}
import amf.internal.environment.Environment

trait PayloadValidationPluginFinder {

  protected def lookupPluginFor(candidate: ValidationCandidate,
                                env: Environment,
                                defaultSeverity: String): AMFPayloadValidationPlugin =
    searchPlugin(candidate.payload.mediaType.value(), candidate.shape, env)
      .getOrElse(AnyMatchPayloadPlugin(defaultSeverity))

  protected def lookupPluginFor(mediaType: String,
                                shape: Shape,
                                env: Environment,
                                defaultSeverity: String = SeverityLevels.VIOLATION): AMFPayloadValidationPlugin =
    searchPlugin(mediaType, shape, env).getOrElse(AnyMatchPayloadPlugin(defaultSeverity))

  protected def searchPlugin(mediaType: String, shape: Shape, env: Environment): Option[AMFPayloadValidationPlugin] =
    AMFPluginsRegistry.dataNodeValidatorPluginForMediaType(mediaType).find(_.canValidate(shape, env))
}
