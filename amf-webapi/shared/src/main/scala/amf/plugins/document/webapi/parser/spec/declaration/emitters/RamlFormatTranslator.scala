package amf.plugins.document.webapi.parser.spec.declaration.emitters

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
