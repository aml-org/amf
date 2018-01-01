package amf.model.domain

import amf.core.model.domain.AmfScalar
import amf.core.vocabulary.Namespace
import amf.dialects.RAML_1_0_VocabularyTopLevel._
import amf.plugins.document.vocabularies.model.domain

import scala.collection.JavaConverters._

case class Vocabulary(private val wrapped: DomainEntity) extends DomainEntity(wrapped.element) {

  val entity: domain.DomainEntity = wrapped.element

  override private[amf] def element: domain.DomainEntity = entity

  def this() = this(new DomainEntity(VocabularyObject().entity))

  private val vocab: VocabularyObject = VocabularyObject(entity)

  def base(): String = vocab.base().orNull
  def withBase(base: String) = {
    entity.withId(base)
    vocab.withBase(base)
    this
  }

  def version(): String = vocab.version().orNull
  def withVersion(version: String) = {
    vocab.withVersion(version)
    this
  }

  def usage(): String = vocab.usage().orNull
  def withUsage(usage: String) = {
    vocab.withUsage(usage)
    this
  }

  def uses(): java.util.Collection[VocabularyImport] =
    vocab.uses().map(u => VocabularyImport(u.entity)).asJavaCollection
  def withUses(vocabs: java.util.Collection[VocabularyImport]) = {
    val vs = vocabs.asScala.map { v =>
      v.element
    }.toSeq
    entity.setArray(amf.plugins.document.vocabularies.core.Vocabulary.uses.field(), vs)
    this
  }

  def externals(): java.util.Collection[ExternalVocabularyImport] =
    vocab.external().map(e => ExternalVocabularyImport(e.entity)).asJavaCollection
  def withExternals(externals: java.util.Collection[ExternalVocabularyImport]) = {
    val exs = externals.asScala.map { e =>
      e.element
    }.toSeq
    entity.setArray(amf.plugins.document.vocabularies.core.Vocabulary.externals.field(), exs)
    this
  }

  def classTerms(): java.util.Collection[ClassTerm] = vocab.classTerms().map(c => ClassTerm(c.entity)).asJavaCollection
  def withClassTerms(classTerms: java.util.Collection[ClassTerm]) = {
    val terms = classTerms.asScala.map(_.element).toSeq
    entity.setArray(amf.plugins.document.vocabularies.core.Vocabulary.classTerms.field(), terms)
    this
  }

  def propertyTerms(): java.util.Collection[PropertyTerm] =
    vocab.propertyTerms().map(c => PropertyTerm(c.entity)).asJavaCollection
  def withPropertyTerms(propertyTerms: java.util.Collection[PropertyTerm]) = {
    val terms = propertyTerms.asScala.map(_.element).toSeq
    entity.setArray(amf.plugins.document.vocabularies.core.Vocabulary.propertyTerms.field(), terms)
    this
  }
}

case class ExternalVocabularyImport(private val entity: domain.DomainEntity) extends DomainEntity(entity) {

  override private[amf] def element: domain.DomainEntity = entity

  def this() = this(ExternalObject().entity)

  private val external: ExternalObject = ExternalObject(entity)

  def name(): String = external.name().orNull
  def withName(name: String) = {
    external.withName(name)
    this
  }

  def uri(): String = external.uri().orNull
  def withUri(uri: String) = {
    external.withUri(uri)
    this
  }
}

case class VocabularyImport(private val entity: domain.DomainEntity) extends DomainEntity(entity) {

  override private[amf] def element: domain.DomainEntity = entity

  def this() = this(NameSpaceImportObject().entity)

  private val importObj: NameSpaceImportObject = NameSpaceImportObject(entity)

  def name(): String = importObj.name().orNull
  def withName(name: String) = {
    importObj.withName(name)
    this
  }

  def uri(): String = importObj.uri().orNull
  def withUri(uri: String) = {
    importObj.withUri(uri)
    this
  }
}

case class ClassTerm(private val entity: domain.DomainEntity) extends DomainEntity(entity) {

  override private[amf] def element: domain.DomainEntity = entity

  def this() = this(ClassObject().entity)

  private val classTerm: ClassObject = ClassObject(entity)

  override def getId(): String = classTerm.id().orNull
  override def withId(id: String) = {
    entity.withId(id)
    classTerm.withId(id)
    this
  }

  def displayName(): String = classTerm.displayName().orNull
  def withDisplayName(displayName: String) = {
    classTerm.withDisplayName(displayName)
    this
  }

  def description(): String = classTerm.description().orNull
  def withDescription(description: String) = {
    classTerm.withDescription(description)
    this
  }

  def termExtends(): String = classTerm.`extends`().head
  def withTermExtends(classUri: String) = {
    classTerm.withExtends(classUri)
    this
  }

  def properties(): java.util.Collection[String] = classTerm.properties().asJavaCollection
  def withProperties(properties: java.util.Collection[String]) = {
    val props = properties.asScala.map { p =>
      AmfScalar(p)
    }.toSeq
    entity.setArray(amf.plugins.document.vocabularies.core.ClassTerm.`properties`.field(), props)
    this
  }

}

case class PropertyTerm(private val entity: domain.DomainEntity) extends DomainEntity(entity) {

  override private[amf] def element: domain.DomainEntity = entity

  def this() = this(PropertyObject().entity)

  private val propertyTerm: PropertyObject = PropertyObject(entity)

  override def getId(): String = propertyTerm.id().orNull
  override def withId(id: String) = {
    entity.withId(id)
    propertyTerm.withId(id)
    this
  }

  def displayName(): String = propertyTerm.displayName().orNull
  def withDisplayName(displayName: String) = {
    propertyTerm.withDisplayName(displayName)
    this
  }

  def description(): String = propertyTerm.description().orNull
  def withDescription(description: String) = {
    propertyTerm.withDescription(description)
    this
  }

  def termExtends(): String = propertyTerm.`extends`().head
  def withTermExtends(propertyUri: String) = {
    propertyTerm.withExtends(propertyUri)
    this
  }

  def range(): java.util.Collection[String] = propertyTerm.range().asJavaCollection
  def withRange(range: java.util.Collection[String]) = {
    val ranges = range.asScala.map { r =>
      AmfScalar(r)
    }.toSeq
    entity.setArray(amf.plugins.document.vocabularies.core.PropertyTerm.range.field(), ranges)
    this
  }
  def withScalarRange(range: java.util.Collection[String]) = {
    val ranges = range.asScala.map { r =>
      AmfScalar((Namespace.Xsd + r).iri())
    }.toSeq
    entity.setArray(amf.plugins.document.vocabularies.core.PropertyTerm.range.field(), ranges)
    this
  }
}
