package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.metamodel.document.FragmentModel
import amf.core.model.document.{Fragment, ExternalFragment}
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{Shape, AmfScalar, ExternalDomainElement}
import amf.core.parser.{Annotations, _}
import amf.core.remote.Raml10
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.RamlFragment
import amf.plugins.document.webapi.parser.RamlFragmentHeader._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.validations.ParserSideValidations.InvalidFragmentType
import org.yaml.model.{YMap, YScalar}

/**
  *
  */
case class RamlFragmentParser(root: Root, fragmentType: RamlFragment)(implicit val ctx: RamlWebApiContext)
    extends RamlSpecParser {

  def parseFragment(): Option[Fragment] = {
    // first i must identify the type of fragment

    val rootMap: YMap = root.parsed.asInstanceOf[SyamlParsedDocument].document.to[YMap] match {
      case Right(map) => map
      case _          =>
        // we need to check if named example fragment in order to support invalid structures as external fragment
        if (fragmentType != Raml10NamedExample)
          ctx.violation(InvalidFragmentType,
                        root.location,
                        "Cannot parse empty map",
                        root.parsed.asInstanceOf[SyamlParsedDocument].document)
        YMap.empty
    }

    val (references, aliases) = ReferencesParserAnnotations("uses", rootMap, root)

    // usage is valid for a fragment, not for the encoded domain element
    val encodedMap = YMap(
      rootMap.entries.filter(e => e.key.as[YScalar].text != "usage" && e.key.as[YScalar].text != "uses"),
      root.location)

    val optionFragment = fragmentType match {
      case Raml10DocumentationItem         => Some(DocumentationItemFragmentParser(encodedMap).parse())
      case Raml10DataType                  => Some(DataTypeFragmentParser(encodedMap).parse())
      case Raml10ResourceType              => Some(ResourceTypeFragmentParser(encodedMap).parse())
      case Raml10Trait                     => Some(TraitFragmentParser(encodedMap).parse())
      case Raml10AnnotationTypeDeclaration => Some(AnnotationFragmentParser(encodedMap).parse())
      case Raml10SecurityScheme            => Some(SecuritySchemeFragmentParser(encodedMap).parse())
      case Raml10NamedExample              => Some(NamedExampleFragmentParser(encodedMap).parse())
      case _                               => None
    }

    optionFragment match {
      case Some(fragment) =>
        rootMap.key("usage", usage => {
          fragment.set(
            FragmentModel.Usage,
            AmfScalar(usage.value.as[String], Annotations(usage.value)),
            Annotations(usage.value)
          )
        })
        fragment.withLocation(root.location)
        UsageParser(rootMap, fragment).parse()
        fragment.add(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))
        if (aliases.isDefined) fragment.annotations += aliases.get
        fragment.encodes.add(SourceVendor(Raml10))
        if (references.references.nonEmpty) fragment.withReferences(references.solvedReferences())
        Some(fragment)
      case _ =>
        None
    }
  }

  private def buildExternalFragment(): ExternalFragment = {
    ExternalFragment()
      .withLocation(root.location)
      .withId(root.location)
      .withEncodes(
        ExternalDomainElement()
          .withRaw(root.raw)
          .withMediaType(root.mediatype))
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
        (shape: Shape) => shape.withId(root.location + "#/shape"), // TODO: this is being ignored
        StringDefaultType
      ).parse()
        .foreach(dataType.withEncodes)

      dataType
    }
  }

  case class ResourceTypeFragmentParser(map: YMap) {
    def parse(): ResourceTypeFragment = {
      val resourceType = ResourceTypeFragment().adopted(root.location + "#")

      val abstractDeclaration =
        new AbstractDeclarationParser(ResourceType(map).withId(resourceType.id), resourceType.id, "resourceType", map)
          .parse()

      resourceType.withEncodes(abstractDeclaration)

    }
  }

  case class TraitFragmentParser(map: YMap) {
    def parse(): TraitFragment = {
      val traitFragment = TraitFragment().adopted(root.location + "#")

      val abstractDeclaration =
        new AbstractDeclarationParser(Trait(map).withId(traitFragment.id), traitFragment.id, "trait", map)
          .parse()

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
                              (annotation: CustomDomainProperty) => annotation.adopted(root.location + "#/")).parse()

      annotation.withEncodes(property)
    }
  }

  case class SecuritySchemeFragmentParser(map: YMap) {
    def parse(): SecuritySchemeFragment = {
      val security = SecuritySchemeFragment().adopted(root.location)

      security.withEncodes(
        RamlSecuritySchemeParser(map,
                                 (security: amf.plugins.domain.webapi.models.security.SecurityScheme) =>
                                   security.adopted(root.location + "#/"))
          .parse())
    }
  }

  case class NamedExampleFragmentParser(map: YMap) {
    def parse(): Fragment = buildExternalFragment()
  }
}
