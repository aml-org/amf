package amf.plugins.document.vocabularies.model.document

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.document.DialectModel._
import amf.plugins.document.vocabularies.metamodel.document.{DialectFragmentModel, DialectLibraryModel, DialectModel}
import amf.plugins.document.vocabularies.model.domain.{DocumentsModel, External, NodeMapping}

trait MappingDeclarer { this: BaseUnit with DeclaresModel =>

  def findNodeMapping(mappingId: String): Option[NodeMapping] = {
    declares.find(_.id == mappingId) match {
      case Some(mapping: NodeMapping) => Some(mapping)
      case _ =>
        references
          .collect {
            case lib: MappingDeclarer =>
              lib
          }
          .map { dec =>
            dec.findNodeMapping(mappingId)
          }
          .filter(_.isDefined)
          .map(_.get)
          .headOption
    }
  }
}

case class Dialect(fields: Fields, annotations: Annotations)
    extends BaseUnit
    with DeclaresModel
    with EncodesModel
    with MappingDeclarer {

  def references: Seq[BaseUnit]    = fields.field(References)
  def encodes: DomainElement       = fields.field(Encodes)
  def declares: Seq[DomainElement] = fields.field(Declares)
  def name(): StrField             = fields.field(Name)
  def version(): StrField          = fields.field(Version)
  def externals: Seq[External]     = fields.field(Externals)
  def documents(): DocumentsModel  = fields.field(Documents)

  def nameAndVersion(): String = s"${name().value()} ${version().value()}"
  def header: String           = s"%${name().value()} ${version().value()}".replace(" ", "")

  override def componentId: String = ""

  def withName(name: String): Dialect                          = set(Name, name)
  def withVersion(version: String): Dialect                    = set(Version, version)
  def withExternals(externals: Seq[External]): Dialect         = setArray(Externals, externals)
  def withDocuments(documentsMapping: DocumentsModel): Dialect = set(Documents, documentsMapping)

  def libraryHeaders: Seq[String] = Option(documents().library()) match {
    case Some(library) => Seq(s"%Library/${header.replaceFirst("%", "")}")
    case None          => Nil
  }

  def isLibraryHeader(header: String): Boolean = libraryHeaders.contains(header.replace(" ", ""))

  def fragmentHeaders: Seq[String] = documents().fragments().map { fragment =>
    s"%${fragment.documentName().value()}/${header.replaceFirst("%", "")}".replace(" ", "")
  }

  def isFragmentHeader(header: String): Boolean = fragmentHeaders.contains(header.replace(" ", ""))

  def allHeaders: Seq[String] = Seq(header) ++ libraryHeaders ++ fragmentHeaders

  def meta: Obj = DialectModel
}

object Dialect {
  def apply(): Dialect = apply(Annotations())

  def apply(annotations: Annotations): Dialect = Dialect(Fields(), annotations)
}

case class DialectLibrary(fields: Fields, annotations: Annotations)
    extends BaseUnit
    with DeclaresModel
    with MappingDeclarer {

  def references: Seq[BaseUnit]    = fields.field(References)
  def declares: Seq[DomainElement] = fields.field(Declares)
  def externals: Seq[External]     = fields.field(Externals)

  override def componentId: String = ""

  def withExternals(externals: Seq[External]): DialectLibrary = setArray(Externals, externals)

  def meta: Obj = DialectLibraryModel
}

object DialectLibrary {
  def apply(): DialectLibrary = apply(Annotations())

  def apply(annotations: Annotations): DialectLibrary = DialectLibrary(Fields(), annotations)
}

case class DialectFragment(fields: Fields, annotations: Annotations) extends BaseUnit with EncodesModel {

  def references: Seq[BaseUnit]     = fields.field(References)
  def externals: Seq[External]      = fields.field(Externals)
  override def encodes: NodeMapping = fields.field(Encodes)

  override def componentId: String = ""

  def withExternals(externals: Seq[External]): DialectFragment = setArray(Externals, externals)
  def withEncodes(nodeMapping: NodeMapping): DialectFragment   = set(Encodes, nodeMapping)

  def meta: Obj = DialectFragmentModel
}

object DialectFragment {
  def apply(): DialectFragment = apply(Annotations())

  def apply(annotations: Annotations): DialectFragment = DialectFragment(Fields(), annotations)
}
