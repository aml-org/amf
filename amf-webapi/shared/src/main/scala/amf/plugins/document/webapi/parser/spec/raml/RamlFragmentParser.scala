package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.model.document.Fragment
import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.Annotations
import amf.core.remote.Raml10
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.RamlFragment
import amf.plugins.document.webapi.parser.RamlFragmentHeader._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain.RamlNamedExampleParser
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import org.yaml.model.YMap

/**
  *
  */
case class RamlFragmentParser(root: Root, fragmentType: RamlFragment)(implicit val ctx: RamlWebApiContext)
    extends RamlSpecParser {

  def parseFragment(): Option[Fragment] = {
    // first i must identify the type of fragment

    val rootMap: YMap = root.parsed.document.to[YMap] match {
      case Right(map) => map
      case _ =>
        ctx.violation(root.location, "Cannot parse empty map", root.parsed.document)
        YMap.empty
    }

    val references = ReferencesParser("uses", rootMap, root.references).parse(root.location)

    val optionFragment = fragmentType match {
      case Raml10DocumentationItem         => Some(DocumentationItemFragmentParser(rootMap).parse())
      case Raml10DataType                  => Some(DataTypeFragmentParser(rootMap).parse())
      case Raml10ResourceType              => Some(ResourceTypeFragmentParser(rootMap).parse())
      case Raml10Trait                     => Some(TraitFragmentParser(rootMap).parse())
      case Raml10AnnotationTypeDeclaration => Some(AnnotationFragmentParser(rootMap).parse())
      case Raml10SecurityScheme            => Some(SecuritySchemeFragmentParser(rootMap).parse())
      case Raml10NamedExample              => Some(NamedExampleFragmentParser(rootMap).parse())
      case _                               => None
    }

    optionFragment match {
      case Some(fragment) =>
        fragment.withLocation(root.location)
        UsageParser(rootMap, fragment).parse()
        fragment.add(Annotations(root.parsed.document))
        fragment.encodes.add(SourceVendor(Raml10))
        if (references.references.nonEmpty) fragment.withReferences(references.solvedReferences())
        Some(fragment)
      case _ =>
        None
    }
  }

  case class DocumentationItemFragmentParser(map: YMap) {
    def parse(): DocumentationItemFragment = {

      val item = DocumentationItemFragment().adopted(root.location)

      item.withEncodes(RamlCreativeWorkParser(map).parse())

      item
    }
  }

  case class DataTypeFragmentParser(map: YMap) {
    def parse(): DataTypeFragment = {
      val dataType = DataTypeFragment().adopted(root.location)

      Raml10TypeParser(
        map,
        "type",
        map,
        (shape: Shape) => shape.withId(root.location + "#shape"), // TODO: this is being ignored
        isAnnotation = false,
        StringDefaultType
      ).parse()
        .foreach(dataType.withEncodes)

      dataType
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

  case class SecuritySchemeFragmentParser(map: YMap) {
    def parse(): SecuritySchemeFragment = {
      val security = SecuritySchemeFragment().adopted(root.location)

      security.withEncodes(
        RamlSecuritySchemeParser(map,
                                 "securityDefinitions",
                                 map,
                                 (security: amf.plugins.domain.webapi.models.security.SecurityScheme) =>
                                   security.adopted(root.location))
          .parse())
    }
  }

  case class NamedExampleFragmentParser(map: YMap) {
    def parse(): NamedExampleFragment = {
      val entries      = map.entries
      val namedExample = NamedExampleFragment().adopted(root.location)

      val producer = (name: Option[String]) => {
        val example = Example()
        name.foreach(example.withName)
        namedExample.withEncodes(example)
        example
      }

      namedExample.withEncodes(RamlNamedExampleParser(entries.head, producer).parse())
    }
  }

}
