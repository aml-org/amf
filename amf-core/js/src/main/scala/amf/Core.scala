package amf

import amf.core.AMF
import amf.core.registries.AMFPluginsRegistry
import amf.core.unsafe.PlatformSecrets
import amf.model.document.{Document, Fragment, Module}
import amf.model.domain.{CustomDomainProperty, DomainElement, DomainExtension, PropertyShape}
import amf.plugins.syntax.SYamlSyntaxPlugin

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object Core extends PlatformSecrets{

  def init() = {
    platform.registerWrapper(amf.core.metamodel.document.ModuleModel) {
      case m: amf.core.model.document.Module => Module(m)
    }
    platform.registerWrapper(amf.core.metamodel.document.DocumentModel) {
      case m: amf.core.model.document.Document => Document(m)
    }
    platform.registerWrapper(amf.core.metamodel.document.FragmentModel) {
      case f: amf.core.model.document.Fragment => Fragment(f)
    }
    platform.registerWrapper(amf.core.metamodel.domain.DomainElementModel) {
      case e: amf.core.model.domain.DomainElement => DomainElement(e)
    }
    platform.registerWrapper(amf.core.metamodel.domain.extensions.CustomDomainPropertyModel) {
      case e: amf.core.model.domain.extensions.CustomDomainProperty => CustomDomainProperty(e)
    }
    platform.registerWrapper(amf.core.metamodel.domain.extensions.DomainExtensionModel) {
      case e: amf.core.model.domain.extensions.DomainExtension => DomainExtension(e)
    }
    platform.registerWrapper(amf.core.metamodel.domain.extensions.PropertyShapeModel) {
      case e: amf.core.model.domain.extensions.PropertyShape => PropertyShape(e)
    }

    // Init the core component
    AMF.init()
    AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
  }

}
