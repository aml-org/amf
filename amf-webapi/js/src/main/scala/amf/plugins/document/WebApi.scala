package amf.plugins.document

import amf.core.registries.AMFPluginsRegistry
import amf.core.unsafe.PlatformSecrets
import amf.model.document._
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels._
import amf.plugins.document.webapi.{OAS20Plugin, PayloadPlugin, RAML10Plugin, model}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object WebApi extends PlatformSecrets {

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
    platform.registerWrapper(amf.plugins.document.webapi.metamodel.ExtensionModel) {
      case m: model.Extension => new Extension(m)
    }
    platform.registerWrapper(amf.plugins.document.webapi.metamodel.OverlayModel) {
      case m: model.Overlay => new Overlay(m)
    }

    // Initialization of plugins
    AMFPluginsRegistry.registerDocumentPlugin(OAS20Plugin)
    AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
    AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)

  }
}
