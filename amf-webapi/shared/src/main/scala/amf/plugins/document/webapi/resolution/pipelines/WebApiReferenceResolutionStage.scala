package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.plugins.domain.webapi.models.Response

class WebApiReferenceResolutionStage(keepEditingInfo: Boolean = false)(
    override implicit val errorHandler: ErrorHandler)
    extends ReferenceResolutionStage(keepEditingInfo) {

  override protected def customDomainElementTransformation: (DomainElement, Linkable) => DomainElement =
    (domain: DomainElement, source: Linkable) => {
      source match {
        case sourceResponse: Response if sourceResponse.statusCode.option().isDefined =>
          domain match {
            case domainResponse: Response =>
              val copy = domainResponse.copyElement().asInstanceOf[Response]
              copy.withId(sourceResponse.id).withStatusCode(sourceResponse.statusCode.value())
            case _ => domain
          }
        case _ => domain
      }
    }

}
