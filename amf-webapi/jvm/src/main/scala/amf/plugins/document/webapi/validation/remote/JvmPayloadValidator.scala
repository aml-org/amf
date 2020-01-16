package amf.plugins.document.webapi.validation.remote

import amf.ProfileName
import amf.client.plugins.ValidationMode
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.utils.RegexConverter
import amf.core.validation.{AMFValidationResult, SeverityLevels}
import amf.plugins.document.webapi.validation.json.{JSONObject, JSONTokenerHack}
import amf.validations.PayloadValidations.{
  ExampleValidationErrorSpecification,
  SchemaException => InternalSchemaException
}
import org.everit.json.schema.internal.{DateFormatValidator, RegexFormatValidator, URIFormatValidator}
import org.everit.json.schema.loader.SchemaLoader
import org.everit.json.schema.regexp.{JavaUtilRegexpFactory, Regexp}
import org.everit.json.schema.{Schema, SchemaException, ValidationException, Validator}
import org.json.JSONException

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
        validationProcessor.processException(validationException, fragment.map(_.encodes))
      case exception: Error =>
        validationProcessor.processException(exception, fragment.map(_.encodes))
    }
  }

  override protected def loadSchema(
      jsonSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor): Either[validationProcessor.Return, Option[LoadedSchema]] = {

    loadJson(
      jsonSchema.toString
        .replace("x-amf-union", "anyOf")) match {
      case schemaNode: JSONObject =>
        schemaNode.remove("x-amf-fragmentType")

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

        try {
          Right(
            Some(
              schemaBuilder
                .build()
                .load()
                .build())
          )
        } catch {
          case e: SchemaException =>
            Left(validationProcessor.processException(e, Some(element)))
        }

      case _ => Right(None)
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

  override protected def loadJson(text: String): Object = {
    try new JSONTokenerHack(text).nextValue()
    catch {
      case e: JSONException => throw new InvalidJsonObject(e)
    }
  }

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JvmReportValidationProcessor(profileName)
}

case class JvmReportValidationProcessor(override val profileName: ProfileName) extends ReportValidationProcessor {

  override def processException(r: Throwable, element: Option[DomainElement]): Return = {
    val results = r match {
      case validationException: ValidationException =>
        iterateValidations(validationException, element)

      case e: SchemaException =>
        Seq(
          AMFValidationResult(
            message = e.getMessage,
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = None,
            validationId = InternalSchemaException.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = e
          ))

      case other =>
        super.processCommonException(other, element)
    }
    processResults(results)
  }

  private def iterateValidations(validationException: ValidationException,
                                 element: Option[DomainElement]): Seq[AMFValidationResult] = {
    var resultsAcc = Seq[AMFValidationResult]()
    val results    = validationException.getCausingExceptions.iterator()
    while (results.hasNext) {
      val result = results.next()
      resultsAcc = resultsAcc ++ iterateValidations(result, element)
    }
    if (resultsAcc.isEmpty) {
      resultsAcc = resultsAcc :+ AMFValidationResult(
        message = makeValidationMessage(validationException),
        level = SeverityLevels.VIOLATION,
        targetNode = element.map(_.id).getOrElse(""),
        targetProperty = element.map(_.id),
        validationId = ExampleValidationErrorSpecification.id,
        position = element.flatMap(_.position()),
        location = element.flatMap(_.location()),
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
