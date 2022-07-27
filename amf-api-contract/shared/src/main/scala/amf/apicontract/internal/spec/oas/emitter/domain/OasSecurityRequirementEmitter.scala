package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.security.{OAuth2Settings, OpenIdConnectSettings, SecurityRequirement}
import amf.apicontract.internal.metamodel.domain.security.ParametrizedSecuritySchemeModel
import amf.apicontract.internal.spec.common.emitter.AbstractSecurityRequirementEmitter
import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.render.BaseEmitters.{ScalarEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
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
