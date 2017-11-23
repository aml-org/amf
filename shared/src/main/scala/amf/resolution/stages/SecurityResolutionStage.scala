package amf.resolution.stages

import amf.framework.model.document.{BaseUnit, Document}
import amf.domain.security.{ParametrizedSecurityScheme, SecurityScheme, Settings}
import amf.domain.WebApi
import amf.framework.metamodel.Field
import amf.framework.model.domain.DomainElement
import amf.metadata.domain.security.{ParametrizedSecuritySchemeModel, SecuritySchemeModel}
import amf.metadata.domain.{EndPointModel, OperationModel, WebApiModel}
import amf.validation.Validation

class SecurityResolutionStage(profile: String)(override implicit val currentValidation: Validation) extends ResolutionStage(profile) {

  private def asSecurityScheme(finder: String => Option[DomainElement],
                               scheme: ParametrizedSecurityScheme,
                               parent: String): SecurityScheme = {
    scheme.fields
      .entry(ParametrizedSecuritySchemeModel.Scheme)
      .fold(SecurityScheme(scheme.annotations).withName(scheme.name)) { f =>
        finder(f.scalar.toString) match {
          case Some(s: SecurityScheme) =>
            val cloned = s.cloneScheme(parent)

            validateSettings(s.settings, scheme.settings).foreach(cloned.set(SecuritySchemeModel.Settings, _))

            cloned
          case _ => throw new Exception(s"Security scheme '${f.scalar.toString}' is not declared.")
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
    element.fields.remove(field)
    result
  }

  private def merge(root: Option[Seq[ParametrizedSecurityScheme]],
                    ep: Option[Seq[ParametrizedSecurityScheme]]): Option[Seq[ParametrizedSecurityScheme]] =
    ep.orElse(root).filter(_.nonEmpty)

  override def resolve(model: BaseUnit): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
        resolveSecurity(model.findById, doc.encodes.asInstanceOf[WebApi])
      case _ =>
    }

    model
  }
}
