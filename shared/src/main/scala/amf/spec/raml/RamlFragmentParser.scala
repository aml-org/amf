package amf.spec.raml

import amf.compiler.RamlFragmentHeader._
import amf.compiler.{RamlFragmentHeader, Root}
import amf.document.Fragment._
import amf.domain.extensions.CustomDomainProperty
import amf.parser.YValueOps
import amf.shape.Shape
import amf.spec.Declarations
import org.yaml.model.YMap

/**
  *
  */
case class RamlFragmentParser(override val root: Root) extends RamlSpecParser(root) {

  def parseFragment(): Fragment = {
    // first i must identify the type of fragment

    val rootMap: YMap =
      root.document.value.map(_.toMap).getOrElse(throw new RuntimeException("Cannot parse empty map"))

    val fragment: Fragment = RamlFragmentHeader(root) match {
      case Some(Raml10DocumentationItem)         => DocumentationItemFragmentParser(rootMap).parse()
      case Some(Raml10DataType)                  => DataTypeFragmentParser(rootMap).parse()
      case Some(Raml10NamedExample)              => throw new IllegalStateException("to be implemented")
      case Some(Raml10ResourceType)              => throw new IllegalStateException("to be implemented")
      case Some(Raml10Trait)                     => throw new IllegalStateException("to be implemented")
      case Some(Raml10AnnotationTypeDeclaration) => AnnotationFragmentParser(rootMap).parse()
      case _                                     => throw new IllegalStateException("Unsupported fragment type")
    }

    UsageParser(rootMap, fragment).parse()

    val environmentRef = ReferencesParser(rootMap, root.references).parse()
    if (environmentRef.nonEmpty) fragment.withReferences(environmentRef.values.toSeq)
    fragment
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
        RamlTypeParser(map, "type", map, (shape: Shape) => shape.adopted(root.location), Declarations()).parse()
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
}
