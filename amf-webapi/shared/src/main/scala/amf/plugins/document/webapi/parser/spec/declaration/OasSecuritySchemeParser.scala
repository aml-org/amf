package amf.plugins.document.webapi.parser.spec.declaration
import amf.core.annotations.LexicalInformation
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import org.yaml.model.{YType, YMap, YPart}
import amf.validations.ParserSideValidations.{
  MissingSecuritySchemeErrorSpecification,
  CrossSecurityWarningSpecification
}
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.document.webapi.parser.spec.toRaml
import amf.core.parser.{Annotations, Range, YMapOps}
import amf.core.utils.AmfStrings

case class OasSecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit ctx: OasWebApiContext)
    extends OasLikeSecuritySchemeParser(part, adopt) {

  override def parse(): SecurityScheme = {
    val node = getNode

    ctx.link(node) match {
      case Left(link) => parseReferenced(link, node, adopt)
      case Right(value) =>
        val scheme = adopt(SecurityScheme(part))
        val map    = value.as[YMap]

        // 3 stages
        // 2 pipes

        map.key("type", SecuritySchemeModel.Type in scheme)

        crossSecurityWarnings(scheme)

        map.key(
          "type",
          value => {
            // we need to check this because of the problem parsing nulls like empty strings of value null
            if (value.value.tagType == YType.Null && scheme.`type`.option().contains("")) {
              ctx.eh.violation(
                MissingSecuritySchemeErrorSpecification,
                scheme.id,
                Some(SecuritySchemeModel.Type.value.iri()),
                "Security Scheme must have a mandatory value from 'oauth2', 'basic' or 'apiKey'",
                Some(LexicalInformation(Range(map.range))),
                Some(ctx.rootContextDocument)
              )
            }
          }
        )

        scheme.normalizeType() // normalize the common type

        map.key("displayName".asOasExtension, SecuritySchemeModel.DisplayName in scheme)
        map.key("description", SecuritySchemeModel.Description in scheme)

        RamlDescribedByParser("describedBy".asOasExtension, map, scheme)(toRaml(ctx)).parse()

        ctx.factory
          .securitySettingsParser(map, scheme)
          .parse()
          .map { settings =>
            scheme.set(SecuritySchemeModel.Settings, settings, Annotations(map))
          }

        AnnotationParser(scheme, map).parse()

        scheme
    }
  }
  override def crossSecurityWarnings(scheme: SecurityScheme): Unit = {
    scheme.`type`.option() match {
      case Some(s) if s.startsWith("x-") =>
        ctx.eh.warning(
          CrossSecurityWarningSpecification,
          scheme.id,
          Some(SecuritySchemeModel.Type.value.iri()),
          s"RAML 1.0 extension security scheme type '$s' detected in OAS 2.0 spec",
          scheme.`type`.annotations().find(classOf[LexicalInformation]),
          Some(ctx.rootContextDocument)
        )
      case Some("OAuth 1.0" | "OAuth 2.0" | "Basic Authentication" | "Digest Authentication" | "Pass Through") =>
        ctx.eh.warning(
          CrossSecurityWarningSpecification,
          scheme.id,
          Some(SecuritySchemeModel.Type.value.iri()),
          s"RAML 1.0 security scheme type detected in OAS 2.0 spec",
          scheme.`type`.annotations().find(classOf[LexicalInformation]),
          Some(ctx.rootContextDocument)
        )
      case _ =>
    }

  }
}
