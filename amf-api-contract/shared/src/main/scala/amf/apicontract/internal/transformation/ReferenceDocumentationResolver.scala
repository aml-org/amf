package amf.apicontract.internal.transformation

import amf.apicontract.client.scala.model.domain.{Operation, Parameter, Request, Response, TemplatedLink}
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.domain.LinkableElementModel
import amf.shapes.client.scala.model.domain.Example

object ReferenceDocumentationResolver {
  def updateSummaryAndDescription(domain: DomainElement, source: Linkable): Unit = {
    def updateSummary(updateFn: String => Unit): Unit =
      Option(source.refSummary.value()).filter(_.nonEmpty).foreach(updateFn)

    def updateDescription(updateFn: String => Unit): Unit =
      Option(source.refDescription.value()).filter(_.nonEmpty).foreach(updateFn)

    domain match {
      case operation: Operation =>
        updateSummary(operation.withSummary)
        updateDescription(operation.withDescription)
      case example: Example =>
        updateSummary(example.withSummary)
        updateDescription(example.withDescription)
      case response: Response             => updateDescription(response.withDescription)
      case param: Parameter               => updateDescription(param.withDescription)
      case request: Request               => updateDescription(request.withDescription)
      case securityScheme: SecurityScheme => updateDescription(securityScheme.withDescription)
      case templatedLink: TemplatedLink   => updateDescription(templatedLink.withDescription)
      case _                              => // ignore
    }
    source.fields.removeField(LinkableElementModel.RefSummary)
    source.fields.removeField(LinkableElementModel.RefDescription)
  }
}
