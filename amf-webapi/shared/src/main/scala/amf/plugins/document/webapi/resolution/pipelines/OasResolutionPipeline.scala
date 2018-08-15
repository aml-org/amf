package amf.plugins.document.webapi.resolution.pipelines
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.plugins.domain.webapi.models.Response
import amf.{OasProfile, ProfileName}

class OasReferenceResolutionStage(keepEditingInfo: Boolean = false)(override implicit val errorHandler: ErrorHandler)
    extends ReferenceResolutionStage(keepEditingInfo) {
  override protected def customDomainElementTransformation(domain: DomainElement, source: Linkable): DomainElement = {
    source match {
      case sourceResponse: Response if sourceResponse.statusCode.option().isDefined =>
        domain match {
          case domainResponse: Response =>
            domainResponse.withStatusCode(sourceResponse.statusCode.value())
          case _ => domain
        }
      case _ => domain
    }

  }
}

class OasResolutionPipeline(override val model: BaseUnit) extends AmfResolutionPipeline(model) {
  override def profileName: ProfileName = OasProfile
  override lazy val references          = new OasReferenceResolutionStage()
}
