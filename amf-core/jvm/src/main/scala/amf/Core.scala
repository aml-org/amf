package amf

import java.util.concurrent.CompletableFuture

import amf.core.AMF
import amf.core.client.{Generator, Parser, Validator}
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.AMFValidationReport
import amf.model.document.{BaseUnit, Document, Fragment, Module}
import amf.model.domain.{CustomDomainProperty, DomainElement, DomainExtension, PropertyShape}

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

    AMF.init()
  }

  def parser(vendor: String, mediaType: String): Parser = new Parser(vendor, mediaType)
  def generator(vendor: String, mediaType: String): Generator = new Generator(vendor, mediaType)
  def validate(model: BaseUnit, profileName: String, messageStyle: String = "AMF"): CompletableFuture[AMFValidationReport] = Validator.validate(model, profileName, messageStyle)
  def loadValidationProfile(url: String): CompletableFuture[Nothing] = Validator.loadValidationProfile(url)
  def registerNamespace(alias: String, prefix: String): Boolean = platform.registerNamespace(alias, prefix).isDefined
}
