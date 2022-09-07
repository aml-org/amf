package amf.apicontract.internal.validation.shacl.graphql
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.validation.shacl.graphql.GraphQLUtils.{inferGraphQLKind, locationFor}
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.metamodel.domain.common.{NameFieldSchema, NameFieldShacl}
import amf.shapes.internal.annotations.GraphQLLocation
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
    val kind               = inferGraphQLKind(element, appliedToDirectiveArgument)
    val currentLocation    = locationFor(kind)
    val directiveLocations = getDirectiveLocations(directiveApplication)
    val isAValidLocation   = directiveLocations.contains(currentLocation)

    if (!isAValidLocation) {
      val message = buildErrorMessage(directiveApplication, element, kind)
      Some(
        ValidationInfo(DomainElementModel.CustomDomainProperties, Some(message), Some(directiveApplication.annotations))
      )
    } else None
  }

  private def getDirectiveLocations(
      directiveApplication: DomainExtension
  ): Set[String] = {
    Option(directiveApplication.definedBy) match {
      case Some(definedBy) => definedBy.annotations.find(classOf[GraphQLLocation]).map(_.location).getOrElse(Set())
      case _               => Set()
    }
  }

  private def buildErrorMessage(directiveApplication: DomainExtension, element: DomainElement, kind: String) = {
    val nameOpt: Option[String] = extractName(element)

    nameOpt match {
      case Some(name) => s"Directive ${directiveApplication.name.value()} cannot be applied to $kind $name"
      case _          => s"Directive ${directiveApplication.name.value()} cannot be applied to $kind"
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
