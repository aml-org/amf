package amf.plugins.document.webapi.validation.remote

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
import java.util.Optional
import java.util.regex.Pattern

import amf.plugins.domain.shapes.models.AnyShape
import org.everit.json.schema.FormatValidator

object JvmJsonSchemaValidator extends PlatformJsonSchemaValidator {
  override protected def validatorForShape(s: AnyShape): PlatformPayloadValidator = new JvmPayloadValidator(s)
}

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
  private val FORMATTER: DateTimeFormatter =
    new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss").toFormatter

  override def formatName = "date-time-only"

  override def validate(value: String): Optional[String] =
    try {
      FORMATTER.parse(value)
      Optional.empty()
    } catch {
      case _: DateTimeParseException =>
        Optional.of(
          String.format("[%s] is not a valid %s. Expected %s", value, this.formatName(), "yyyy-MM-dd'T'HH:mm:ss"))
    }
}

object PartialTimeFormatValidator extends FormatValidator {
  private val PATTERN = "^([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60)$"

  override def formatName = "time"

  override def validate(value: String): Optional[String] = {
    if (!value.matches(PATTERN)) {
      Optional.of(String.format("[%s] is not a valid %s. Expected %s", value, this.formatName(), "HH:mm:ss"))
    } else {
      Optional.empty()
    }
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val pattern = Pattern.compile("(([0-9A-Z]+)([_]?+)*)*")
    pattern.matcher("FOOOOO_BAAAR_FOOOOOOOOO_BA_ ").matches

    val s = "^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$"
    val javaOne = Pattern
      .compile(s)
      .matcher(
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/Marlon_Brando_%28cropped%29.jpg/220px-Marlon_Brando_%28cropped%29.jpg")
      .find()
    println(javaOne)
  }
}
