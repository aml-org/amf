package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.Annotations
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.{OasWebApiContext, WebApiContext}
import amf.plugins.domain.webapi.models.security.{Scope, SecurityRequirement}
import amf.validations.ParserSideValidations.{InvalidSecurityRequirementObject, InvalidSecuritySchemeObject}
import org.yaml.model.{YMap, YNode, YScalar, YSequence}

case class OasSecurityRequirementParser(node: YNode, producer: String => SecurityRequirement, idCounter: IdCounter) (implicit val ctx: OasWebApiContext){
  def parse(): Option[SecurityRequirement] = node.to[YMap] match {
    case Right(map) if map.entries.nonEmpty =>
      val securityRequirement = producer(idCounter.genId("requirement"))

      // Parse individual schemes
      map.entries.foreach { entry =>
        val scheme = securityRequirement.withScheme()

        entry.key.to[YScalar] match {
          case Right(key) =>
            scheme.withName(key.text)
          case _ =>
            ctx.violation(InvalidSecuritySchemeObject, scheme.id, s"Invalid security scheme $entry", entry)
        }

        scheme.adopted(securityRequirement.id) // Re-adopt to force setting name to id

        entry.value.to[YSequence] match {
          case Right(seq) if !seq.isEmpty =>
            val scopes = seq.nodes.map {
              node => node.asScalar match {
                case Some(scalar) =>
                  Scope().withName(scalar.text)
                case None =>
                  val scope = Scope()
                  ctx.violation(InvalidSecurityRequirementObject, scope.id, s"Scopes must be a string array $node", node)
                  scope
              }
            }
            val settings = scheme.withOAuth2Settings()
            val flow = settings.withFlow()
            flow.withScopes(scopes)
          case Right(seq) if seq.isEmpty =>
            scheme.withDefaultSettings()
          case _ =>
            ctx.violation(InvalidSecuritySchemeObject, scheme.id, s"Invalid security scheme $entry", entry)
        }
      }
      Some(securityRequirement)
    case Right(map) if map.entries.isEmpty =>
      None
    case _ =>
      val requirement = producer(node.toString)
      ctx.violation(InvalidSecurityRequirementObject, requirement.id, s"Invalid security requirement $node", node)
      None
  }
}


object RamlSecurityRequirementParser {
  def parse(producer: String => SecurityRequirement)(node: YNode)(
    implicit ctx: WebApiContext): SecurityRequirement = {
    RamlSecurityRequirementParser(node, producer).parse()
  }
}
case class RamlSecurityRequirementParser(node: YNode, producer: String => SecurityRequirement) (implicit val ctx: WebApiContext) {
  def parse(): SecurityRequirement = {
    val requirement = producer("default-requirement").add(Annotations(node))
    RamlParametrizedSecuritySchemeParser(node, requirement.withScheme().withName).parse()
    requirement
  }

}
