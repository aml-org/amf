package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.resolution.stages.TransformationStep
import amf.core.resolution.stages.elements.resolution.ReferenceResolution
import amf.core.resolution.stages.elements.resolution.ReferenceResolution.ASSERT_DIFFERENT
import amf.core.resolution.stages.selectors.{LinkSelector, MetaModelSelector, Selector}
import amf.core.vocabulary.Namespace.ApiContract
import amf.plugins.domain.webapi.models.{Response, Payload}

object ResolveRamlCompatibleDeclarationsStage extends TransformationStep {
  override def apply[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T =
    new ResolveRamlCompatibleDeclarations(errorHandler).resolve(model)
}

private class ResolveRamlCompatibleDeclarations(val errorHandler: ErrorHandler) {
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
