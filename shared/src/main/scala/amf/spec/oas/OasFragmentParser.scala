package amf.spec.oas

import amf.compiler.OasFragmentHeader._
import amf.compiler.{OasFragmentHeader, OasHeader, Root}
import amf.document.Fragment._
import amf.domain.`abstract`.{ResourceType, Trait}
import amf.domain.extensions.CustomDomainProperty
import amf.domain.{Annotations, ExternalDomainElement}
import amf.metadata.document.FragmentsTypesModels.{ExtensionModel, OverlayModel}
import amf.model.AmfScalar
import amf.parser._
import amf.shape.Shape
import amf.spec.declaration._
import amf.spec.domain.RamlNamedExampleParser
import amf.spec.{Declarations, ParserContext}
import org.yaml.model.{YMap, YType}

/**
  *
  */
case class OasFragmentParser(root: Root, fragment: Option[OasHeader] = None)(implicit val ctx: ParserContext)
    extends OasSpecParser {

  def parseFragment(): Fragment = {
    // first i must identify the type of fragment
    val rootMap: YMap = root.document.to[YMap] match {
      case Right(map) => map
      case _ =>
        ctx.violation(root.location, "Cannot parse empty map", root.document)
        YMap()
    }

    val fragment = (detectType() map {
      case Oas20DocumentationItem         => DocumentationItemFragmentParser(rootMap).parse()
      case Oas20DataType                  => DataTypeFragmentParser(rootMap).parse()
      case Oas20ResourceType              => ResourceTypeFragmentParser(rootMap).parse()
      case Oas20Trait                     => TraitFragmentParser(rootMap).parse()
      case Oas20AnnotationTypeDeclaration => AnnotationFragmentParser(rootMap).parse()
      case Oas20Extension                 => ExtensionFragmentParser(rootMap).parse()
      case Oas20Overlay                   => OverlayFragmentParser(rootMap).parse()
      case Oas20SecurityScheme            => SecuritySchemeFragmentParser(rootMap).parse()
      case Oas20NamedExample              => NamedExampleFragmentParser(rootMap).parse()
    }).getOrElse {
      val fragment = ExternalFragment().withEncodes(ExternalDomainElement().withRaw(root.raw))
      ctx.violation(fragment.id, "Unsuported oas type", rootMap)
      fragment
    }

    fragment
      .add(Annotations(root.document))

    UsageParser(rootMap, fragment).parse()

    val references = ReferencesParser("x-uses", rootMap, root.references).parse(root.location)

    if (references.references.nonEmpty) fragment.withReferences(references.solvedReferences())
    fragment
  }

  def detectType(): Option[OasHeader] = {
    fragment match {
      case t if t.isDefined => t
      case _                => OasFragmentHeader(root)
    }
  }

  case class DocumentationItemFragmentParser(map: YMap) {
    def parse(): DocumentationItem = {

      val item = DocumentationItem().adopted(root.location)

      item.withEncodes(OasCreativeWorkParser(map).parse())

      item
    }
  }

  case class DataTypeFragmentParser(map: YMap) {
    def parse(): DataType = {
      val dataType = DataType().adopted(root.location)

      val shapeOption =
        OasTypeParser(map, "type", map, (shape: Shape) => shape.adopted(root.location), Declarations(), "schema")
          .parse()
      shapeOption.map(dataType.withEncodes(_))

      dataType
      //
    }
  }

  case class AnnotationFragmentParser(map: YMap) {
    def parse(): AnnotationTypeDeclaration = {
      val annotation = AnnotationTypeDeclaration().adopted(root.location)

      val property = AnnotationTypesParser(map,
                                           "annotation",
                                           map,
                                           (annotation: CustomDomainProperty) => annotation.adopted(root.location),
                                           Declarations()).parse()

      annotation.withEncodes(property)
    }
  }

  case class ResourceTypeFragmentParser(map: YMap) {
    def parse(): ResourceTypeFragment = {
      val resourceType = ResourceTypeFragment().adopted(root.location)

      val abstractDeclaration =
        new AbstractDeclarationParser(ResourceType(map), resourceType.id, "resourceType", map, Declarations()).parse()

      resourceType.withEncodes(abstractDeclaration)

    }
  }

  case class TraitFragmentParser(map: YMap) {
    def parse(): TraitFragment = {
      val traitFragment = TraitFragment().adopted(root.location)

      val abstractDeclaration =
        new AbstractDeclarationParser(Trait(map), traitFragment.id, "trait", map, Declarations()).parse()

      traitFragment.withEncodes(abstractDeclaration)
    }
  }

  case class ExtensionFragmentParser(map: YMap) {
    def parse(): ExtensionFragment = {
      val extension = ExtensionFragment().adopted(root.location)

      val api = OasDocumentParser(root).parseWebApi(map, Declarations())
      extension.withEncodes(api)

      map
        .key("x-extends")
        .foreach(e => {
          ctx.link(e.value) match {
            case Left(url) =>
              root.references
                .find(_.parsedUrl == url)
                .foreach(extend =>
                  extension
                    .set(ExtensionModel.Extends, AmfScalar(extend.baseUnit.id, Annotations(e.value)), Annotations(e)))
            case _ =>
          }
        })

      extension
    }
  }

  case class OverlayFragmentParser(map: YMap) {
    def parse(): OverlayFragment = {
      val overlay = OverlayFragment().adopted(root.location)

      val api = OasDocumentParser(root).parseWebApi(map, Declarations())
      overlay.withEncodes(api)

      map
        .key("x-extends")
        .foreach(e => {
          ctx.link(e.value) match {
            case Left(url) =>
              root.references
                .find(_.parsedUrl == url)
                .foreach(extend =>
                  overlay
                    .set(OverlayModel.Extends, AmfScalar(extend.baseUnit.id, Annotations(e.value)), Annotations(e)))
            case _ =>
          }
        })

      overlay
    }

  }

  case class SecuritySchemeFragmentParser(map: YMap) {
    def parse(): SecurityScheme = {
      val security = SecurityScheme().adopted(root.location)

      security.withEncodes(
        OasSecuritySchemeParser(map,
                                "securityDefinitions",
                                map,
                                (security: amf.domain.security.SecurityScheme) => security.adopted(root.location),
                                Declarations())
          .parse())
    }
  }

  case class NamedExampleFragmentParser(map: YMap) {
    def parse(): NamedExample = {
      val entries      = map.entries.filter(e => e.value.tagType == YType.Map)
      val namedExample = NamedExample().adopted(root.location)

      if (entries.size == 1) namedExample.withEncodes(RamlNamedExampleParser(entries.head).parse())
      else
        throw new IllegalStateException(
          "Could not identified the named example in fragment because it contains more than one named map.")
    }
  }
}
