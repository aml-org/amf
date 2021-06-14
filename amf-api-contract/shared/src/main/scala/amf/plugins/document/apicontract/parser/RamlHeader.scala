package amf.plugins.document.apicontract.parser

import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.plugins.document.apicontract.parser.RamlHeader.{Raml10Extension, Raml10Overlay}

import scala.util.matching.Regex

/**
  * Raml header comment
  */
case class RamlHeader(text: String) {
  def asRegExp(): Regex = ("\\s*" + text.replaceAll(" ", "\\\\s*") + "\\s*").r
}

sealed trait RamlFragment

object RamlHeader {

  object Raml08          extends RamlHeader("%RAML 0.8")
  object Raml10          extends RamlHeader("%RAML 1.0")
  object Raml10Library   extends RamlHeader("%RAML 1.0 Library")
  object Raml10Overlay   extends RamlHeader("%RAML 1.0 Overlay")
  object Raml10Extension extends RamlHeader("%RAML 1.0 Extension")

  def apply(root: Root): Option[RamlHeader] = {
    root.parsed match {
      case parsed: SyamlParsedDocument => parsed.comment.flatMap(fromText).orElse(RamlFragmentHeader(root))
      case _                           => None
    }
  }

  def fromText(text: String): Option[RamlHeader] = text match {
    case t if Raml08.asRegExp().pattern.matcher(t).matches()          => Some(Raml08)
    case t if Raml10.asRegExp().pattern.matcher(t).matches()          => Some(Raml10)
    case t if Raml10Library.asRegExp().pattern.matcher(t).matches()   => Some(Raml10Library)
    case t if Raml10Overlay.asRegExp().pattern.matcher(t).matches()   => Some(Raml10Overlay)
    case t if Raml10Extension.asRegExp().pattern.matcher(t).matches() => Some(Raml10Extension)
    case RamlFragmentHeader(fragment)                                 => Some(fragment)
    case t if t startsWith "%"                                        => Some(RamlHeader(t))
    case _                                                            => None
  }
}

object RamlFragmentHeader {
  object Raml10DocumentationItem         extends RamlHeader("%RAML 1.0 DocumentationItem") with RamlFragment
  object Raml10DataType                  extends RamlHeader("%RAML 1.0 DataType") with RamlFragment
  object Raml10NamedExample              extends RamlHeader("%RAML 1.0 NamedExample") with RamlFragment
  object Raml10ResourceType              extends RamlHeader("%RAML 1.0 ResourceType") with RamlFragment
  object Raml10Trait                     extends RamlHeader("%RAML 1.0 Trait") with RamlFragment
  object Raml10AnnotationTypeDeclaration extends RamlHeader("%RAML 1.0 AnnotationTypeDeclaration") with RamlFragment
  object Raml10SecurityScheme            extends RamlHeader("%RAML 1.0 SecurityScheme") with RamlFragment

  def fromRoot(root: Root): Option[RamlHeader] = root.parsed match {
    case parsed: SyamlParsedDocument => parsed.comment.flatMap(fromText)
    case _                           => None
  }

  def unapply(root: Root): Option[RamlHeader] = fromRoot(root)

  def apply(root: Root): Option[RamlHeader] = fromRoot(root)

  def unapply(text: String): Option[RamlHeader] = fromText(text)

  private def fromText(text: String): Option[RamlHeader] = text match {
    case t if Raml10DocumentationItem.asRegExp().pattern.matcher(t).matches() => Some(Raml10DocumentationItem)
    case t if Raml10DataType.asRegExp().pattern.matcher(t).matches()          => Some(Raml10DataType)
    case t if Raml10NamedExample.asRegExp().pattern.matcher(t).matches()      => Some(Raml10NamedExample)
    case t if Raml10ResourceType.asRegExp().pattern.matcher(t).matches()      => Some(Raml10ResourceType)
    case t if Raml10Trait.asRegExp().pattern.matcher(t).matches()             => Some(Raml10Trait)
    case t if Raml10AnnotationTypeDeclaration.asRegExp().pattern.matcher(t).matches() =>
      Some(Raml10AnnotationTypeDeclaration)
    case t if Raml10Overlay.asRegExp().pattern.matcher(t).matches()        => Some(Raml10Overlay)
    case t if Raml10Extension.asRegExp().pattern.matcher(t).matches()      => Some(Raml10Extension)
    case t if Raml10SecurityScheme.asRegExp().pattern.matcher(t).matches() => Some(Raml10SecurityScheme)
    case t if Raml10NamedExample.asRegExp().pattern.matcher(t).matches()   => Some(Raml10NamedExample)
    case _                                                                 => None
  }
}
