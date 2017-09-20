package amf.compiler

import org.yaml.model.{YComment, YDocument}

/**
  * Raml header comment
  */
class RamlHeader private[compiler] (val text: String) {
  def isDocument: Boolean = this == RamlHeader.RAML_10
  def isLibrary: Boolean  = this == RamlHeader.RAML_10_LIBRARY
  def isFragment: Boolean = this == RamlHeader.RAML_10_FRAGMENT
}

object RamlHeader {

  val RAML_10: RamlHeader          = new RamlHeader("%RAML 1.0")
  val RAML_10_LIBRARY: RamlHeader  = new RamlHeader("%RAML 1.0 Library")
  val RAML_10_FRAGMENT: RamlHeader = new RamlHeader("%RAML 1.0 Fragment")

  def apply(document: YDocument): Option[RamlHeader] = {
    document.children.headOption.flatMap {
      case c: YComment =>
        c.metaText match {
          case t if t.equals(RAML_10.text)          => Some(RAML_10)
          case t if t.equals(RAML_10_LIBRARY.text)  => Some(RAML_10_LIBRARY)
          case t if t.equals(RAML_10_FRAGMENT.text) => Some(RAML_10_FRAGMENT)
        }
      case _ => None
    }
  }
}
