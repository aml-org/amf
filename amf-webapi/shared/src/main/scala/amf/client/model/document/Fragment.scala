package amf.client.model.document

import amf.core.model.document.{PayloadFragment => InternalPayloadFragment}
import amf.plugins.document.webapi.model.{
  AnnotationTypeDeclarationFragment => InternalAnnotationTypeDeclarationFragment,
  DataTypeFragment => InternalDataTypeFragment,
  DocumentationItemFragment => InternalDocumentationItemFragment,
  NamedExampleFragment => InternalNamedExampleFragment,
  ResourceTypeFragment => InternalResourceTypeFragment,
  SecuritySchemeFragment => InternalSecuritySchemeFragment,
  TraitFragment => InternalTraitFragment
}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Fragment model class
  */
@JSExportAll
case class DocumentationItem(override private[amf] val _internal: InternalDocumentationItemFragment)
    extends Fragment(_internal) {
  @JSExportTopLevel("model.document.DocumentationItemFragment")
  def this() = this(InternalDocumentationItemFragment())

}

@JSExportAll
case class DataType(override private[amf] val _internal: InternalDataTypeFragment) extends Fragment(_internal) {
  @JSExportTopLevel("model.document.DataTypeFragment")
  def this() = this(InternalDataTypeFragment())
}

@JSExportAll
case class NamedExample(override private[amf] val _internal: InternalNamedExampleFragment)
    extends Fragment(_internal) {
  @JSExportTopLevel("model.document.NamedExampleFragment")
  def this() = this(InternalNamedExampleFragment())
}

@JSExportAll
case class ResourceTypeFragment(override private[amf] val _internal: InternalResourceTypeFragment)
    extends Fragment(_internal) {
  @JSExportTopLevel("model.document.ResourceTypeFragment")
  def this() = this(InternalResourceTypeFragment())
}

@JSExportAll
case class TraitFragment(override private[amf] val _internal: InternalTraitFragment) extends Fragment(_internal) {
  @JSExportTopLevel("model.document.TraitFragment")
  def this() = this(InternalTraitFragment())
}

@JSExportAll
case class AnnotationTypeDeclaration(override private[amf] val _internal: InternalAnnotationTypeDeclarationFragment)
    extends Fragment(_internal) {
  @JSExportTopLevel("model.document.AnnotationTypeDeclarationFragment")
  def this() = this(InternalAnnotationTypeDeclarationFragment())
}

@JSExportAll
case class SecuritySchemeFragment(override private[amf] val _internal: InternalSecuritySchemeFragment)
    extends Fragment(_internal) {
  @JSExportTopLevel("model.document.SecuritySchemeFragment")
  def this() = this(InternalSecuritySchemeFragment())
}
