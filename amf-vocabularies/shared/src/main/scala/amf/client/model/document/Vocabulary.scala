package amf.client.model.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.client.model.domain._
import amf.core.parser.Annotations
import amf.plugins.document.vocabularies.metamodel.document.VocabularyModel
import amf.plugins.document.vocabularies.model.document.{Vocabulary => InternalVocabulary}
import amf.plugins.document.vocabularies.model.domain.{ClassTerm => InternalClassTerm, DatatypePropertyTerm => InternalDatatypePropertyTerm, ObjectPropertyTerm => InternalObjectPropertyTerm}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class Vocabulary(private[amf] val _internal: InternalVocabulary) extends BaseUnit with DeclaresModel {

  @JSExportTopLevel("model.document.Vocabulary")
  def this() = this(InternalVocabulary())

  def name: StrField = _internal.name
  def description: StrField = new StrField {
    /** Return string value as option. */
    override def option(): Option[String] = Option(_internal.usage)

    /** Return annotations. */
    override def annotations(): Annotations = _internal.fields.entry(VocabularyModel.Usage) match {
      case Some(fieldEntry) => fieldEntry.value.annotations
      case None             => Annotations()
    }
  }
  def base: StrField = _internal.base
  def imports: ClientList[VocabularyReference] = _internal.imports.asClient
  def externals: ClientList[External] = _internal.externals.asClient

  def withName(name: String) = {
    _internal.withName(name)
    this
  }
  def withBase(base: String) = {
    _internal.withBase(base)
    this
  }
  def withUsage(usage: String) = {
    _internal.withUsage(usage)
    this
  }
  def withExternals(externals: ClientList[External]) = {
    _internal.withExternals(externals.asInternal)
    this
  }
  def withImports(vocabularies: ClientList[VocabularyReference]) = {
    _internal.withImports(vocabularies.asInternal)
    this
  }

  def objectPropertyTerms(): ClientList[ObjectPropertyTerm] = {
    val res: Seq[InternalObjectPropertyTerm] = _internal.declares.collect {
      case term: InternalObjectPropertyTerm => term
    }
    res.asClient
  }

  def datatypePropertyTerms() = {
    _internal.declares.collect {
      case term: InternalDatatypePropertyTerm => term
    }.asClient
  }

  def classTerms() = {
    _internal.declares.collect {
      case term: InternalClassTerm => term
    }.asClient
  }
}
