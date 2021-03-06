package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.model.document.{ExternalFragment, Fragment, RecursiveUnit}
import amf.core.parser.JsonParserFactory
import amf.core.parser.errorhandler.ParserErrorHandler
import org.yaml.parser.{YParser, YamlParser}

object JsonYamlParser {
  def apply(fragment: Fragment)(implicit errorHandler: ParserErrorHandler): YParser = {
    val location = fragment.location().getOrElse("")
    if (isYaml(location)) YamlParser(getRaw(fragment), location)
    else JsonParserFactory.fromCharsWithSource(getRaw(fragment), fragment.location().getOrElse(""))(errorHandler)
  }

  private def isYaml(location: String) = location.endsWith(".yaml") || location.endsWith(".yml")

  private def getRaw(inputFragment: Fragment): String = inputFragment match {
    case fragment: ExternalFragment => fragment.encodes.raw.value()
    case fragment: RecursiveUnit    => fragment.raw.get
    case _                          => ""
  }
}
