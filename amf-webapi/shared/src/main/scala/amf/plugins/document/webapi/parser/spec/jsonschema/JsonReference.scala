package amf.plugins.document.webapi.parser.spec.jsonschema

object JsonReference {
  def buildReference(reference: String, toolkit: UrlFragmentAdapter): JsonReference = {
    val parts: Array[String] = reference.split("#")
    val url: String          = parts.head
    val hashFragment: Option[String] = parts.tail.headOption.map(toolkit.adaptUrlFragment)
    new JsonReference(url, hashFragment)
  }
}

case class JsonReference(url: String, fragment: Option[String])