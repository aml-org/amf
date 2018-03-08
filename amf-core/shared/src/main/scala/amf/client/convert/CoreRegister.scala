package amf.client.convert

import amf.client.model.document.{Document, ExternalFragment, Fragment, Module}
import amf.client.model.domain._
import amf.client.convert.CoreClientConverters._
import amf.core.remote.Platform

/** Shared Core registrations. */
object CoreRegister {

  def register(platform: Platform): Unit = {
    platform.registerWrapper(amf.core.metamodel.document.ModuleModel) {
      case m: amf.core.model.document.Module => Module(m)
    }
    platform.registerWrapper(amf.core.metamodel.document.DocumentModel) {
      case m: amf.core.model.document.Document => new Document(m)
    }
    platform.registerWrapper(amf.core.metamodel.document.FragmentModel) {
      case f: amf.core.model.document.Fragment => new Fragment(f)
    }
    platform.registerWrapper(amf.core.metamodel.document.ExternalFragmentModel) {
      case f: amf.core.model.document.ExternalFragment => ExternalFragment(f)
    }
    platform.registerWrapper(amf.core.metamodel.domain.ExternalDomainElementModel) {
      case f: amf.core.model.domain.ExternalDomainElement => ExternalDomainElement(f)
    }
    platform.registerWrapper(amf.core.metamodel.domain.DomainElementModel) {
      case e: amf.core.model.domain.DomainElement => asClient(e)
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
    platform.registerWrapper(amf.core.metamodel.domain.DataNodeModel) {
      case d: amf.core.model.domain.DataNode => DataNodeMatcher.asClient(d)
    }
    platform.registerWrapper(amf.core.metamodel.domain.templates.VariableValueModel) {
      case v: amf.core.model.domain.templates.VariableValue => VariableValue(v)
    }
  }

}
