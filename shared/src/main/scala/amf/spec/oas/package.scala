package amf.spec

/**
  * Oas package object
  */
package object oas {

  val definitionsPrefix = "#/definitions/"

  val parameterDefinitionsPrefix = "#/parameters/"

  def stripDefinitionsPrefix(url: String): String = url.stripPrefix(definitionsPrefix)

  def stripParameterDefinitionsPrefix(url: String): String = url.stripPrefix(parameterDefinitionsPrefix)

  def appendDefinitionsPrefix(url: String): String = definitionsPrefix + url

  def appendParameterDefinitionsPrefix(url: String): String = parameterDefinitionsPrefix + url
}
