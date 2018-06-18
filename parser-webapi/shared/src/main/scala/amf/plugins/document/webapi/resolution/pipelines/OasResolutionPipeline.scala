package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.plugins.domain.webapi.models.Response
import amf.plugins.domain.webapi.resolution.stages.{MediaTypeResolutionStage, ParametersNormalizationStage}

class OasReferenceResolutionStage(keepEditingInfo: Boolean = false) extends ReferenceResolutionStage(ProfileNames.AMF, keepEditingInfo) {
  override protected def customDomainElementTransformation(domain: DomainElement, source: Linkable): DomainElement = {
    source match {
      case sourceResponse: Response if sourceResponse.statusCode.option().isDefined => domain match {
        case domainResponse: Response =>
          domainResponse.withStatusCode(sourceResponse.statusCode.value())
        case _ => domain
      }
      case _ => domain
    }

  }
}

class OasResolutionPipeline extends AmfResolutionPipeline {
  override val profileName: String = ProfileNames.OAS
  override val references = new OasReferenceResolutionStage()
  override val parameters = new ParametersNormalizationStage(ProfileNames.OAS)

  override def resolve[T <: BaseUnit](model: T): T = {
    super.resolve(model)
  }
}
