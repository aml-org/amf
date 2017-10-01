package amf.spec.oas

import amf.compiler.OasFragmentHeader._
import amf.compiler.{OasFragmentHeader, OasHeader, Root}
import amf.document.Fragment._
import amf.domain.Annotations
import amf.domain.`abstract`.{ResourceType, Trait}
import amf.domain.extensions.CustomDomainProperty
import amf.parser.YValueOps
import amf.shape.Shape
import amf.spec.Declarations
import amf.spec.common.BaseSpecParser.AbstractDeclarationParser
import org.yaml.model.YMap

/**
  *
  */
case class OasFragmentParser(root: Root) extends OasSpecParser(root) {

  def parseFragment(): Fragment = {
    // first i must identify the type of fragment

    val rootMap: YMap =
      root.document.value.map(_.toMap).getOrElse(throw new RuntimeException("Cannot parse empty map"))

    val fragment = detectType() match {
      case Some(Oas20DocumentationItem) => DocumentationItemFragmentParser(rootMap).parse()
      case Some(Oas20DataType)          => DataTypeFragmentParser(rootMap).parse()
      //      case Some(Oas20NamedExample)              =>
      case Some(Oas20ResourceType)              => ResourceTypeFragmentParser(rootMap).parse()
      case Some(Oas20Trait)                     => TraitFragmentParser(rootMap).parse()
      case Some(Oas20AnnotationTypeDeclaration) => AnnotationFragmentParser(rootMap).parse()
      case _                                    => throw new IllegalStateException("Unsuported raml type")
    }

    fragment
      .add(Annotations(root.document))

    UsageParser(rootMap, fragment).parse()

    val environmentRef = ReferencesParser(rootMap, root.references).parse()

    if (environmentRef.nonEmpty)
      fragment.withReferences(environmentRef.values.toSeq)
    fragment
  }

  def detectType(): Option[OasHeader] = {

    OasHeader(root).flatMap(_ => {
      OasFragmentHeader(root)
    })
  }

  case class DocumentationItemFragmentParser(map: YMap) {
    def parse(): DocumentationItem = {

      val item = DocumentationItem().adopted(root.location)

      item.withEncodes(UserDocumentationParser(map).parse())

      item
    }
  }

  case class DataTypeFragmentParser(map: YMap) {
    def parse(): DataType = {
      val dataType = DataType().adopted(root.location)

      val shapeOption =
        OasTypeParser(map, "type", map, (shape: Shape) => shape.adopted(root.location), Declarations()).parse()
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
}
