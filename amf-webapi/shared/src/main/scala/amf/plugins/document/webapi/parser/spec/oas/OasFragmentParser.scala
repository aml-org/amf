package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.model.document._
import amf.core.model.domain.{ExternalDomainElement, Shape}
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.Annotations
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.OasHeader
import amf.plugins.document.webapi.parser.OasHeader._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain.RamlNamedExampleParser
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import org.yaml.model.{YMap, YType}

/**
  *
  */
case class OasFragmentParser(root: Root, fragment: Option[OasHeader] = None)(implicit val ctx: WebApiContext)
    extends OasSpecParser {

  def parseFragment(): Fragment = {
    // first i must identify the type of fragment
    val map: YMap = root.parsed.document.to[YMap] match {
      case Right(m) => m
      case _ =>
        ctx.violation(root.location, "Cannot parse empty map", root.parsed.document)
        YMap.empty
    }

    val references = ReferencesParser("x-uses", map, root.references).parse(root.location)

    val fragment = (detectType() map {
      case Oas20DocumentationItem         => DocumentationItemFragmentParser(map).parse()
      case Oas20DataType                  => DataTypeFragmentParser(map).parse()
      case Oas20ResourceType              => ResourceTypeFragmentParser(map).parse()
      case Oas20Trait                     => TraitFragmentParser(map).parse()
      case Oas20AnnotationTypeDeclaration => AnnotationFragmentParser(map).parse()
      case Oas20SecurityScheme            => SecuritySchemeFragmentParser(map).parse()
      case Oas20NamedExample              => NamedExampleFragmentParser(map).parse()
    }).getOrElse {
      val fragment = ExternalFragment().withEncodes(ExternalDomainElement().withRaw(root.raw))
      ctx.violation(fragment.id, "Unsupported oas type", map)
      fragment
    }

    fragment
      .withLocation(root.location)
      .add(Annotations(root.parsed.document))

    UsageParser(map, fragment).parse()

    if (references.references.nonEmpty) fragment.withReferences(references.solvedReferences())
    fragment
  }

  def detectType(): Option[OasHeader] = fragment.orElse(OasHeader(root))

  case class DocumentationItemFragmentParser(map: YMap) {
    def parse(): DocumentationItemFragment = {

      val item = DocumentationItemFragment().adopted(root.location)

      item.withEncodes(OasCreativeWorkParser(map).parse())

      item
    }
  }

  case class DataTypeFragmentParser(map: YMap) {
    def parse(): DataTypeFragment = {
      val dataType = DataTypeFragment().adopted(root.location)

      val shapeOption =
        OasTypeParser(map, "type", map, (shape: Shape) => shape.withId(root.location + "#shape"), "schema")
          .parse()
      shapeOption.map(dataType.withEncodes(_))

      dataType
      //
    }
  }

  case class AnnotationFragmentParser(map: YMap) {
    def parse(): AnnotationTypeDeclarationFragment = {
      val annotation = AnnotationTypeDeclarationFragment().adopted(root.location)

      val property =
        AnnotationTypesParser(map,
                              "annotation",
                              map,
                              (annotation: CustomDomainProperty) => annotation.adopted(root.location)).parse()

      annotation.withEncodes(property)
    }
  }

  case class ResourceTypeFragmentParser(map: YMap) {
    def parse(): ResourceTypeFragment = {
      val resourceType = ResourceTypeFragment().adopted(root.location)

      val abstractDeclaration =
        new AbstractDeclarationParser(ResourceType(map), resourceType.id, "resourceType", map).parse()

      resourceType.withEncodes(abstractDeclaration)

    }
  }

  case class TraitFragmentParser(map: YMap) {
    def parse(): TraitFragment = {
      val traitFragment = TraitFragment().adopted(root.location)

      val abstractDeclaration =
        new AbstractDeclarationParser(Trait(map), traitFragment.id, "trait", map).parse()

      traitFragment.withEncodes(abstractDeclaration)
    }
  }

  case class SecuritySchemeFragmentParser(map: YMap) {
    def parse(): SecuritySchemeFragment = {
      val security = SecuritySchemeFragment().adopted(root.location)

      security.withEncodes(
        OasSecuritySchemeParser(map,
                                "securityDefinitions",
                                map,
                                (security: amf.plugins.domain.webapi.models.security.SecurityScheme) =>
                                  security.adopted(root.location))
          .parse())
    }
  }

  case class NamedExampleFragmentParser(map: YMap) {
    def parse(): NamedExampleFragment = {
      val entries      = map.entries.filter(e => e.key.as[String] != "x-fragment-type")
      val namedExample = NamedExampleFragment().adopted(root.location)

      namedExample.withEncodes(RamlNamedExampleParser(entries.head).parse())
    }
  }
}
