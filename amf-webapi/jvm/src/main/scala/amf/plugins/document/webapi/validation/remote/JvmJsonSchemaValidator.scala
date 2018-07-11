package amf.plugins.document.webapi.validation.remote

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
import java.util.Optional

import amf.core.annotations.LexicalInformation
import amf.core.model.document.PayloadFragment
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, AMFValidationResult, ValidationCandidate, _}
import amf.core.vocabulary.Namespace
import com.google.common.collect.ImmutableList
import org.everit.json.schema.internal.{DateFormatValidator, RegexFormatValidator, URIFormatValidator}
import org.everit.json.schema.{FormatValidator, ValidationException}
import org.everit.json.schema.loader.SchemaLoader
import org.json.{JSONObject, JSONTokener}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class Rfc2616Attribute extends FormatValidator {

  override def formatName = Rfc2616Attribute.name

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
  val pattern = "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), (0[1-9]|[12][0-9]|3[01]) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) ([0-9]{4}) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60) (GMT)$"
}

object Rfc2616AttributeLowerCase extends Rfc2616Attribute {
  override def formatName = "rfc2616"
}

object DateTimeOnlyFormatValidator extends FormatValidator {
  private val FORMATS_ACCEPTED = ImmutableList.of("yyyy-MM-ddTHH:mm:ss")
  private var FORMATTER: DateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss").toFormatter

  override def formatName = "date-time-only"


  override def validate(value: String): Optional[String] = try {
    FORMATTER.parse(value)
    Optional.empty()
  } catch {
    case _: DateTimeParseException => Optional.of(String.format("[%s] is not a valid %s. Expected %s", value, this.formatName(), "yyyy-MM-dd'T'HH:mm:ss"))
  }
}

object PartialTimeFormatValidator extends FormatValidator {
  private var PATTERN = "^([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60)$"

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

  val AML_JSON_SCHEMA = "http://a.aml/amfschema#" // identifier of our custom schema

  def validate(validationCandidates: Seq[ValidationCandidate],
               profile: ValidationProfile): Future[AMFValidationReport] = {

    val jsonSchemaCandidates: Seq[(PayloadFragment, String, String, Option[Throwable])] = computeJsonSchemaCandidates(validationCandidates)


    val results = jsonSchemaCandidates flatMap  { case (payload, jsonSchema, shapeId, maybeException) =>

      maybeException match {
        case Some(e: UnknownDiscriminator) => Seq( // error case for polymorphic shapes without matching discriminator
          AMFValidationResult(
            message = "Unknown discriminator value",
            level = SeverityLevels.VIOLATION,
            targetNode = payload.encodes.id,
            targetProperty = None,
            validationId = (Namespace.AmfParser + "exampleError").iri(),
            position = payload.encodes.annotations.find(classOf[LexicalInformation]),
            location = None,
            source = e
          )
        )
        case Some(_)                       => Nil // we ignore other exceptions
        case None                          => // standard ase, we have a payload and shape
          loadDataNodeString(payload) match {
            case Some(dataNode) =>

              loadJson(jsonSchema) match {
                case schemaNode: JSONObject =>
                  schemaNode.remove("x-amf-fragmentType")
                  schemaNode.remove("example")
                  schemaNode.remove("examples")

                  val schemaBuilder = SchemaLoader.builder()
                    .schemaJson(schemaNode)
                    .addFormatValidator(DateTimeOnlyFormatValidator)
                    .addFormatValidator(Rfc2616Attribute)
                    .addFormatValidator(Rfc2616AttributeLowerCase)
                    .addFormatValidator(new DateFormatValidator())
                    .addFormatValidator(new URIFormatValidator())
                    .addFormatValidator(new RegexFormatValidator())
                    .addFormatValidator(PartialTimeFormatValidator)

                  val schemaLoader = schemaBuilder.build()
                  val schema = schemaLoader.load().build()

                  /*
                  println("\n\nValidating...")
                  println("  - SCHEMA:")
                  println(jsonSchema)
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

                      var resultsAcc = Seq[AMFValidationResult]()
                      val results = validationException.getCausingExceptions.iterator()
                      while (results.hasNext) {
                        val result = results.next()
                        resultsAcc = resultsAcc :+ AMFValidationResult(
                          message = result.toJSON.toString(),
                          level = SeverityLevels.VIOLATION,
                          targetNode = payload.encodes.id,
                          targetProperty = None,
                          validationId = (Namespace.AmfParser + "exampleError").iri(),
                          position = payload.encodes.annotations.find(classOf[LexicalInformation]),
                          location = None,
                          source = validationException
                        )
                      }
                      if (resultsAcc.isEmpty) {
                        resultsAcc = resultsAcc :+ AMFValidationResult(
                          message = validationException.toJSON.toString(),
                          level = SeverityLevels.VIOLATION,
                          targetNode = payload.encodes.id,
                          targetProperty = None,
                          validationId = (Namespace.AmfParser + "exampleError").iri(),
                          position = payload.encodes.annotations.find(classOf[LexicalInformation]),
                          location = None,
                          source = validationException
                        )
                      }
                      resultsAcc
                  }

                case _ => Nil // schema is not a JSON object
              }
            case _ => Nil
          }
      }
    }

    Future {
      AMFValidationReport(
        conforms = results.isEmpty,
        model = "http://test.com/paylaod#validations",
        profile = profile.name, // profiles.headOption.map(_.name).getOrElse(ProfileNames.AMF)
        results = results
      )
    }
  }

  protected def loadDataNodeString(payload: PayloadFragment): Option[Object] = {
    try {
      literalRepresentation(payload) map { payloadText =>
        loadJson(payloadText)
      }
    } catch {
      case _: ExampleUnknownException => None
    }
  }

  protected def loadJson(text: String): Object = new JSONTokener(text).nextValue()
}
