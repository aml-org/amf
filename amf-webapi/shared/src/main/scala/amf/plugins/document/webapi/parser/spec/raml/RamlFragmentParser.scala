package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.model.document.Fragment
import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.Annotations
import amf.core.remote.Raml
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.RamlFragment
import amf.plugins.document.webapi.parser.RamlFragmentHeader._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain.RamlNamedExampleParser
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import org.yaml.model.{YMap, YType}

/**
  *
  */
case class RamlFragmentParser(root: Root,  fragmentType: RamlFragment)(implicit val ctx: WebApiContext) extends RamlSpecParser {

  def parseFragment(): Option[Fragment] = {
    // first i must identify the type of fragment

    val rootMap: YMap = root.parsed.document.to[YMap] match {
      case Right(map) => map
      case _ =>
        ctx.violation(root.location, "Cannot parse empty map", root.parsed.document)
        YMap()
    }

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
        UsageParser(rootMap, fragment).parse()
        fragment.add(Annotations(root.parsed.document) += SourceVendor(Raml))
        val references = ReferencesParser("uses", rootMap, root.references).parse(root.location)
        if (references.references.nonEmpty) fragment.withReferences(references.solvedReferences())
        Some(fragment)
      case _ =>
        None
    }
  }

  case class DocumentationItemFragmentParser(map: YMap) {
    def parse(): DocumentationItemFragment = {

      val item = DocumentationItemFragment().adopted(root.location)

      item.withEncodes(RamlCreativeWorkParser(map, withExtention = true).parse())

      item
    }
  }

  case class DataTypeFragmentParser(map: YMap) {
    def parse(): DataTypeFragment = {
      val dataType = DataTypeFragment().adopted(root.location)

      RamlTypeParser(
        map,
        "type",
        map,
        (shape: Shape) => shape.adopted(root.location),
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
                                 (security: amf.plugins.domain.webapi.models.security.SecurityScheme) => security.adopted(root.location))
          .parse())
    }
  }

  case class NamedExampleFragmentParser(map: YMap) {
    def parse(): NamedExampleFragment = {
      val entries      = map.entries.filter(e => e.value.tagType == YType.Map)
      val namedExample = NamedExampleFragment().adopted(root.location)

      if (entries.size == 1) namedExample.withEncodes(RamlNamedExampleParser(entries.head).parse())
      else
        throw new IllegalStateException(
          "Could not identified the named example in fragment because it contains more than one named map.")
    }
  }

}
