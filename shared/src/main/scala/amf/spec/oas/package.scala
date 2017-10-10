package amf.spec

/**
  * Oas package object
  */
package object oas {

  val definitionsPrefix = "#/definitions/"

  def stripDefinitionsPrefix(url: String): String = url.stripPrefix(definitionsPrefix)

  def appendDefinitionsPrefix(url: String): String = definitionsPrefix + url
}
