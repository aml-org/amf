package amf.model.document

import amf.plugins.document.webapi.model.{
  AnnotationTypeDeclarationFragment => CoreAnnotationTypeDeclarationFragment,
  DataTypeFragment => CoreDataTypeFragment,
  DocumentationItemFragment => CoreDocumentationItemFragment,
  NamedExampleFragment => CoreNamedExampleFragment,
  ResourceTypeFragment => CoreResourceTypeFragment,
  SecuritySchemeFragment => CoreSecuritySchemeFragment,
  TraitFragment => CoreTraitFragment
}

/**
  * JS Fragment model class
  */
case class DocumentationItem(private[amf] val documentationItem: CoreDocumentationItemFragment)
    extends Fragment(documentationItem) {
  def this() = this(CoreDocumentationItemFragment())

}

case class DataType(private[amf] val dataType: CoreDataTypeFragment) extends Fragment(dataType) {
  def this() = this(CoreDataTypeFragment())
}

case class NamedExample(private[amf] val namedExample: CoreNamedExampleFragment) extends Fragment(namedExample) {
  def this() = this(CoreNamedExampleFragment())
}

case class ResourceTypeFragment(private[amf] val resourceTypeFragment: CoreResourceTypeFragment)
    extends Fragment(resourceTypeFragment) {

  def this() = this(CoreResourceTypeFragment())
}

case class TraitFragment(private[amf] val traitFragment: CoreTraitFragment) extends Fragment(traitFragment) {

  def this() = this(CoreTraitFragment())
}

case class AnnotationTypeDeclaration(private[amf] val annotationTypeDeclaration: CoreAnnotationTypeDeclarationFragment)
    extends Fragment(annotationTypeDeclaration) {
  def this() = this(CoreAnnotationTypeDeclarationFragment())
}

case class SecuritySchemeFragment(private[amf] val extensionFragment: CoreSecuritySchemeFragment)
    extends Fragment(extensionFragment) {

  def this() = this(CoreSecuritySchemeFragment())
}
