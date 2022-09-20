package amf.apicontract.internal.validation.shacl.graphql
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.validation.shacl.graphql.GraphQLLocationHelper.toLocation
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.metamodel.domain.common.{NameFieldSchema, NameFieldShacl}
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object GraphQLDirectiveLocationValidator {
  def apply(
      directiveApplications: Seq[DomainExtension],
      element: DomainElement,
      appliedToDirectiveArgument: Boolean = false
  ): Seq[Option[ValidationInfo]] = {
    directiveApplications
      .map(application => validateApplication(application, element, appliedToDirectiveArgument))
      .filter(_.isDefined)
  }

  private def validateApplication(
      directiveApplication: DomainExtension,
      element: DomainElement,
      appliedToDirectiveArgument: Boolean = false
  ): Option[ValidationInfo] = {
    val result = for {
      actual      <- toLocation(element, appliedToDirectiveArgument)
      declaration <- Option(directiveApplication.definedBy)
    } yield {
      val expected           = declaration.domain.map(_.value())
      val isValidApplication = expected.contains(actual.iri.iri())
      (actual, isValidApplication)
    }

    result match {
      case Some((actual, false)) =>
        val message = buildErrorMessage(directiveApplication, element, actual.name)
        Some(ValidationInfo(DomainElementModel.CustomDomainProperties, Some(message), Some(directiveApplication.annotations)))
      case _ => None
    }
  }

  private def buildErrorMessage(directiveApplication: DomainExtension, element: DomainElement, kind: String) = {
    val nameOpt: Option[String] = extractName(element)

    nameOpt match {
      case Some(name) => s"Directive '${directiveApplication.name.value()}' cannot be applied to $kind $name"
      case _          => s"Directive '${directiveApplication.name.value()}' cannot be applied to $kind"
    }
  }

  private def extractName(element: DomainElement) = {
    element.fields
      .getValueAsOption(NameFieldSchema.Name)
      .orElse(element.fields.getValueAsOption(NameFieldShacl.Name))
      .flatMap { v =>
        element match {
          case _: WebApi => None
          case _: EndPoint =>
            Some(
              v.value
                .asInstanceOf[AmfScalar]
                .toString
                .stripPrefix("Query.")
                .stripPrefix("Mutation.")
                .stripPrefix("Subscription.")
            )
          case _ => Some(v.value.asInstanceOf[AmfScalar].toString)
        }
      }
      .map(name => s"'$name'")
  }
}
