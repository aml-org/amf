package amf.plugins.document.webapi.validation.remote

import amf.ProfileName
import amf.client.plugins.ValidationMode
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.utils.RegexConverter
import amf.core.validation.{AMFValidationResult, SeverityLevels}
import amf.core.vocabulary.Namespace
import org.everit.json.schema.internal.{DateFormatValidator, RegexFormatValidator, URIFormatValidator}
import org.everit.json.schema.loader.SchemaLoader
import org.everit.json.schema.regexp.{JavaUtilRegexpFactory, Regexp}
import org.everit.json.schema.{Schema, ValidationException, Validator}
import org.json.{JSONException, JSONObject}

class JvmPayloadValidator(val shape: Shape, val validationMode: ValidationMode)
    extends PlatformPayloadValidator(shape) {

  case class CustomJavaUtilRegexpFactory() extends JavaUtilRegexpFactory {
    override def createHandler(regexp: String): Regexp = super.createHandler(regexp.convertRegex)
  }

  override protected def callValidator(schema: LoadedSchema,
                                       payload: LoadedObj,
                                       fragment: Option[PayloadFragment],
                                       validationProcessor: ValidationProcessor): validationProcessor.Return = {
    val validator = validationProcessor match {
      case BooleanValidationProcessor => Validator.builder().failEarly().build()
      case _                          => Validator.builder().build()
    }

    try {
      validator.performValidation(schema, payload)
      validationProcessor.processResults(Nil)
    } catch {
      case validationException: ValidationException =>
        validationProcessor.processException(validationException, fragment)
      case exception: Error =>
        validationProcessor.processException(exception, fragment)
    }
  }

  override protected def loadSchema(jsonSchema: CharSequence): Option[LoadedSchema] = {

    loadJson(
      jsonSchema.toString
        .replace("\"type\": \"file\"", "\"type\": \"string\"")
        .replace("x-amf-union", "anyOf")) match {
      case schemaNode: JSONObject =>
        schemaNode.remove("x-amf-fragmentType")
        schemaNode.remove("example")
        schemaNode.remove("examples")
        schemaNode.remove("x-amf-examples")

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
        Some(
          schemaBuilder
            .build()
            .load()
            .build())
      case _ => None
    }
  }

  override type LoadedObj    = Object
  override type LoadedSchema = Schema

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

  override protected def loadJson(text: String): Object = new JSONTokenerHack(text).nextValue()
  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JvmReportValidationProcessor(profileName)
}

case class JvmReportValidationProcessor(override val profileName: ProfileName) extends ReportValidationProcessor {

  override def processException(r: Throwable, fragment: Option[PayloadFragment]): Return = {
    val results = r match {
      case validationException: ValidationException =>
        iterateValidations(validationException, fragment)

      case other =>
        super.processCommonException(other, fragment)
    }
    processResults(results)
  }

  private def iterateValidations(validationException: ValidationException,
                                 payload: Option[PayloadFragment]): Seq[AMFValidationResult] = {
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
        targetNode = payload.map(_.encodes.id).getOrElse(""),
        targetProperty = None,
        validationId = (Namespace.AmfParser + "example-validation-error").iri(),
        position = payload.flatMap(_.encodes.position()),
        location = payload.flatMap(_.encodes.location()),
        source = validationException
      )
    }
    resultsAcc
  }

  private def makeValidationMessage(validationException: ValidationException): String = {
    val json    = validationException.toJSON
    var pointer = json.getString("pointerToViolation")
    if (pointer.startsWith("#")) pointer = pointer.replaceFirst("#", "")
    (pointer + " " + json.getString("message")).trim
  }
}
