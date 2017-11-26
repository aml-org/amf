package amf.plugins.document.webapi

import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels._
import amf.model.document._

object PluginWrapper extends PlatformSecrets {
  def init() = {
    platform.registerWrapper(AnnotationTypeDeclarationFragmentModel) {
      case s: model.AnnotationTypeDeclarationFragment => AnnotationTypeDeclaration(s)
    }
    platform.registerWrapper(DataTypeFragmentModel) {
      case s: model.DataTypeFragment => DataType(s)
    }
    platform.registerWrapper(DocumentationItemFragmentModel) {
      case s: model.DocumentationItemFragment => DocumentationItem(s)
    }
    platform.registerWrapper(ExternalFragmentModel) {
      case s: model.ExternalFragment => ExternalFragment(s)
    }
    platform.registerWrapper(NamedExampleFragmentModel) {
      case s: model.NamedExampleFragment => NamedExample(s)
    }
    platform.registerWrapper(ResourceTypeFragmentModel) {
      case s: model.ResourceTypeFragment => ResourceTypeFragment(s)
    }
    platform.registerWrapper(SecuritySchemeFragmentModel) {
      case s: model.SecuritySchemeFragment => SecuritySchemeFragment(s)
    }
    platform.registerWrapper(TraitFragmentModel) {
      case s: model.TraitFragment => TraitFragment(s)
    }
  }
}
