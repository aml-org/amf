package amf.compiler

import amf.parser.YValueOps

/**
  * Raml header comment
  */
case class RamlHeader private[compiler] (text: String)

trait RamlFragment

object RamlHeader {

  object Raml10        extends RamlHeader("%RAML 1.0")
  object Raml10Library extends RamlHeader("%RAML 1.0 Library")

  def apply(root: Root): Option[RamlHeader] = {
    root.parsed.comment.flatMap(c => fromText(c.metaText)) match {
      case Some(header) => Option(header)
      case _            => RamlFragmentHeader(root)
    }
  }

  def fromText(text: String): Option[RamlHeader] = text match {
    case t if t.equals(Raml10.text)        => Some(Raml10)
    case t if t.equals(Raml10Library.text) => Some(Raml10Library)
    case RamlFragmentHeader(fragment)      => Some(fragment)
    case t if t.startsWith("%")            => Some(RamlHeader(t))
    case _                                 => None
  }

}

object RamlFragmentHeader {
  object Raml10DocumentationItem         extends RamlHeader("%RAML 1.0 DocumentationItem") with RamlFragment
  object Raml10DataType                  extends RamlHeader("%RAML 1.0 DataType") with RamlFragment
  object Raml10NamedExample              extends RamlHeader("%RAML 1.0 NamedExample") with RamlFragment
  object Raml10ResourceType              extends RamlHeader("%RAML 1.0 ResourceType") with RamlFragment
  object Raml10Trait                     extends RamlHeader("%RAML 1.0 Trait") with RamlFragment
  object Raml10AnnotationTypeDeclaration extends RamlHeader("%RAML 1.0 AnnotationTypeDeclaration") with RamlFragment
  object Raml10Overlay                   extends RamlHeader("%RAML 1.0 Overlay") with RamlFragment
  object Raml10Extension                 extends RamlHeader("%RAML 1.0 Extension") with RamlFragment
  object Raml10SecurityScheme            extends RamlHeader("%RAML 1.0 SecurityScheme") with RamlFragment

  val fragmentNames = Seq(
    Raml10DocumentationItem.text,
    Raml10DataType.text,
    Raml10NamedExample.text,
    Raml10ResourceType.text,
    Raml10Trait.text,
    Raml10AnnotationTypeDeclaration.text,
    Raml10Overlay.text,
    Raml10Extension.text,
    Raml10SecurityScheme.text
  )

  def isFragment(text: String): Boolean = fragmentNames.contains(text)

  def fromRoot(root: Root): Option[RamlHeader] = root.parsed.comment.flatMap(c => fromText(c.metaText)) match {
    case Some(header) => Option(header)
    case _ =>
      root.parsed.document.value
        .flatMap(m =>
          FragmentTypes(m.toMap) match {
            case FragmentTypes.DataTypeFragment          => Some(Raml10DataType)
            case FragmentTypes.DocumentationItemFragment => Some(Raml10DocumentationItem)
            case FragmentTypes.ResourceTypeFragment      => Some(Raml10ResourceType)
            case FragmentTypes.TraitFragment             => Some(Raml10Trait)
            case FragmentTypes.AnnotationTypeFragment    => Some(Raml10AnnotationTypeDeclaration)
            case FragmentTypes.ExtensionFragment         => Some(Raml10Extension)
            case FragmentTypes.OverlayFragment           => Some(Raml10Overlay)
            case FragmentTypes.SecuritySchemeFragment    => Some(Raml10SecurityScheme)
            case _                                       => None // UnknowFragment
        })
  }

  def unapply(root: Root): Option[RamlHeader] = fromRoot(root)

  def apply(root: Root): Option[RamlHeader] = fromRoot(root)

  def unapply(text: String): Option[RamlHeader] = fromText(text)

  private def fromText(text: String): Option[RamlHeader] = text match {
    case t if t.equals(Raml10DocumentationItem.text)         => Some(Raml10DocumentationItem)
    case t if t.equals(Raml10DataType.text)                  => Some(Raml10DataType)
    case t if t.equals(Raml10NamedExample.text)              => Some(Raml10NamedExample)
    case t if t.equals(Raml10ResourceType.text)              => Some(Raml10ResourceType)
    case t if t.equals(Raml10Trait.text)                     => Some(Raml10Trait)
    case t if t.equals(Raml10AnnotationTypeDeclaration.text) => Some(Raml10AnnotationTypeDeclaration)
    case t if t.equals(Raml10Overlay.text)                   => Some(Raml10Overlay)
    case t if t.equals(Raml10Extension.text)                 => Some(Raml10Extension)
    case t if t.equals(Raml10SecurityScheme.text)            => Some(Raml10SecurityScheme)
    case _                                                   => None
  }
}
