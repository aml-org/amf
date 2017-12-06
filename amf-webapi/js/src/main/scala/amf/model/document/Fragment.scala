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

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS Fragment model class
  */
@JSExportAll
case class DocumentationItem(private[amf] val documentationItem: CoreDocumentationItemFragment)
    extends Fragment(documentationItem) {
  def this() = this(CoreDocumentationItemFragment())

}

@JSExportAll
case class DataType(private[amf] val dataType: CoreDataTypeFragment) extends Fragment(dataType) {
  def this() = this(CoreDataTypeFragment())
}

@JSExportAll
case class NamedExample(private[amf] val namedExample: CoreNamedExampleFragment) extends Fragment(namedExample) {
  def this() = this(CoreNamedExampleFragment())
}

@JSExportAll
case class ResourceTypeFragment(private[amf] val resourceTypeFragment: CoreResourceTypeFragment)
    extends Fragment(resourceTypeFragment) {

  def this() = this(CoreResourceTypeFragment())
}

@JSExportAll
case class TraitFragment(private[amf] val traitFragment: CoreTraitFragment) extends Fragment(traitFragment) {

  def this() = this(CoreTraitFragment())
}

@JSExportAll
case class AnnotationTypeDeclaration(private[amf] val annotationTypeDeclaration: CoreAnnotationTypeDeclarationFragment)
    extends Fragment(annotationTypeDeclaration) {
  def this() = this(CoreAnnotationTypeDeclarationFragment())
}

@JSExportAll
case class SecuritySchemeFragment(private[amf] val extensionFragment: CoreSecuritySchemeFragment)
    extends Fragment(extensionFragment) {

  def this() = this(CoreSecuritySchemeFragment())
}
