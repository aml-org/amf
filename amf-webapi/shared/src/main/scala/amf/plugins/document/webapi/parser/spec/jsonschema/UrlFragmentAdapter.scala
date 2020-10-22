package amf.plugins.document.webapi.parser.spec.jsonschema

trait UrlFragmentAdapter {
  def adaptUrlFragment(fragment: String): String
}

object JsonSchemaUrlFragmentAdapter$ extends UrlFragmentAdapter {
  override def adaptUrlFragment(fragment: String): String =
    if (fragment.startsWith("/definitions")) fragment.stripPrefix("/")
    else fragment
}

object DefaultUrlFragmentAdapter$ extends UrlFragmentAdapter {
  override def adaptUrlFragment(fragment: String): String =
    if (fragment.startsWith("/")) fragment.stripPrefix("/")
    else fragment
}