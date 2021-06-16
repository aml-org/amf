package amf.apicontract.internal.spec.oas

import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.{Root, YMapOps, YNodeLikeOps}
import amf.core.internal.utils.AmfStrings
import org.yaml.model.YMap

/**
  *
  */
class OasHeader(val key: String, val value: String) {
  def tuple: (String, String) = (key, value)
}

object OasHeader {

  val extensionName: String = "fragmentType".asOasExtension

  val extensionType: String = "extensionType".asOasExtension

  val swagger = "swagger"

  val openapi = "openapi"

  object Oas20Header extends OasHeader(swagger, "2.0")

  object Oas30Header extends OasHeader(openapi, "3\\.0\\.[0-9]+")

  object Oas20DocumentationItem extends OasHeader(extensionName, "2.0 DocumentationItem")

  object Oas20DataType extends OasHeader(extensionName, "2.0 DataType")

  object Oas20NamedExample extends OasHeader(extensionName, "2.0 NamedExample")

  object Oas20ResourceType extends OasHeader(extensionName, "2.0 ResourceType")

  object Oas20Trait extends OasHeader(extensionName, "2.0 Trait")

  object Oas20AnnotationTypeDeclaration extends OasHeader(extensionName, "2.0 AnnotationTypeDeclaration")

  object Oas20SecurityScheme extends OasHeader(extensionName, "2.0 SecurityScheme")

  object Oas20Extension extends OasHeader(extensionType, "2.0 Extension")

  object Oas20Overlay extends OasHeader(extensionType, "2.0 Overlay")

  def apply(root: Root): Option[OasHeader] =
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        parsed.document.to[YMap] match {
          case Right(map) =>
            map
              .key(extensionName)
              .orElse(map.key(extensionType))
              .orElse(map.key(swagger))
              .orElse(map.key(openapi))
              .flatMap(extension => OasHeader(extension.value.toOption[String].getOrElse("")))
          case Left(_) => None
        }
      case _ => None
    }

  def apply(text: String): Option[OasHeader] = {
    val oas30 = Oas30Header.value.r
    text match {
      case Oas20Header.value                    => Some(Oas20Header)
      case oas30()                              => Some(Oas30Header)
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
  }
}
