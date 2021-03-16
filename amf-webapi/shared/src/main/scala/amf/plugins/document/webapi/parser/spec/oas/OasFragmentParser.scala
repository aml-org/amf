package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.model.document._
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{ExternalDomainElement, Shape}
import amf.core.parser.{Annotations, ScalarNode, SyamlParsedDocument}
import amf.core.unsafe.PlatformSecrets
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.ExternalJsonYamlRefsPlugin
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.OasHeader
import amf.plugins.document.webapi.parser.OasHeader._
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.SchemaPosition.Schema
import amf.plugins.document.webapi.parser.spec.declaration.{OasTypeParser, _}
import amf.plugins.document.webapi.parser.spec.domain.{ExampleOptions, RamlNamedExampleParser}
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.validations.ParserSideValidations.InvalidFragmentType
import org.yaml.model.{YMap, YMapEntry, YScalar}

case class OasFragmentParser(root: Root, fragment: Option[OasHeader] = None)(implicit val ctx: OasWebApiContext)
    extends OasSpecParser
    with PlatformSecrets {

  def parseFragment(): BaseUnit = {
    // first i must identify the type of fragment
    val map: YMap = root.parsed.asInstanceOf[SyamlParsedDocument].document.to[YMap] match {
      case Right(m) => m
      case _ =>
        ctx.eh.violation(InvalidFragmentType,
                         root.location,
                         "Cannot parse empty map",
                         root.parsed.asInstanceOf[SyamlParsedDocument].document)
        YMap.empty
    }

    val fragment = (detectType() flatMap {
      case Oas20DocumentationItem         => Some(DocumentationItemFragmentParser(map).parse())
      case Oas20DataType                  => Some(DataTypeFragmentParser(map).parse())
      case Oas20ResourceType              => Some(ResourceTypeFragmentParser(map).parse())
      case Oas20Trait                     => Some(TraitFragmentParser(map).parse())
      case Oas20AnnotationTypeDeclaration => Some(AnnotationFragmentParser(map).parse())
      case Oas20SecurityScheme            => Some(SecuritySchemeFragmentParser(map).parse())
      case Oas20NamedExample              => Some(NamedExampleFragmentParser(map).parse())
      case Oas20Header | Oas30Header =>
        new ExternalJsonYamlRefsPlugin().parse(root, ctx, ParsingOptions())
      case _ => None
    }).getOrElse {
      val fragment = ExternalFragment()
        .withLocation(root.location)
        .withId(root.location)
        .withEncodes(ExternalDomainElement().withRaw(root.raw))
      ctx.eh.violation(InvalidFragmentType, fragment.id, "Unsupported oas type", map)
      fragment
    }

    val references = ReferencesParser(fragment, root.location, "uses".asOasExtension, map, root.references).parse()

    fragment
      .withLocation(root.location)
      .add(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))

    UsageParser(map, fragment).parse()

    if (references.nonEmpty) fragment.withReferences(references.baseUnitReferences())
    fragment
  }

  def detectType(): Option[OasHeader] = fragment.orElse(OasHeader(root))

  case class DocumentationItemFragmentParser(map: YMap) {
    def parse(): DocumentationItemFragment = {

      val item = DocumentationItemFragment().adopted(root.location + "#/")

      item.withEncodes(OasLikeCreativeWorkParser(map, item.id).parse())

      item
    }
  }

  case class DataTypeFragmentParser(map: YMap) {
    def parse(): DataTypeFragment = {
      val dataType = DataTypeFragment().adopted(root.location)
      val filterMap = YMap(
        map.entries.filter({
          case c: YMapEntry =>
            val key = ScalarNode(c.key).text().toString
            !key.equals(Oas20DataType.key) && !key.equals(OasHeader.swagger)
          case _ => true
        }),
        map.sourceName
      )

      val shapeOption =
        OasTypeParser(YMapEntryLike(filterMap),
                      "type",
                      (shape: Shape) => shape.withId(root.location + "#/shape"),
                      OAS20SchemaVersion(Schema))
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
                              (annotation: CustomDomainProperty) => annotation.adopted(root.location + "#/")).parse()

      annotation.withEncodes(property)
    }
  }

  case class ResourceTypeFragmentParser(map: YMap) {
    def parse(): ResourceTypeFragment = {
      val resourceType = ResourceTypeFragment().adopted(root.location)

      val abstractDeclaration =
        new AbstractDeclarationParser(ResourceType(map).withId(resourceType.id + "#/"),
                                      resourceType.id,
                                      YMapEntryLike(map)).parse()

      resourceType.withEncodes(abstractDeclaration)

    }
  }

  case class TraitFragmentParser(map: YMap) {
    def parse(): TraitFragment = {
      val traitFragment = TraitFragment().adopted(root.location)

      val abstractDeclaration =
        new AbstractDeclarationParser(Trait(map).withId(traitFragment.id + "#/"), traitFragment.id, YMapEntryLike(map))
          .parse()

      traitFragment.withEncodes(abstractDeclaration)
    }
  }

  case class SecuritySchemeFragmentParser(map: YMap) {
    def parse(): SecuritySchemeFragment = {
      val security = SecuritySchemeFragment().adopted(root.location)

      security.withEncodes(
        ctx.factory
          .securitySchemeParser(map,
                                (security: amf.plugins.domain.webapi.models.security.SecurityScheme) =>
                                  security.adopted(root.location + "#/"))
          .parse())
    }
  }

  case class NamedExampleFragmentParser(map: YMap) {
    def parse(): NamedExampleFragment = {
      val entries      = map.entries.filter(e => e.key.as[YScalar].text != "fragmentType".asOasExtension)
      val namedExample = NamedExampleFragment().adopted(root.location + "#/")

      val producer = (name: Option[String]) => {
        val example = Example()
        name.foreach(example.withName(_))
        namedExample.withEncodes(example)
        example
      }

      namedExample.withEncodes(
        RamlNamedExampleParser(entries.head, producer, ExampleOptions(strictDefault = true, quiet = true)).parse())
    }
  }
}
