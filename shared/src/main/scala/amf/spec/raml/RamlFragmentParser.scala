package amf.spec.raml

import amf.compiler.RamlFragmentHeader._
import amf.compiler.{RamlFragment, Root}
import amf.document.Fragment._
import amf.domain.Annotation.SourceVendor
import amf.domain.Annotations
import amf.domain.`abstract`.{ResourceType, Trait}
import amf.domain.extensions.CustomDomainProperty
import amf.metadata.document.FragmentsTypesModels.{ExtensionModel, OverlayModel}
import amf.model.AmfScalar
import amf.parser._
import amf.parser.YValueOps
import amf.remote.Raml
import amf.shape.Shape
import amf.spec.Declarations
import amf.spec.declaration._
import org.yaml.model.YMap

/**
  *
  */
case class RamlFragmentParser(root: Root, fragmentType: RamlFragment) extends RamlSpecParser {

  def parseFragment(): Fragment = {
    // first i must identify the type of fragment

    val rootMap: YMap =
      root.document.value.map(_.toMap).getOrElse(throw new RuntimeException("Cannot parse empty map"))

    val fragment: Fragment = fragmentType match {
      case Raml10DocumentationItem         => DocumentationItemFragmentParser(rootMap).parse()
      case Raml10DataType                  => DataTypeFragmentParser(rootMap).parse()
      case Raml10ResourceType              => ResourceTypeFragmentParser(rootMap).parse()
      case Raml10Trait                     => TraitFragmentParser(rootMap).parse()
      case Raml10AnnotationTypeDeclaration => AnnotationFragmentParser(rootMap).parse()
      case Raml10Extension                 => ExtensionFragmentParser(rootMap).parse()
      case Raml10Overlay                   => OverlayFragmentParser(rootMap).parse()
      case Raml10SecurityScheme            => SecuritySchemeFragmentParser(rootMap).parse()
      case _                               => throw new IllegalStateException("Unsupported fragment type")
    }

    UsageParser(rootMap, fragment).parse()

    fragment.add(Annotations(root.document) += SourceVendor(Raml))

    val references = ReferencesParser("uses", rootMap, root.references).parse()

    if (references.references.nonEmpty) fragment.withReferences(references.solvedReferences())
    fragment
  }

  case class DocumentationItemFragmentParser(map: YMap) {
    def parse(): DocumentationItem = {

      val item = DocumentationItem().adopted(root.location)

      item.withEncodes(RamlCreativeWorkParser(map, withExtention = true).parse())

      item
    }
  }

  case class DataTypeFragmentParser(map: YMap) {
    def parse(): DataType = {
      val dataType = DataType().adopted(root.location)

      RamlTypeParser(map, "type", map, (shape: Shape) => shape.adopted(root.location), Declarations())
        .parse()
        .foreach(dataType.withEncodes(_))

      dataType
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

  case class ExtensionFragmentParser(map: YMap) {
    def parse(): ExtensionFragment = {
      val extension = ExtensionFragment().adopted(root.location)

      val api = RamlDocumentParser(root).parseWebApi(map, Declarations())
      extension.withEncodes(api)

      map
        .key("extends")
        .foreach(e => {
          root.references
            .find(_.parsedUrl == e.value.value.toScalar.text)
            .foreach(extend =>
              extension
                .set(ExtensionModel.Extends, AmfScalar(extend.baseUnit.id, Annotations(e.value)), Annotations(e)))
        })

      extension
    }
  }

  case class OverlayFragmentParser(map: YMap) {
    def parse(): OverlayFragment = {
      val overlay = OverlayFragment().adopted(root.location)

      val api = RamlDocumentParser(root).parseWebApi(map, Declarations())
      overlay.withEncodes(api)

      map
        .key("extends")
        .foreach(e => {
          root.references
            .find(_.parsedUrl == e.value.value.toScalar.text)
            .foreach(extend =>
              overlay.set(OverlayModel.Extends, AmfScalar(extend.baseUnit.id, Annotations(e.value)), Annotations(e)))
        })

      overlay
    }
  }

  case class SecuritySchemeFragmentParser(map: YMap) {
    def parse(): SecurityScheme = {
      val security = SecurityScheme().adopted(root.location)

      security.withEncodes(
        RamlSecuritySchemeParser(map,
                                 "securityDefinitions",
                                 map,
                                 (security: amf.domain.security.SecurityScheme) => security.adopted(root.location),
                                 Declarations())
          .parse())
    }
  }

}
