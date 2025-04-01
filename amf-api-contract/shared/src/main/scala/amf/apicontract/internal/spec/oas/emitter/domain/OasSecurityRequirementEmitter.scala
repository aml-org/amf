package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.security.{
  ApiKeySettings,
  HttpSettings,
  MutualTLSSettings,
  OAuth2Settings,
  OpenIdConnectSettings,
  Scope,
  SecurityRequirement
}
import amf.apicontract.internal.metamodel.domain.security.ParametrizedSecuritySchemeModel
import amf.apicontract.internal.spec.common.emitter.AbstractSecurityRequirementEmitter
import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{ScalarEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import org.yaml.model.YDocument.PartBuilder

class OasSecurityRequirementEmitter(requirement: SecurityRequirement, ordering: SpecOrdering)
    extends AbstractSecurityRequirementEmitter(requirement, ordering) {

  override def emit(b: PartBuilder): Unit = {
    b.obj { eb =>
      requirement.schemes.foreach { parametrizedScheme =>
        val fs = parametrizedScheme.fields
        fs.entry(ParametrizedSecuritySchemeModel.Settings) match {
          case Some(f) =>
            val scopes = emitScopes(f)
            eb.entry(parametrizedScheme.name.value(), _.list(traverse(ordering.sorted(scopes), _)))
          case None =>
            eb.entry(parametrizedScheme.name.value(), _.list(_ => {}))
        }
      }
    }
  }

  private def emitScopes(scopesEntry: FieldEntry): Seq[ScalarEmitter] = {
    scopesEntry.element match {
      case oAuth2Settings: OAuth2Settings =>
        oAuth2Settings.flows.headOption.toList
          .flatMap { flow =>
            flow.scopes.map(s => ScalarEmitter(AmfScalar(s.name.value(), s.annotations)))
          }
      case openIdConnectSettings: OpenIdConnectSettings =>
        openIdConnectSettings.scopes.map(s => ScalarEmitter(AmfScalar(s.name.value(), s.annotations)))
      case _ => // we cant emit, if its not 2.0 isnt valid in oas.
        emitAdditionalScopes(scopesEntry)
    }
  }

  protected def emitAdditionalScopes(scopesEntry: FieldEntry): Seq[ScalarEmitter] = Nil

  override def position(): Position = pos(requirement.annotations)
}

object OasSecurityRequirementEmitter {
  def apply(requirement: SecurityRequirement, ordering: SpecOrdering) =
    new OasSecurityRequirementEmitter(requirement, ordering)
}

case class Oas31SecurityRequirementEmitter(requirement: SecurityRequirement, ordering: SpecOrdering)
    extends OasSecurityRequirementEmitter(requirement, ordering) {

  override protected def emitAdditionalScopes(scopesEntry: FieldEntry): Seq[ScalarEmitter] = {
    scopesEntry.element match {
      case httpSettings: HttpSettings           => emitScopes(httpSettings.scopes)
      case apiKeySettings: ApiKeySettings       => emitScopes(apiKeySettings.scopes)
      case mutualTLSSettings: MutualTLSSettings => emitScopes(mutualTLSSettings.scopes)
      case _                                    => Nil
    }
  }

  private def emitScopes(scopes: Seq[Scope]): Seq[ScalarEmitter] = {
    scopes.map(s => ScalarEmitter(AmfScalar(s.name.value(), s.annotations)))
  }
}

object Oas31SecurityRequirementEmitter {
  def apply(requirement: SecurityRequirement, ordering: SpecOrdering) =
    new Oas31SecurityRequirementEmitter(requirement, ordering)
}
