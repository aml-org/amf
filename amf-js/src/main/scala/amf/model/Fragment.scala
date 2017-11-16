package amf.model

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS Fragment model class
  */
@JSExportAll
abstract class Fragment(private[amf] val fragment: amf.document.Fragment.Fragment) extends BaseUnit with EncodesModel {

  override private[amf] val element = fragment

}

object Fragment {
  def apply(fragment: amf.document.Fragment.Fragment): Fragment = fragment match {
    case a: amf.document.Fragment.AnnotationTypeDeclaration => AnnotationTypeDeclaration(a)
    case d: amf.document.Fragment.DataType                  => DataType(d)
    case d: amf.document.Fragment.DialectFragment           => DialectFragment(d)
    case d: amf.document.Fragment.DocumentationItem         => DocumentationItem(d)
    case e: amf.document.Fragment.ExternalFragment          => ExternalFragment(e)
    case n: amf.document.Fragment.NamedExample              => NamedExample(n)
    case r: amf.document.Fragment.ResourceTypeFragment      => ResourceTypeFragment(r)
    case s: amf.document.Fragment.SecurityScheme            => SecuritySchemeFragment(s)
    case t: amf.document.Fragment.TraitFragment             => TraitFragment(t)
  }
}

case class DocumentationItem(private[amf] val documentationItem: amf.document.Fragment.DocumentationItem)
    extends Fragment(documentationItem) {
  def this() = this(amf.document.Fragment.DocumentationItem())

}

case class DataType(private[amf] val dataType: amf.document.Fragment.DataType) extends Fragment(dataType) {
  def this() = this(amf.document.Fragment.DataType())
}

case class NamedExample(private[amf] val namedExample: amf.document.Fragment.NamedExample)
    extends Fragment(namedExample) {
  def this() = this(amf.document.Fragment.NamedExample())
}

case class DialectFragment(private[amf] val df: amf.document.Fragment.DialectFragment) extends Fragment(df) {
  def this() = this(amf.document.Fragment.DialectFragment())
}

case class ExternalFragment(private[amf] val ef: amf.document.Fragment.ExternalFragment) extends Fragment(ef) {
  def this() = this(amf.document.Fragment.ExternalFragment())
}

case class ResourceTypeFragment(private[amf] val resourceTypeFragment: amf.document.Fragment.ResourceTypeFragment)
    extends Fragment(resourceTypeFragment) {

  def this() = this(amf.document.Fragment.ResourceTypeFragment())
}

case class TraitFragment(private[amf] val traitFragment: amf.document.Fragment.TraitFragment)
    extends Fragment(traitFragment) {

  def this() = this(amf.document.Fragment.TraitFragment())
}

case class AnnotationTypeDeclaration(
    private[amf] val annotationTypeDeclaration: amf.document.Fragment.AnnotationTypeDeclaration)
    extends Fragment(annotationTypeDeclaration) {
  def this() = this(amf.document.Fragment.AnnotationTypeDeclaration())
}

case class SecuritySchemeFragment(private[amf] val extensionFragment: amf.document.Fragment.SecurityScheme)
    extends Fragment(extensionFragment) {

  def this() = this(amf.document.Fragment.SecurityScheme())
}
