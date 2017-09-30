package amf.compiler

/**
  * Raml header comment
  */
case class RamlHeader private[compiler] (text: String)

object RamlHeader {

  object Raml10         extends RamlHeader("%RAML 1.0")
  object Raml10Library  extends RamlHeader("%RAML 1.0 Library")
  object Raml10Fragment extends RamlHeader("%RAML 1.0 Fragment") // ??

  def apply(root: Root): Option[RamlHeader] = {
    root.parsed.comment.flatMap(c => fromText(c.metaText))
  }

  def fromText(text: String): Option[RamlHeader] = text match {
    case t if t.equals(Raml10.text)           => Some(Raml10)
    case t if t.equals(Raml10Library.text)    => Some(Raml10Library)
    case t if t.equals(Raml10Fragment.text)   => Some(Raml10Fragment)
    case t if RamlFragmentHeader(t).isDefined => Some(Raml10Fragment)
    case t if t.startsWith("%")               => Some(RamlHeader(t))
    case _                                    => None
  }

}

object RamlFragmentHeader {
  object Raml10DocumentationItem         extends RamlHeader("%RAML 1.0 DocumentationItem")
  object Raml10DataType                  extends RamlHeader("%RAML 1.0 DataType")
  object Raml10NamedExample              extends RamlHeader("%RAML 1.0 NamedExample")
  object Raml10ResourceType              extends RamlHeader("%RAML 1.0 ResourceType")
  object Raml10Trait                     extends RamlHeader("%RAML 1.0 Trait")
  object Raml10AnnotationTypeDeclaration extends RamlHeader("%RAML 1.0 AnnotationTypeDeclaration")
  //  Overlay	An overlay file	Overlays
  //  Extension	An extension file	Extensions
  //  SecurityScheme

  val fragmentNames = Seq(
    Raml10DocumentationItem.text,
    Raml10DataType.text,
    Raml10NamedExample.text,
    Raml10ResourceType.text,
    Raml10Trait.text,
    Raml10AnnotationTypeDeclaration.text
  )

  def isFragment(text: String): Boolean = fragmentNames.contains(text)

  def apply(root: Root): Option[RamlHeader] =
    root.parsed.comment.flatMap(c => apply(c.metaText))

  def apply(text: String): Option[RamlHeader] = text match {
    case t if t.equals(Raml10DocumentationItem.text)         => Some(Raml10DocumentationItem)
    case t if t.equals(Raml10DataType.text)                  => Some(Raml10DataType)
    case t if t.equals(Raml10NamedExample.text)              => Some(Raml10NamedExample)
    case t if t.equals(Raml10ResourceType.text)              => Some(Raml10ResourceType)
    case t if t.equals(Raml10Trait.text)                     => Some(Raml10Trait)
    case t if t.equals(Raml10AnnotationTypeDeclaration.text) => Some(Raml10AnnotationTypeDeclaration)
    case _                                                   => None
  }

}
