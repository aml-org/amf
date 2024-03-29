package amf.shapes.internal.spec.jsonschema.ref

object JsonReference {

  def apply(reference: String): (String, Option[String]) = {
    val ref = buildReference(reference)
    (ref.url, ref.fragment)
  }

  def buildReference(reference: String): JsonReference = {
    val parts: Array[String]         = reference.split("#")
    val url: String                  = parts.head
    val hashFragment: Option[String] = parts.tail.headOption.map(adaptUrlFragment)
    new JsonReference(url, hashFragment)
  }

  private def adaptUrlFragment(fragment: String): String =
    if (fragment.startsWith("/")) fragment.stripPrefix("/")
    else fragment
}

case class JsonReference(url: String, fragment: Option[String]) {
  override def toString: String = {
    val pointer = fragment.map(addHash).getOrElse("")
    url + pointer
  }

  private def addHash(pointer: String) = "#" + pointer
}
