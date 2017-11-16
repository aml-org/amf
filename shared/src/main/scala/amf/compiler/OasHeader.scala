package amf.compiler

import amf.compiler.FragmentTypes._
import amf.parser.{YMapOps, YScalarYRead}
import org.yaml.model.YMap

/**
  *
  */
class OasHeader private[compiler] (val key: String, val value: String) {
  def tuple: (String, String) = (key, value)
}

object OasHeader {

  val extensionName = "x-fragment-type"

  val extensionType = "x-extension-type"

  val swagger = "swagger"

  object Oas20Header extends OasHeader("swagger", "2.0")

  object Oas20DocumentationItem extends OasHeader(extensionName, "2.0 DocumentationItem")

  object Oas20DataType extends OasHeader(extensionName, "2.0 DataType")

  object Oas20NamedExample extends OasHeader(extensionName, "2.0 NamedExample")

  object Oas20ResourceType extends OasHeader(extensionName, "2.0 ResourceType")

  object Oas20Trait extends OasHeader(extensionName, "2.0 Trait")

  object Oas20AnnotationTypeDeclaration extends OasHeader(extensionName, "2.0 AnnotationTypeDeclaration")

  object Oas20SecurityScheme extends OasHeader(extensionName, "2.0 SecurityScheme")

  object Oas20Extension extends OasHeader(extensionType, "2.0 Extension")

  object Oas20Overlay extends OasHeader(extensionType, "2.0 Overlay")

  def apply(root: Root): Option[OasHeader] = {
    val map = root.document.as[YMap]

    map
      .key(extensionName)
      .orElse(map.key(extensionType))
      .orElse(map.key(swagger))
      .flatMap(extension => OasHeader(extension.value))
      .orElse(toOasType(FragmentTypes(map)))
  }

  def apply(text: String): Option[OasHeader] = text match {
    case Oas20Header.value                    => Some(Oas20Header)
    case Oas20DocumentationItem.value         => Some(Oas20DocumentationItem)
    case Oas20DataType.value                  => Some(Oas20DataType)
    case Oas20NamedExample.value              => Some(Oas20NamedExample)
    case Oas20ResourceType.value              => Some(Oas20ResourceType)
    case Oas20Trait.value                     => Some(Oas20Trait)
    case Oas20AnnotationTypeDeclaration.value => Some(Oas20AnnotationTypeDeclaration)
    case Oas20SecurityScheme.value            => Some(Oas20SecurityScheme)
    case Oas20Extension.value                 => Some(Oas20Extension)
    case Oas20Overlay.value                   => Some(Oas20Overlay)
    case _                                    => None
  }

  def toOasType(fragmentType: FragmentType): Option[OasHeader] =
    fragmentType match {
      case DataTypeFragment          => Some(Oas20DataType)
      case ResourceTypeFragment      => Some(Oas20ResourceType)
      case TraitFragment             => Some(Oas20Trait)
      case AnnotationTypeFragment    => Some(Oas20AnnotationTypeDeclaration)
      case DocumentationItemFragment => Some(Oas20DocumentationItem)
      case SecuritySchemeFragment    => Some(Oas20SecurityScheme)
      case NamedExampleFragment      => Some(Oas20NamedExample)
      case _                         => None // UnknownFragment
    }

}
