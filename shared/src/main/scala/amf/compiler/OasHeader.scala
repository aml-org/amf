package amf.compiler

import amf.compiler.FragmentTypes._
import amf.parser.{YMapOps, YNodeLikeOps, YScalarYRead}
import org.yaml.model.{YMap, YScalar}

/**
  *
  */
class OasHeader private[compiler] (val key: String, val value: String) {}

object OasFragmentHeader {

  val extentionName = "x-fragment-type"

  object Oas20DocumentationItem extends OasHeader(extentionName, "2.0 DocumentationItem")

  object Oas20DataType extends OasHeader(extentionName, "2.0 DataType")

  object Oas20NamedExample extends OasHeader(extentionName, "2.0 NamedExample")

  object Oas20ResourceType extends OasHeader(extentionName, "2.0 ResourceType")

  object Oas20Trait extends OasHeader(extentionName, "2.0 Trait")

  object Oas20AnnotationTypeDeclaration extends OasHeader(extentionName, "2.0 AnnotationTypeDeclaration")

  object Oas20Extension extends OasHeader(extentionName, "2.0 Extension")

  object Oas20Overlay extends OasHeader(extentionName, "2.0 Overlay")

  object Oas20SecurityScheme extends OasHeader(extentionName, "2.0 SecurityScheme")

  def apply(root: Root): Option[OasHeader] = {
    root.document
      .toOption[YMap]
      .flatMap(map => {
        val headerOption = for {
          node         <- map.key(extentionName).map(_.value)
          fragmentType <- node.toOption[YScalar].flatMap(s => apply(s.text))
        } yield fragmentType

        headerOption.orElse(toOasType(FragmentTypes(map)))
      })
  }

  def apply(text: String): Option[OasHeader] = text match {
    case Oas20DocumentationItem.value         => Some(Oas20DocumentationItem)
    case Oas20DataType.value                  => Some(Oas20DataType)
    case Oas20NamedExample.value              => Some(Oas20NamedExample)
    case Oas20ResourceType.value              => Some(Oas20ResourceType)
    case Oas20Trait.value                     => Some(Oas20Trait)
    case Oas20AnnotationTypeDeclaration.value => Some(Oas20AnnotationTypeDeclaration)
    case Oas20Extension.value                 => Some(Oas20Extension)
    case Oas20Overlay.value                   => Some(Oas20Overlay)
    case Oas20SecurityScheme.value            => Some(Oas20SecurityScheme)
    case _                                    => None
  }

  def toOasType(fragmentType: FragmentType): Option[OasHeader] =
    fragmentType match {
      case DataTypeFragment          => Some(Oas20DataType)
      case ResourceTypeFragment      => Some(Oas20ResourceType)
      case TraitFragment             => Some(Oas20Trait)
      case AnnotationTypeFragment    => Some(Oas20AnnotationTypeDeclaration)
      case DocumentationItemFragment => Some(Oas20DocumentationItem)
      case ExtensionFragment         => Some(Oas20Extension)
      case OverlayFragment           => Some(Oas20Overlay)
      case SecuritySchemeFragment    => Some(Oas20SecurityScheme)
      case NamedExampleFragment      => Some(Oas20NamedExample)
      case _                         => None // UnknowFragment
    }

}
