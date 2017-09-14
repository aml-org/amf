package amf.compiler

/**
  * Raml header comment
  */
case class RamlHeader private[compiler] (text: String)

object RamlHeader {

  object Raml10         extends RamlHeader("%RAML 1.0")
  object Raml10Library  extends RamlHeader("%RAML 1.0 Library")
  object Raml10Fragment extends RamlHeader("%RAML 1.0 Fragment")


  def apply(root: Root): Option[RamlHeader] = {
    root.parsed.comment.flatMap(c => fromText(c.metaText))
  }

  def fromText(text: String): Option[RamlHeader] = text match {
    case t if t.equals(Raml10.text)         => Some(Raml10)
    case t if t.equals(Raml10Library.text)  => Some(Raml10Library)
    case t if t.equals(Raml10Fragment.text) => Some(Raml10Fragment)
    case t if t.startsWith("%")             => Some(RamlHeader(t))
    case _                                  => None
  }
}
