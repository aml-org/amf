package amf.plugins.document.apicontract.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters.{ScalarEmitter, pos, traverse}
import amf.core.emitter.SpecOrdering
import amf.core.model.domain.AmfScalar
import amf.core.parser.Position
import amf.plugins.document.apicontract.parser.spec.domain.AbstractSecurityRequirementEmitter
import amf.plugins.domain.apicontract.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.apicontract.models.security.{OAuth2Settings, OpenIdConnectSettings, SecurityRequirement}
import org.yaml.model.YDocument.PartBuilder

case class OasSecurityRequirementEmitter(requirement: SecurityRequirement, ordering: SpecOrdering)
    extends AbstractSecurityRequirementEmitter(requirement, ordering) {

  override def emit(b: PartBuilder): Unit = {
    b.obj { eb =>
      requirement.schemes.foreach { parametrizedScheme =>
        val fs = parametrizedScheme.fields
        fs.entry(ParametrizedSecuritySchemeModel.Settings) match {
          case Some(f) =>
            val scopes = f.element match {
              case settings: OAuth2Settings =>
                settings.flows.headOption.toList
                  .flatMap { flow =>
                    flow.scopes.map(s => ScalarEmitter(AmfScalar(s.name.value(), s.annotations)))
                  }
              case settings: OpenIdConnectSettings =>
                settings.scopes.map(s => ScalarEmitter(AmfScalar(s.name.value(), s.annotations)))
              case _ => // we cant emit, if its not 2.0 isnt valid in oas.
                Nil
            }

            eb.entry(parametrizedScheme.name.value(), _.list(traverse(ordering.sorted(scopes), _)))

          case None =>
            eb.entry(parametrizedScheme.name.value(), _.list(_ => {}))
        }
      }
    }
  }

  override def position(): Position = pos(requirement.annotations)
}
