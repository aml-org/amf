package amf.model

/** Any parsable unit, backed by a source URI. */
trait BaseUnit {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  val references: java.util.List[BaseUnit]

  /** Returns the file location for the document that has been parsed to generate this model */
  val location: String

  def id(parent: String): String = parent

  def unit: amf.document.BaseUnit

  def usage: String
}
