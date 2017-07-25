package amf.document

import amf.domain.FieldsInstance

/** Any parseable unit, backed by a source URI. */
trait BaseUnit extends FieldsInstance {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  val references: Seq[BaseUnit]

  /** Returns the file location for the document that has been parsed to generate this model */
  val location: String
}
