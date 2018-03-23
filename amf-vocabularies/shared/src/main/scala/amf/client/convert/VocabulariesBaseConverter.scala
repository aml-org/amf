package amf.client.convert

import amf.client.model.document.{Dialect => ClientDialect}
import amf.client.model.domain.{
  ClassTerm => ClientClassTerm,
  DatatypePropertyTerm => ClientDatatypePropertyTerm,
  DialectDomainElement => ClientDialectDomainElement,
  DocumentMapping => ClientDocumentMapping,
  DocumentsModel => ClientDocumentsModel,
  External => ClientExternal,
  NodeMapping => ClientNodeMapping,
  ObjectPropertyTerm => ClientObjectPropertyTerm,
  PropertyMapping => ClientPropertyMapping,
  PublicNodeMapping => ClientPublicNodeMapping,
  VocabularyReference => ClientVocabularyReference
}
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain._

trait VocabulariesBaseConverter
    extends CoreBaseConverter
    with PropertyMappingConverter
    with PublicNodeMappingConverter
    with DialectConverter
    with DocumentsModelConverter
    with DocumentMappingConverter
    with VocabularyReferenceConverter
    with ExternalConverter
    with NodeMappingConverter
    with DialectDomainElementConverter
    with DatatypePropertyMappingConverter
    with ObjectPropertyMappingConverter
    with ClassTermMappingConverter

trait DatatypePropertyMappingConverter extends PlatformSecrets {

  implicit object DatatypePropertyMappingConverter
      extends BidirectionalMatcher[DatatypePropertyTerm, ClientDatatypePropertyTerm] {
    override def asClient(from: DatatypePropertyTerm): ClientDatatypePropertyTerm =
      platform.wrap[ClientDatatypePropertyTerm](from)
    override def asInternal(from: ClientDatatypePropertyTerm): DatatypePropertyTerm = from._internal
  }
}

trait ObjectPropertyMappingConverter extends PlatformSecrets {

  implicit object ObjectPropertyMappingConverter
      extends BidirectionalMatcher[ObjectPropertyTerm, ClientObjectPropertyTerm] {
    override def asClient(from: ObjectPropertyTerm): ClientObjectPropertyTerm =
      platform.wrap[ClientObjectPropertyTerm](from)
    override def asInternal(from: ClientObjectPropertyTerm): ObjectPropertyTerm = from._internal
  }
}

trait DialectConverter extends PlatformSecrets {

  implicit object DialectConverter extends BidirectionalMatcher[Dialect, ClientDialect] {
    override def asClient(from: Dialect): ClientDialect   = ClientDialect(from)
    override def asInternal(from: ClientDialect): Dialect = from._internal
  }
}

trait ClassTermMappingConverter extends PlatformSecrets {

  implicit object ClassTermMappingConverter extends BidirectionalMatcher[ClassTerm, ClientClassTerm] {
    override def asClient(from: ClassTerm): ClientClassTerm   = platform.wrap[ClientClassTerm](from)
    override def asInternal(from: ClientClassTerm): ClassTerm = from._internal
  }
}

trait PropertyMappingConverter extends PlatformSecrets {

  implicit object PropertyMappingConverter extends BidirectionalMatcher[PropertyMapping, ClientPropertyMapping] {
    override def asClient(from: PropertyMapping): ClientPropertyMapping   = platform.wrap[ClientPropertyMapping](from)
    override def asInternal(from: ClientPropertyMapping): PropertyMapping = from._internal
  }
}

trait PublicNodeMappingConverter extends PlatformSecrets {

  implicit object PublicNodeMappingConverter extends BidirectionalMatcher[PublicNodeMapping, ClientPublicNodeMapping] {
    override def asClient(from: PublicNodeMapping): ClientPublicNodeMapping =
      platform.wrap[ClientPublicNodeMapping](from)
    override def asInternal(from: ClientPublicNodeMapping): PublicNodeMapping = from._internal
  }
}

trait DocumentsModelConverter extends PlatformSecrets {

  implicit object DocumentModelConverter extends BidirectionalMatcher[DocumentsModel, ClientDocumentsModel] {
    override def asClient(from: DocumentsModel): ClientDocumentsModel   = platform.wrap[ClientDocumentsModel](from)
    override def asInternal(from: ClientDocumentsModel): DocumentsModel = from._internal
  }
}

trait DocumentMappingConverter extends PlatformSecrets {

  implicit object DocumentMappingConverter extends BidirectionalMatcher[DocumentMapping, ClientDocumentMapping] {
    override def asClient(from: DocumentMapping): ClientDocumentMapping   = platform.wrap[ClientDocumentMapping](from)
    override def asInternal(from: ClientDocumentMapping): DocumentMapping = from._internal
  }
}

trait VocabularyReferenceConverter extends PlatformSecrets {

  implicit object VocabularyReferenceConverter
      extends BidirectionalMatcher[VocabularyReference, ClientVocabularyReference] {
    override def asClient(from: VocabularyReference): ClientVocabularyReference =
      platform.wrap[ClientVocabularyReference](from)
    override def asInternal(from: ClientVocabularyReference): VocabularyReference = from._internal
  }
}

trait ExternalConverter extends PlatformSecrets {

  implicit object ExternalConverter extends BidirectionalMatcher[External, ClientExternal] {
    override def asClient(from: External): ClientExternal   = platform.wrap[ClientExternal](from)
    override def asInternal(from: ClientExternal): External = from._internal
  }
}

trait NodeMappingConverter extends PlatformSecrets {

  implicit object NodeMappingConverter extends BidirectionalMatcher[NodeMapping, ClientNodeMapping] {
    override def asClient(from: NodeMapping): ClientNodeMapping   = platform.wrap[ClientNodeMapping](from)
    override def asInternal(from: ClientNodeMapping): NodeMapping = from._internal
  }
}

trait DialectDomainElementConverter extends PlatformSecrets {

  implicit object DialectDomainElementConvertej
      extends BidirectionalMatcher[DialectDomainElement, ClientDialectDomainElement] {
    override def asClient(from: DialectDomainElement): ClientDialectDomainElement =
      platform.wrap[ClientDialectDomainElement](from)
    override def asInternal(from: ClientDialectDomainElement): DialectDomainElement = from._internal
  }
}
