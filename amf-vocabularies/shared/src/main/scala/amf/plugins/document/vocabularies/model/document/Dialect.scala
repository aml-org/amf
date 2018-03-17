package amf.plugins.document.vocabularies.model.document

import amf.client.model.StrField
import amf.core.metamodel.Obj
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.document.DialectModel._
import amf.plugins.document.vocabularies.metamodel.document.{DialectFragmentModel, DialectLibraryModel, DialectModel}
import amf.plugins.document.vocabularies.model.domain.{DocumentsModel, External, NodeMapping}

case class Dialect(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel with EncodesModel {
  def meta: Obj                          = DialectModel
  def references: Seq[BaseUnit]          = fields.field(References)
  def location: String                   = fields(Location)
  def encodes: DomainElement             = fields.field(Encodes)
  def declares: Seq[DomainElement]       = fields.field(Declares)
  def usage: String                      = fields(Usage)
  def adopted(parent: String): this.type = withId(parent)

  def nameAndVersion(): String = s"${name().value()} ${version().value()}"

  def name(): StrField                                = fields.field(Name)
  def withName(name: String)                          = set(Name, name)
  def version(): StrField                             = fields.field(Version)
  def withVersion(version: String)                    = set(Version, version)
  def externals: Seq[External]                        = fields.field(Externals)
  def withExternals(externals: Seq[External])         = setArray(Externals, externals)
  def documents(): DocumentsModel                     = fields.field(Documents)
  def withDocuments(documentsMapping: DocumentsModel) = set(Documents, documentsMapping)

  def header = s"%${name().value()} ${version().value()}".replace(" ", "")

  def libraryHeaders = Option(documents().library()) match {
    case Some(library) => Seq(s"%RAMLLibrary/${header.replaceFirst("%", "")}")
    case None          => Nil
  }

  def isLibraryHeader(header: String) = libraryHeaders.contains(header.replace(" ", ""))

  def fragmentHeaders = documents().fragments().map { fragment =>
    s"%${fragment.documentName().value()}/${header.replaceFirst("%", "")}".replace(" ", "")
  }

  def isFragmentHeader(header: String) = fragmentHeaders.contains(header.replace(" ", ""))

  def allHeaders = Seq(header) ++ libraryHeaders ++ fragmentHeaders
}

object Dialect {
  def apply(): Dialect = apply(Annotations())

  def apply(annotations: Annotations): Dialect = Dialect(Fields(), annotations)
}

case class DialectLibrary(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel {
  def meta: Obj                          = DialectLibraryModel
  def references: Seq[BaseUnit]          = fields.field(References)
  def location: String                   = fields(Location)
  def declares: Seq[DomainElement]       = fields.field(Declares)
  def usage: String                      = fields(Usage)
  def adopted(parent: String): this.type = withId(parent)

  def externals: Seq[External]                = fields.field(Externals)
  def withExternals(externals: Seq[External]) = setArray(Externals, externals)
}

object DialectLibrary {
  def apply(): DialectLibrary = apply(Annotations())

  def apply(annotations: Annotations): DialectLibrary = DialectLibrary(Fields(), annotations)
}

case class DialectFragment(fields: Fields, annotations: Annotations) extends BaseUnit with EncodesModel {
  def meta: Obj                 = DialectFragmentModel
  def references: Seq[BaseUnit] = fields.field(References)
  def location: String          = fields(Location)
  def usage: String             = fields(Usage)

  def adopted(parent: String): this.type = withId(parent)

  def externals: Seq[External]                = fields.field(Externals)
  def withExternals(externals: Seq[External]) = setArray(Externals, externals)
  override def encodes: NodeMapping           = fields.field(Encodes)
  def withEncodes(nodeMapping: NodeMapping)   = set(Encodes, nodeMapping)
}

object DialectFragment {
  def apply(): DialectFragment = apply(Annotations())

  def apply(annotations: Annotations): DialectFragment = DialectFragment(Fields(), annotations)
}
