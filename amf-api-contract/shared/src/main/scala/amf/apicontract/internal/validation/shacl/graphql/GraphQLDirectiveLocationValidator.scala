package amf.apicontract.internal.validation.shacl.graphql
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.validation.shacl.graphql.GraphQLUtils.inferGraphQLKind
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.metamodel.domain.common.{NameFieldSchema, NameFieldShacl}
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.shapes.internal.domain.metamodel.operations.ShapeParameterModel
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
    val validDomains     = directiveApplication.definedBy.domain.map(_.toString)
    val currentDomains   = element.meta.typeIris // maybe head?
    val isAValidLocation = currentDomains.exists(validDomains.contains)

    // when parsing directives we parse arguments as property shapes of a virtual node shape
    lazy val isValidApplicationToDirectiveArgument = appliedToDirectiveArgument && element.meta
      .isInstanceOf[PropertyShapeModel.type] && validDomains.intersect(ShapeParameterModel.typeIris).nonEmpty

    if (!isAValidLocation && !isValidApplicationToDirectiveArgument) {
      val message = buildErrorMessage(directiveApplication, element, appliedToDirectiveArgument)
      Some(
        ValidationInfo(DomainElementModel.CustomDomainProperties, Some(message), Some(directiveApplication.annotations))
      )
    } else None
  }

  private def buildErrorMessage(
      directiveApplication: DomainExtension,
      element: DomainElement,
      appliedToDirectiveArgument: Boolean
  ) = {
    val kind                    = inferGraphQLKind(element, appliedToDirectiveArgument)
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
