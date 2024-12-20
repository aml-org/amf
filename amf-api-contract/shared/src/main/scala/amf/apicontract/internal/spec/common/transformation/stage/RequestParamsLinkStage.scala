package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain.Request
import amf.apicontract.internal.transformation.ReferenceDocumentationResolver.updateSummaryAndDescription
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.transform.stages.elements.resolution.ReferenceResolution
import amf.core.internal.transform.stages.selectors.{LinkSelector, Selector}

/** Resolve links in request parameters (OAS 3.0, 3.1) */
object RequestParamsLinkStage extends TransformationStep {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit =
    new RequestParamsLinkStage(errorHandler).resolve(model, configuration)
}

private class RequestParamsLinkStage(val errorHandler: AMFErrorHandler) {
  def resolve[T <: BaseUnit](model: T, configuration: AMFGraphConfiguration): T = {
    model
      .transform(LinkSelector && ReqWithParametersSelector, transform(_, _, configuration))(errorHandler)
      .asInstanceOf[T]
  }

  private def transform(
      e: DomainElement,
      isCycle: Boolean,
      configuration: AMFGraphConfiguration
  ): Option[DomainElement] = {
    val referenceResolution =
      new ReferenceResolution(errorHandler, customDomainElementTransformation = customDomainElementTransformation)
    referenceResolution.transform(e, conditions = Seq(ReferenceResolution.ASSERT_DIFFERENT), configuration)
  }

  private def customDomainElementTransformation(resolved: DomainElement, link: Linkable): DomainElement = {
    updateSummaryAndDescription(resolved, link)
    (resolved, link) match {
      case (resolvedReq: Request, link: Request) =>
        val copied = Request(resolvedReq.fields.copy(), resolvedReq.annotations.copy())
        copied.id = link.id
        setParams(link, copied)
        copied
      case _ => resolved
    }
  }

  private def setParams(from: Request, to: Request): Unit = {
    if (from.cookieParameters.nonEmpty) to.withCookieParameters(from.cookieParameters)
    if (from.headers.nonEmpty) to.withHeaders(from.headers)
    if (from.uriParameters.nonEmpty) to.withUriParameters(from.uriParameters)
    if (from.queryParameters.nonEmpty) to.withQueryParameters(from.queryParameters)
  }

}

object ReqWithParametersSelector extends Selector {
  override def apply(element: DomainElement): Boolean = element match {
    case req: Request => hasParameters(req)
    case _            => false
  }

  private def hasParameters(req: Request) =
    (req.queryParameters ++ req.uriParameters ++ req.cookieParameters ++ req.headers).nonEmpty
}
