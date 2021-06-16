package amf.apicontract.internal.transformation

import amf.apicontract.client.scala.model.domain.{Message, Parameter, Request, Response}
import amf.apicontract.internal.metamodel.domain.MessageModel
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.transform.stages.ReferenceResolutionStage
import amf.core.internal.parser.domain.{Annotations, Fields}

class WebApiReferenceResolutionStage(keepEditingInfo: Boolean = false)
    extends ReferenceResolutionStage(keepEditingInfo) {

  override protected def customDomainElementTransformation: (DomainElement, Linkable) => DomainElement =
    (domain: DomainElement, source: Linkable) => {
      source match {
        case sourceResp: Response =>
          domain match {
            case domainResponse: Response if sourceResp.statusCode.option().isDefined =>
              val copy = domainResponse.copyElement().asInstanceOf[Response]
              copy.withId(sourceResp.id).withStatusCode(sourceResp.statusCode.value())
            case message: Message if message.meta == MessageModel =>
              copyMessage(message, sourceResp, (fields, annotations) => Response(fields, annotations))
            case _ => domain
          }
        case sourceReq: Request =>
          domain match {
            case message: Message
                if message.meta == MessageModel => // has to be an instance of message, not child instances
              copyMessage(message, sourceReq, (fields, annotations) => Request(fields, annotations))
            case _ => domain
          }
        case sourceParam: Parameter if sourceParam.name.option().isDefined =>
          domain match {
            case domainParam: Parameter =>
              if (sourceParam.name.option() != domainParam.name.option()) {
                val copy = domainParam.copyElement().asInstanceOf[Parameter]
                copy.withId(sourceParam.id).withName(sourceParam.name.value())
              } else
                domain
            case _ => domain
          }
        case _ => domain
      }
    }

  /**
    * called when there is a link to a message, this method creates a request or response that has the same
    * content (fields and annotations) of the declared message.
    */
  private def copyMessage(message: Message,
                          link: Message,
                          constructor: (Fields, Annotations) => Message): DomainElement = {
    val copy = constructor(message.fields.copy(), message.annotations.copy())
    copy.withId(link.id)
  }

}
