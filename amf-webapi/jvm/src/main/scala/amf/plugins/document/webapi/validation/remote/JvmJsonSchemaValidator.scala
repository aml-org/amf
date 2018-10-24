package amf.plugins.document.webapi.validation.remote

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
import java.util.Optional
import java.util.regex.Pattern

import amf.core.model.document.PayloadFragment
import amf.core.utils.RegexConverter
import amf.core.validation.{AMFValidationResult, _}
import amf.core.vocabulary.Namespace
import org.everit.json.schema.internal.{DateFormatValidator, RegexFormatValidator, URIFormatValidator}
import org.everit.json.schema.loader.SchemaLoader
import org.everit.json.schema.regexp._
import org.everit.json.schema.{FormatValidator, ValidationException}
import org.json.{JSONException, JSONObject, JSONTokener}

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

object JvmJsonSchemaValidator extends PlatformJsonSchemaValidator {

  override type LoadedObj = Object

  override protected def processCandidate(dataNode: Object,
                                          jsonSchema: String,
                                          payload: PayloadFragment): Seq[AMFValidationResult] =
    // hack! TODO: clean json object properly
    loadJson(jsonSchema.replace("\"type\": \"file\"", "\"type\": \"string\"")) match {
      case schemaNode: JSONObject =>
        schemaNode.remove("example")
        schemaNode.remove("examples")
        schemaNode.remove("x-amf-examples")

        /*
        println("\n\nValidating...")
        println("  - SCHEMA:")
        println(jsonSchema)
         */

        case class CustomJavaUtilRegexpFactory() extends JavaUtilRegexpFactory {
          override def createHandler(regexp: String): Regexp = super.createHandler(regexp.convertRegex)
        }

        val schemaBuilder = SchemaLoader
          .builder()
          .schemaJson(schemaNode)
          .regexpFactory(CustomJavaUtilRegexpFactory())
          .addFormatValidator(DateTimeOnlyFormatValidator)
          .addFormatValidator(Rfc2616Attribute)
          .addFormatValidator(Rfc2616AttributeLowerCase)
          .addFormatValidator(new DateFormatValidator())
          .addFormatValidator(new URIFormatValidator())
          .addFormatValidator(new RegexFormatValidator())
          .addFormatValidator(PartialTimeFormatValidator)

        val schemaLoader = schemaBuilder.build()
        val schema       = schemaLoader.load().build()

        /*
        println("  - DATA:")
        println(dataNode)
         */

        try {
          schema.validate(dataNode)
          /*
              println(s"  ====> RESULT: true")
              println("-----------------------\n\n")
           */
          Nil
        } catch {
          case validationException: ValidationException =>
            /*
                println(s"  ====> RESULT: false")
                println(validationException.getAllMessages)
                println("-----------------------\n\n")
             */
            iterateValidations(validationException, payload)
          case exception: Error =>
            reportValidationException(exception, payload)
        }

      case _ => Nil // schema is not a JSON object
    }

  def iterateValidations(validationException: ValidationException,
                         payload: PayloadFragment): Seq[AMFValidationResult] = {
    var resultsAcc = Seq[AMFValidationResult]()
    val results    = validationException.getCausingExceptions.iterator()
    while (results.hasNext) {
      val result = results.next()
      resultsAcc = resultsAcc ++ iterateValidations(result, payload)
    }
    if (resultsAcc.isEmpty) {
      resultsAcc = resultsAcc :+ AMFValidationResult(
        message = makeValidationMessage(validationException),
        level = SeverityLevels.VIOLATION,
        targetNode = payload.encodes.id,
        targetProperty = None,
        validationId = (Namespace.AmfParser + "example-validation-error").iri(),
        position = payload.encodes.position(),
        location = payload.encodes.location(),
        source = validationException
      )
    }
    resultsAcc
  }

  def reportValidationException(exception: Throwable, payload: PayloadFragment): Seq[AMFValidationResult] = {
    Seq(
      AMFValidationResult(
        message = s"Internal error during validation ${exception.getMessage}",
        level = SeverityLevels.VIOLATION,
        targetNode = payload.encodes.id,
        targetProperty = None,
        validationId = (Namespace.AmfParser + "example-validation-error").iri(),
        position = payload.encodes.position(),
        location = payload.encodes.location(),
        source = exception
      ))
  }

  private def makeValidationMessage(validationException: ValidationException): String = {
    val json    = validationException.toJSON
    var pointer = json.getString("pointerToViolation")
    if (pointer.startsWith("#")) pointer = pointer.replaceFirst("#", "")
    (pointer + " " + json.getString("message")).trim
  }

  protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj] = {
    try {
      literalRepresentation(payload) map { payloadText =>
        loadJson(payloadText)
      }
    } catch {
      case _: ExampleUnknownException => None
      case e: JSONException           => throw new InvalidJsonObject(e)
    }
  }

  protected def loadJson(text: String): LoadedObj = new JSONTokener(text).nextValue()
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
