package amf.document

import amf.model.AmfObject

/** Any parseable unit, backed by a source URI. */
trait BaseUnit extends AmfObject {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  def references: Seq[BaseUnit]

  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String

  /** Returns the usage comment for de element */
  def usage: String
}
