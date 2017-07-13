package amf.document

import java.net.URL

/** Any parseable unit, backed by a source URI */
trait Unit {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
//  def references(): List[URL]

  /** Returns the file location for the document that has been parsed to generate this model */
//  def location(): URL
}
