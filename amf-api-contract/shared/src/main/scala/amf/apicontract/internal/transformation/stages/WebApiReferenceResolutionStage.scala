package amf.apicontract.internal.transformation.stages

import amf.apicontract.client.scala.model.domain._
import amf.apicontract.internal.metamodel.domain.MessageModel
import amf.apicontract.internal.transformation.ReferenceDocumentationResolver.updateSummaryAndDescription
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.annotations.DeclaredServerVariable
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.transform.stages.ReferenceResolutionStage

class WebApiReferenceResolutionStage(keepEditingInfo: Boolean = false)
    extends ReferenceResolutionStage(keepEditingInfo) {

  override protected def customDomainElementTransformation: (DomainElement, Linkable) => DomainElement =
    (domain: DomainElement, source: Linkable) => {
      updateSummaryAndDescription(domain, source)
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
        case sourceServer: Server =>
          domain match {
            case server: Server =>
              val copy = server.copyElement().asInstanceOf[Server]
              copy.withId(sourceServer.id).withName(sourceServer.name.value())
            case _ => domain
          }
        case sourceServerVariable: Parameter
            if sourceServerVariable.annotations.contains(classOf[DeclaredServerVariable]) =>
          domain match {
            case serverVariable: Parameter =>
              val copy = serverVariable.copyElement().asInstanceOf[Parameter]
              copy.withId(sourceServerVariable.id).withName(sourceServerVariable.name.value())
            case _ => domain
          }
        case sourceEndpoint: EndPoint => // asyncApi channel
          domain match {
            case channel: EndPoint =>
              val copy = channel.copyElement().asInstanceOf[EndPoint]
              sourceEndpoint.name.option().foreach(copy.withName)
              sourceEndpoint.path.option().foreach(copy.withPath)
              copy.withId(sourceEndpoint.id).withPath(sourceEndpoint.path.value())

            case _ => domain
          }
        case _ => domain
      }
    }

  /** called when there is a link to a message, this method creates a request or response that has the same content
    * (fields and annotations) of the declared message.
    */
  private def copyMessage(
      message: Message,
      link: Message,
      constructor: (Fields, Annotations) => Message
  ): DomainElement = {
    val copy = constructor(message.fields.copy(), message.annotations.copy())
    copy.withId(link.id)
  }

}
