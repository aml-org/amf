package amf.apicontract.internal.transformation.compatibility.raml
import amf.apicontract.client.scala.model.domain.Response
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.transform.stages.elements.resolution.ReferenceResolution
import amf.core.internal.transform.stages.elements.resolution.ReferenceResolution.ASSERT_DIFFERENT
import amf.core.internal.transform.stages.selectors.{LinkSelector, MetaModelSelector, Selector}
import amf.core.client.scala.vocabulary.Namespace.ApiContract

object ResolveRamlCompatibleDeclarationsStage extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit =
    new ResolveRamlCompatibleDeclarations(errorHandler).resolve(model)
}

private class ResolveRamlCompatibleDeclarations(val errorHandler: AMFErrorHandler) {
  val domainSelector
    : Selector = ResponseSelector || ParameterSelector || PayloadSelector || CallbackSelector || ExampleSelector

  def resolve[T <: BaseUnit](model: T): T = {
    val result = model.transform(LinkSelector && domainSelector, transformation)(errorHandler).asInstanceOf[T]
    model match {
      case d: Document =>
        val filteredDeclarations = d.declares.filterNot(domainSelector)
        d.withDeclares(filteredDeclarations)
    }
    result
  }

  private def transformation(e: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    val referenceResolution =
      new ReferenceResolution(errorHandler, customDomainElementTransformation = customDomainElementTransformation)
    referenceResolution.transform(e, conditions = Seq(ASSERT_DIFFERENT))
  }

  private def customDomainElementTransformation(resolved: DomainElement, link: Linkable): DomainElement = {
    (resolved, link) match {
      case (resolvedResponse: Response, linkResponse: Response) =>
        val statusCode = linkResponse.statusCode.value()
        resolvedResponse.withStatusCode(statusCode)
      case _ => resolved
    }
  }

}

object ResponseSelector  extends MetaModelSelector(ApiContract, "Response")
object ParameterSelector extends MetaModelSelector(ApiContract, "Parameter")
object PayloadSelector   extends MetaModelSelector(ApiContract, "Payload")
object CallbackSelector  extends MetaModelSelector(ApiContract, "Callback")
object ExampleSelector   extends MetaModelSelector(ApiContract, "Example")
