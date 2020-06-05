package amf.plugins.domain.webapi.resolution.stages

import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.DomainElement
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.security.OAuth2FlowModel
import amf.plugins.domain.webapi.metamodel.{EndPointModel, OperationModel, WebApiModel}
import amf.plugins.domain.webapi.models.WebApi
import amf.plugins.domain.webapi.models.security.{
  OAuth2Settings,
  ParametrizedSecurityScheme,
  SecurityRequirement,
  Settings
}
import amf.plugins.features.validation.CoreValidations

class SecurityResolutionStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {

  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
        resolveSecurity(doc.encodes.asInstanceOf[WebApi])
      case _ =>
    }
    model.asInstanceOf[T]
  }

  protected def resolveSecurity(api: WebApi): Unit = {
    val rootSecurity = getAndRemove(api, WebApiModel.Security)

    api.endPoints.foreach { endPoint =>
      val endPointSecurity = overrideWith(rootSecurity, getAndRemove(endPoint, EndPointModel.Security))

      endPoint.operations.foreach { operation =>
        // I need to know if this is an empty array or if it's not defined.
        val opSecurity = getAndRemove(operation, OperationModel.Security)

        overrideWith(endPointSecurity, opSecurity).foreach { requirements =>
          requirements.foreach {
            case r: SecurityRequirement =>
              r.schemes.foreach {
                case s: ParametrizedSecurityScheme
                    if Option(s.settings).isDefined && Option(s.scheme).map(_.settings).isDefined =>
                  validateSettings(s.scheme.settings, s.settings)
                case _ => // ignore
              }
            case _ => // ignore
          }
          if (requirements.nonEmpty) operation.setArray(OperationModel.Security, requirements)
        }
      }
    }
  }

  // TODO validate given settings against root defined settings? Step over? Override only the explicit ones?
  private def validateSettings(root: Settings, settings: Settings): Unit =
    root match {
      case rootAuth2: OAuth2Settings if settings.isInstanceOf[OAuth2Settings] =>
        val auth2Settings   = settings.asInstanceOf[OAuth2Settings]
        val rootAuth2Scopes = scopeNames(rootAuth2)
        val scopes = scopeNames(auth2Settings).filter { s =>
          !rootAuth2Scopes.contains(s)
        }
        if (scopes.nonEmpty) scopesNotDefinedValidation(settings, scopes)
      // TODO: Should I validate that settings are from same class?
      case _ => // ignore
    }

  private def scopeNames(oauth2: OAuth2Settings): Seq[String] =
    oauth2.flows.headOption.toList.flatMap(_.scopes.flatMap(_.name.option()))

  private def getAndRemove(element: DomainElement, field: Field) = {
    val result = element.fields.entry(field).map(_.array.values.map(v => v.asInstanceOf[SecurityRequirement]))
    element.fields.removeField(field)
    result
  }

  private def overrideWith(base: Option[Seq[SecurityRequirement]],
                           overrider: Option[Seq[SecurityRequirement]]): Option[Seq[SecurityRequirement]] =
    overrider.orElse(base).filter(_.nonEmpty)

  private def scopesNotDefinedValidation(settings: Settings, scopes: Seq[String]) = {
    errorHandler.violation(
      CoreValidations.ResolutionValidation,
      settings.id,
      Some(OAuth2FlowModel.Scopes.value.toString),
      "Follow scopes are not defined in root: " + scopes.toString(),
      settings.position(),
      settings.location()
    )
  }
}
