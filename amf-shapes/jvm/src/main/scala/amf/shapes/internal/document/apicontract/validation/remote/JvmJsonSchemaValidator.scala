package amf.shapes.internal.document.apicontract.validation.remote

import org.everit.json.schema.FormatValidator

import java.util.Optional

class Rfc2616Attribute extends FormatValidator {

  override def formatName: String = Rfc2616Attribute.name

  override def validate(value: String): Optional[String] = {
    if (!value.matches(Rfc2616Attribute.pattern)) {
      Optional.of(s"Invalid RFC2616 string, '$value' does not match the expected format '${Rfc2616Attribute.pattern}'")
    } else {
      Optional.empty()
    }
  }
}

object Rfc2616Attribute extends Rfc2616Attribute {
  val name = "RFC2616"
  val pattern =
    "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), (0[1-9]|[12][0-9]|3[01]) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) ([0-9]{4}) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60) (GMT)$"
}

object Rfc2616AttributeLowerCase extends Rfc2616Attribute {
  override def formatName = "rfc2616"
}

object DateTimeOnlyFormatValidator extends FormatValidator {
  val pattern =
    "^([0-9]{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])[Tt]([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60)(\\.[0-9]+)?$"

  override def formatName = "date-time-only"

  override def validate(value: String): Optional[String] =
    if (!value.matches(pattern)) {
      Optional.of(
        String.format("[%s] is not a valid %s. Expected %s", value, this.formatName(), "yyyy-MM-dd'T'HH:mm:ss")
      )
    } else Optional.empty()
}

object PartialTimeFormatValidator extends FormatValidator {
  private val PATTERN = "^([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60)(\\.[0-9]+)?$"

  override def formatName = "time"

  override def validate(value: String): Optional[String] = {
    if (!value.matches(PATTERN)) {
      Optional.of(String.format("[%s] is not a valid %s. Expected %s", value, this.formatName(), "HH:mm:ss[.ff...]"))
    } else {
      Optional.empty()
    }
  }
}
