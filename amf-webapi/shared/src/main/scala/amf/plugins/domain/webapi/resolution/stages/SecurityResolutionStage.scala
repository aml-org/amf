package amf.plugins.domain.webapi.resolution.stages

import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.DomainElement
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.security.{ParametrizedSecuritySchemeModel, SecuritySchemeModel}
import amf.plugins.domain.webapi.metamodel.{EndPointModel, OperationModel, WebApiModel}
import amf.plugins.domain.webapi.models.WebApi
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityScheme, Settings}

class SecurityResolutionStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {

  private def asSecurityScheme(finder: String => Option[DomainElement],
                               scheme: ParametrizedSecurityScheme,
                               parent: String): SecurityScheme = {
    scheme.fields
      .entry(ParametrizedSecuritySchemeModel.Scheme)
      .fold(SecurityScheme(scheme.annotations).withName(scheme.name.value())) { f =>
        f.value.value match {
          case s: SecurityScheme =>
            val cloned = s.cloneScheme(parent)

            if (Option(s.settings).isEmpty) Option(scheme.settings).foreach {
              cloned.set(SecuritySchemeModel.Settings, _)
            }

            // keep root definition  for now. If has scopes defined override only scopes??
//            validateSettings(s.settings, scheme.settings).foreach(cloned.set(SecuritySchemeModel.Settings, _))

            cloned
          case _ => throw new Exception(s"Security scheme not found for parameterized security scheme ${scheme.id}.")
        }
      }
  }

  // TODO validate given settings against root defined settings? Step over? Override only the explicit ones?
  private def validateSettings(root: Settings, settings: Settings): Option[Settings] =
    Option(settings).orElse(Option(root))

  private def resolveSecurity(finder: String => Option[DomainElement], api: WebApi): Unit = {
    val rootSecurity = field(api, WebApiModel.Security)

    api.endPoints.foreach { endPoint =>
      val endPointSecurity = merge(rootSecurity, field(endPoint, EndPointModel.Security))

      endPoint.operations.foreach { operation =>
        // I need to know if this is an empty array or if it's not defined.
        val opSecurity = field(operation, OperationModel.Security)

        val security = merge(endPointSecurity, opSecurity)

        security.foreach { schemes =>
          val resolved = schemes.map(asSecurityScheme(finder, _, operation.id))

          if (resolved.nonEmpty) operation.setArray(OperationModel.Security, resolved)
        }
      }
    }
  }

  /** Get and remove field from domain element */
  private def field(element: DomainElement, field: Field) = {
    val result = element.fields.entry(field).map(_.array.values.map(v => v.asInstanceOf[ParametrizedSecurityScheme]))
    element.fields.removeField(field)
    result
  }

  private def merge(root: Option[Seq[ParametrizedSecurityScheme]],
                    ep: Option[Seq[ParametrizedSecurityScheme]]): Option[Seq[ParametrizedSecurityScheme]] =
    ep.orElse(root).filter(_.nonEmpty)

  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
        resolveSecurity(model.findById, doc.encodes.asInstanceOf[WebApi])
      case _ =>
    }
    model.asInstanceOf[T]
  }
}
