package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

trait RamlFormatTranslator {
  def checkRamlFormats(format: String): String = {
    format match {
      case "date-only"     => "date"
      case "time-only"     => "time"
      case "datetime-only" => "date-time"
      case "datetime"      => "date-time"
      case "rfc3339"       => "date-time"
      case other           => other
    }
  }
}
