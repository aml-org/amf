package amf.model

/**
  * JVM Fragment model class
  */
abstract class Fragment(private[amf] val fragment: amf.document.Fragment.Fragment) extends BaseUnit with EncodesModel {

  override private[amf] val element = fragment

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
