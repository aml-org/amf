package amf.model.domain

import amf.core.model.domain.AmfScalar
import amf.core.vocabulary.Namespace
import amf.dialects.RAML_1_0_VocabularyTopLevel._
import amf.plugins.document.vocabularies.model.domain

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

@JSExportAll
@JSExportTopLevel("model.domain.Vocabulary")
case class Vocabulary(private val wrapped: DomainEntity) extends DomainEntity(wrapped.element) {

  val entity: domain.DomainEntity = wrapped.element

  override private[amf] def element: domain.DomainEntity = entity

  @JSExportTopLevel("model.domain.Vocabulary")
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

  def uses(): js.Array[VocabularyImport] = vocab.uses().map(u => VocabularyImport(u.entity)).toJSArray
  def withUses(vocabs: js.Array[VocabularyImport]) = {
    val vs = vocabs.toSeq.map { v =>
      v.element
    }
    entity.setArray(amf.plugins.document.vocabularies.core.Vocabulary.uses.field(), vs)
    this
  }

  def externals(): js.Array[ExternalVocabularyImport] =
    vocab.external().map(e => ExternalVocabularyImport(e.entity)).toJSArray
  def withExternals(externals: js.Array[ExternalVocabularyImport]) = {
    val exs = externals.toSeq.map { e =>
      e.element
    }
    entity.setArray(amf.plugins.document.vocabularies.core.Vocabulary.externals.field(), exs)
    this
  }

  def classTerms(): js.Array[ClassTerm] = vocab.classTerms().map(c => ClassTerm(c.entity)).toJSArray
  def withClassTerms(classTerms: js.Array[ClassTerm]) = {
    val terms = classTerms.toSeq.map(_.element)
    entity.setArray(amf.plugins.document.vocabularies.core.Vocabulary.classTerms.field(), terms)
    this
  }

  def propertyTerms(): js.Array[PropertyTerm] = vocab.propertyTerms().map(c => PropertyTerm(c.entity)).toJSArray
  def withPropertyTerms(propertyTerms: js.Array[PropertyTerm]) = {
    val terms = propertyTerms.toSeq.map(_.element)
    entity.setArray(amf.plugins.document.vocabularies.core.Vocabulary.propertyTerms.field(), terms)
    this
  }
}

@JSExportAll
case class ExternalVocabularyImport(private val entity: domain.DomainEntity) extends DomainEntity(entity) {

  override private[amf] def element: domain.DomainEntity = entity

  @JSExportTopLevel("model.domain.ExternalVocabularyImport")
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

@JSExportAll
case class VocabularyImport(private val entity: domain.DomainEntity) extends DomainEntity(entity) {

  override private[amf] def element: domain.DomainEntity = entity

  @JSExportTopLevel("model.domain.VocabularyImport")
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

@JSExportAll
case class ClassTerm(private val entity: domain.DomainEntity) extends DomainEntity(entity) {

  override private[amf] def element: domain.DomainEntity = entity

  @JSExportTopLevel("model.domain.ClassTerm")
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

  def properties(): js.Array[String] = classTerm.properties().toJSArray
  def withProperties(properties: js.Array[String]) = {
    val props = properties.toSeq.map { p =>
      AmfScalar(p)
    }
    entity.setArray(amf.plugins.document.vocabularies.core.ClassTerm.`properties`.field(), props)
    this
  }

}

@JSExportAll
case class PropertyTerm(private val entity: domain.DomainEntity) extends DomainEntity(entity) {

  override private[amf] def element: domain.DomainEntity = entity

  @JSExportTopLevel("model.domain.PropertyTerm")
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

  def range(): js.Array[String] = propertyTerm.range().toJSArray
  def withRange(range: js.Array[String]) = {
    val ranges = range.toSeq.map { r =>
      AmfScalar(r)
    }
    entity.setArray(amf.plugins.document.vocabularies.core.PropertyTerm.range.field(), ranges)
    this
  }

  def withScalarRange(range: js.Array[String]) = {
    val ranges = range.toSeq.map { r =>
      AmfScalar((Namespace.Xsd + r).iri())
    }
    entity.setArray(amf.plugins.document.vocabularies.core.PropertyTerm.range.field(), ranges)
    this
  }
}
