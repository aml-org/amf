package amf.document

import amf.remote.URL

/** Any parseable unit, backed by a source URI. */
trait BaseUnit {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  def references(): Seq[URL]

  /** Returns the file location for the document that has been parsed to generate this model */
  def location(): URL
}
