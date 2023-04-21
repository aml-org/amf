package amf.shapes.internal.document.apicontract.validation.remote

import amf.shapes.internal.validation.jsonschema.PayloadValidatorCommon
import org.everit.json.schema.FormatValidator

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException, ResolverStyle}
import java.time.temporal.ChronoField
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
  val pattern: String = PayloadValidatorCommon.rfc2616Regex
}

object Rfc2616AttributeLowerCase extends Rfc2616Attribute {
  override def formatName = "rfc2616"
}

object DateTimeOnlyFormatValidator extends FormatValidator {

  val pattern: String = PayloadValidatorCommon.dateTimeOnlyRegex

  override def formatName = "date-time-only"

  override def validate(value: String): Optional[String] =
    if (!value.matches(pattern)) {
      Optional.of(
        String.format("[%s] is not a valid %s. Expected %s", value, this.formatName(), "yyyy-MM-dd'T'HH:mm:ss[.ff...]")
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

object DateTimeFormatValidator extends AmfFormatterFormatValidator {

  // Most of this code, except the leap year validation, are extracted from the DateTimeFormatValidator implemented by everit
  override def formatName = "date-time"

  private val secondsFormatter =
    (new DateTimeFormatterBuilder)
      .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
      .parseStrict()
      .toFormatter
      .withResolverStyle(ResolverStyle.STRICT)

  override val formatter: DateTimeFormatter =
    (new DateTimeFormatterBuilder)
      .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
      .appendOptional(secondsFormatter)
      .appendPattern("XXX")
      .parseStrict()
      .toFormatter
      .withResolverStyle(ResolverStyle.STRICT)

  override val pattern: String =
    Seq(
      "yyyy-MM-dd'T'HH:mm:ssZ",
      "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z",
      "yyyy-MM-dd'T'HH:mm:ss[+-]HH:mm",
      "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}[+-]HH:mm"
    ).mkString(", ")

}

object DateFormatValidator extends AmfFormatterFormatValidator {

  // Most of this code, except the leap year validation, are extracted from the DateTimeFormatValidator implemented by everit
  override def formatName(): String = "date"

  override val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT)
  override val pattern: String              = "yyyy-MM-dd"
}

abstract class AmfFormatterFormatValidator extends FormatValidator {

  protected val formatter: DateTimeFormatter
  protected val pattern: String

  override def validate(value: String): Optional[String] = {
    try {
      formatter.parse(value)
      Optional.empty()
    } catch {
      case _: DateTimeParseException =>
        Optional.of(String.format("[%s] is not a valid %s. Expected [%s]", value, this.formatName, pattern))
    }
  }
}
