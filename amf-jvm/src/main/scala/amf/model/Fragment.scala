package amf.model

/**
  * JVM Fragment model class
  */
abstract class Fragment(private[amf] val fragment: amf.framework.model.document.Fragment.Fragment) extends BaseUnit with EncodesModel {

  override private[amf] val element = fragment

}

object Fragment {
  def apply(fragment: amf.framework.model.document.Fragment.Fragment): Fragment = fragment match {
    case a: amf.framework.model.document.Fragment.AnnotationTypeDeclaration => AnnotationTypeDeclaration(a)
    case d: amf.framework.model.document.Fragment.DataType                  => DataType(d)
    case d: amf.framework.model.document.Fragment.DialectFragment           => DialectFragment(d)
    case d: amf.framework.model.document.Fragment.DocumentationItem         => DocumentationItem(d)
    case e: amf.framework.model.document.Fragment.ExternalFragment          => ExternalFragment(e)
    case n: amf.framework.model.document.Fragment.NamedExample              => NamedExample(n)
    case r: amf.framework.model.document.Fragment.ResourceTypeFragment      => ResourceTypeFragment(r)
    case s: amf.framework.model.document.Fragment.SecurityScheme            => SecuritySchemeFragment(s)
    case t: amf.framework.model.document.Fragment.TraitFragment             => TraitFragment(t)
  }
}

case class DocumentationItem(private[amf] val documentationItem: amf.framework.model.document.Fragment.DocumentationItem)
    extends Fragment(documentationItem) {
  def this() = this(amf.framework.model.document.Fragment.DocumentationItem())

}

case class DataType(private[amf] val dataType: amf.framework.model.document.Fragment.DataType) extends Fragment(dataType) {
  def this() = this(amf.framework.model.document.Fragment.DataType())
}

case class NamedExample(private[amf] val namedExample: amf.framework.model.document.Fragment.NamedExample)
    extends Fragment(namedExample) {
  def this() = this(amf.framework.model.document.Fragment.NamedExample())
}

case class DialectFragment(private[amf] val df: amf.framework.model.document.Fragment.DialectFragment) extends Fragment(df) {
  def this() = this(amf.framework.model.document.Fragment.DialectFragment())
}

case class ExternalFragment(private[amf] val ef: amf.framework.model.document.Fragment.ExternalFragment) extends Fragment(ef) {
  def this() = this(amf.framework.model.document.Fragment.ExternalFragment())
}

case class ResourceTypeFragment(private[amf] val resourceTypeFragment: amf.framework.model.document.Fragment.ResourceTypeFragment)
    extends Fragment(resourceTypeFragment) {

  def this() = this(amf.framework.model.document.Fragment.ResourceTypeFragment())
}

case class TraitFragment(private[amf] val traitFragment: amf.framework.model.document.Fragment.TraitFragment)
    extends Fragment(traitFragment) {

  def this() = this(amf.framework.model.document.Fragment.TraitFragment())
}

case class AnnotationTypeDeclaration(
    private[amf] val annotationTypeDeclaration: amf.framework.model.document.Fragment.AnnotationTypeDeclaration)
    extends Fragment(annotationTypeDeclaration) {
  def this() = this(amf.framework.model.document.Fragment.AnnotationTypeDeclaration())
}

case class SecuritySchemeFragment(private[amf] val extensionFragment: amf.framework.model.document.Fragment.SecurityScheme)
    extends Fragment(extensionFragment) {

  def this() = this(amf.framework.model.document.Fragment.SecurityScheme())
}
