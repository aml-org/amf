package amf.model

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS Fragment model class
  */
@JSExportAll
abstract class Fragment(private[amf] val fragment: amf.framework.document.Fragment.Fragment) extends BaseUnit with EncodesModel {

  override private[amf] val element = fragment

}

object Fragment {
  def apply(fragment: amf.framework.document.Fragment.Fragment): Fragment = fragment match {
    case a: amf.framework.document.Fragment.AnnotationTypeDeclaration => AnnotationTypeDeclaration(a)
    case d: amf.framework.document.Fragment.DataType                  => DataType(d)
    case d: amf.framework.document.Fragment.DialectFragment           => DialectFragment(d)
    case d: amf.framework.document.Fragment.DocumentationItem         => DocumentationItem(d)
    case e: amf.framework.document.Fragment.ExternalFragment          => ExternalFragment(e)
    case n: amf.framework.document.Fragment.NamedExample              => NamedExample(n)
    case r: amf.framework.document.Fragment.ResourceTypeFragment      => ResourceTypeFragment(r)
    case s: amf.framework.document.Fragment.SecurityScheme            => SecuritySchemeFragment(s)
    case t: amf.framework.document.Fragment.TraitFragment             => TraitFragment(t)
  }
}

case class DocumentationItem(private[amf] val documentationItem: amf.framework.document.Fragment.DocumentationItem)
    extends Fragment(documentationItem) {
  def this() = this(amf.framework.document.Fragment.DocumentationItem())

}

case class DataType(private[amf] val dataType: amf.framework.document.Fragment.DataType) extends Fragment(dataType) {
  def this() = this(amf.framework.document.Fragment.DataType())
}

case class NamedExample(private[amf] val namedExample: amf.framework.document.Fragment.NamedExample)
    extends Fragment(namedExample) {
  def this() = this(amf.framework.document.Fragment.NamedExample())
}

case class DialectFragment(private[amf] val df: amf.framework.document.Fragment.DialectFragment) extends Fragment(df) {
  def this() = this(amf.framework.document.Fragment.DialectFragment())
}

case class ExternalFragment(private[amf] val ef: amf.framework.document.Fragment.ExternalFragment) extends Fragment(ef) {
  def this() = this(amf.framework.document.Fragment.ExternalFragment())
}

case class ResourceTypeFragment(private[amf] val resourceTypeFragment: amf.framework.document.Fragment.ResourceTypeFragment)
    extends Fragment(resourceTypeFragment) {

  def this() = this(amf.framework.document.Fragment.ResourceTypeFragment())
}

case class TraitFragment(private[amf] val traitFragment: amf.framework.document.Fragment.TraitFragment)
    extends Fragment(traitFragment) {

  def this() = this(amf.framework.document.Fragment.TraitFragment())
}

case class AnnotationTypeDeclaration(
    private[amf] val annotationTypeDeclaration: amf.framework.document.Fragment.AnnotationTypeDeclaration)
    extends Fragment(annotationTypeDeclaration) {
  def this() = this(amf.framework.document.Fragment.AnnotationTypeDeclaration())
}

case class SecuritySchemeFragment(private[amf] val extensionFragment: amf.framework.document.Fragment.SecurityScheme)
    extends Fragment(extensionFragment) {

  def this() = this(amf.framework.document.Fragment.SecurityScheme())
}
