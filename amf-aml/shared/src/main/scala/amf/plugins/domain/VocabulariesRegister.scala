package amf.plugins.domain

import amf.client.model.document._
import amf.client.model.domain._
import amf.core.metamodel.Obj
import amf.core.remote.Platform
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.metamodel.document._
import amf.plugins.document.vocabularies.metamodel.domain._
import amf.plugins.document.vocabularies.model.{document, domain}

object VocabulariesRegister {

  def register(platform: Platform): Unit = {

    val p: (Obj) => Boolean = (x: Obj) => x.isInstanceOf[DialectDomainElementModel]
    platform.registerWrapperPredicate(p) {
      case m: domain.DialectDomainElement => DialectDomainElement(m)
    }

    platform.registerWrapper(ClassTermModel) {
      case s: domain.ClassTerm => ClassTerm(s)
    }
    platform.registerWrapper(ExternalModel) {
      case s: domain.External => External(s)
    }
    platform.registerWrapper(NodeMappingModel) {
      case s: domain.NodeMapping => NodeMapping(s)
    }
    platform.registerWrapper(PropertyMappingModel) {
      case s: domain.PropertyMapping => PropertyMapping(s)
    }
    platform.registerWrapper(ObjectPropertyTermModel) {
      case s: domain.ObjectPropertyTerm => ObjectPropertyTerm(s)
    }
    platform.registerWrapper(DatatypePropertyTermModel) {
      case s: domain.DatatypePropertyTerm => DatatypePropertyTerm(s)
    }
    platform.registerWrapper(PublicNodeMappingModel) {
      case s: domain.PublicNodeMapping => PublicNodeMapping(s)
    }
    platform.registerWrapper(DocumentMappingModel) {
      case s: domain.DocumentMapping => DocumentMapping(s)
    }
    platform.registerWrapper(DocumentsModelModel) {
      case s: domain.DocumentsModel => DocumentsModel(s)
    }
    platform.registerWrapper(VocabularyReferenceModel) {
      case s: domain.VocabularyReference => VocabularyReference(s)
    }
    platform.registerWrapper(VocabularyModel) {
      case s: document.Vocabulary => new Vocabulary(s)
    }
    platform.registerWrapper(DialectModel) {
      case s: document.Dialect => Dialect(s)
    }
    platform.registerWrapper(DialectFragmentModel) {
      case s: document.DialectFragment => new DialectFragment(s)
    }
    platform.registerWrapper(DialectLibraryModel) {
      case s: document.DialectLibrary => new DialectLibrary(s)
    }
    platform.registerWrapper(DialectInstanceModel) {
      case s: document.DialectInstance => new DialectInstance(s)
    }
    platform.registerWrapper(DialectInstanceFragmentModel) {
      case s: document.DialectInstanceFragment => new DialectInstanceFragment(s)
    }
    platform.registerWrapper(DialectInstanceLibraryModel) {
      case s: document.DialectInstanceLibrary => new DialectInstanceLibrary(s)
    }

    amf.Core.registerPlugin(AMLPlugin)
  }
}