package amf.compiler

import amf.parser.{YMapOps, YValueOps}
import FragmentTypes._

/**
  *
  */
class OasHeader private[compiler] (val key: String, val value: String) {}

object OasHeader {

  val Oas20: OasHeader = new OasHeader("swagger", "2.0")

  def apply(root: Root): Option[OasHeader] = {
    for {
      map     <- root.document.value.flatMap(_.asMap)
      value   <- map.key("swagger").map(_.value.value)
      version <- value.asScalar.map(_.text) if version.equals("2.0")
    } yield Oas20
  }
}

object OasFragmentHeader {

  val extentionName = "x-fragment-type"

  object Oas20DocumentationItem extends OasHeader(extentionName, "2.0 DocumentationItem")

  object Oas20DataType extends OasHeader(extentionName, "2.0 DataType")

  object Oas20NamedExample extends OasHeader(extentionName, "2.0 NamedExample")

  object Oas20ResourceType extends OasHeader(extentionName, "2.0 ResourceType")

  object Oas20Trait extends OasHeader(extentionName, "2.0 Trait")

  object Oas20AnnotationTypeDeclaration extends OasHeader(extentionName, "2.0 AnnotationTypeDeclaration")

  def apply(root: Root): Option[OasHeader] = {
    for {
      map                 <- root.document.value.flatMap(_.asMap)
      value               <- map.key(extentionName).map(_.value.value)
      fragmentType        <- value.asScalar.map(s => apply(s.text))
      detectedFragmenType <- fragmentType.orElse(toOasType(FragmentTypes(map)))
    } yield detectedFragmenType
  }

  def apply(text: String): Option[OasHeader] = text match {
    case Oas20DocumentationItem.value         => Some(Oas20DocumentationItem)
    case Oas20DataType.value                  => Some(Oas20DataType)
    case Oas20NamedExample.value              => Some(Oas20NamedExample)
    case Oas20ResourceType.value              => Some(Oas20ResourceType)
    case Oas20Trait.value                     => Some(Oas20Trait)
    case Oas20AnnotationTypeDeclaration.value => Some(Oas20AnnotationTypeDeclaration)
    case _                                    => None
  }

  def toOasType(fragmentType: FragmentType): Option[OasHeader] =
    fragmentType match {
      case DataTypeFragment          => Some(Oas20DataType)
      case ResourceTypeFragment      => Some(Oas20ResourceType)
      case TraitFragment             => Some(Oas20Trait)
      case AnnotationTypeFragment    => Some(Oas20AnnotationTypeDeclaration)
      case DocumentationItemFragment => Some(Oas20DocumentationItem)
      case _                         => None //UnknowFragment
    }

}
