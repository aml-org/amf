package amf.plugins.document.vocabularies2.model.document

import amf.core.metamodel.Obj
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies2.metamodel.document.DialectModel
import amf.plugins.document.vocabularies2.metamodel.document.DialectModel._
import amf.plugins.document.vocabularies2.model.domain.{DocumentsModel, External}

case class Dialect(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel with EncodesModel  {
  def meta: Obj = DialectModel
  def references: Seq[BaseUnit] = fields(References)
  def location: String = fields(Location)
  def encodes: DomainElement = fields(Encodes)
  def declares: Seq[DomainElement] = fields(Declares)
  def usage: String = fields(Usage)
  def adopted(parent: String): this.type = withId(parent)

  def name(): String = fields(Name)
  def withName(name: String) = set(Name, name)
  def version(): String = fields(Version)
  def withVersion(version: String) = set(Version, version)
  def externals: Seq[External]          = fields(Externals)
  def withExternals(externals: Seq[External])             = setArray(Externals, externals)
  def documents(): DocumentsModel = fields(Documents)
  def withDocuments(documentsMapping: DocumentsModel) = set(Documents, documentsMapping)
}

object Dialect {
  def apply(): Dialect = apply(Annotations())

  def apply(annotations: Annotations): Dialect = Dialect(Fields(), annotations)
}
