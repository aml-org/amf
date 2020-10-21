package amf.plugins.document.webapi.validation.remote

import amf.ProfileName
import amf.client.plugins.ValidationMode
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.utils.RegexConverter
import amf.core.validation.{AMFValidationResult, SeverityLevels}
import amf.internal.environment.Environment
import amf.plugins.document.webapi.validation.json.{
  InvalidJSONValueException,
  JSONObject,
  JSONTokenerHack,
  ScalarTokenerHack
}
import amf.plugins.domain.shapes.models.ScalarShape
import amf.validations.PayloadValidations.{
  ExampleValidationErrorSpecification,
  SchemaException => InternalSchemaException
}
import org.everit.json.schema.internal.{
  DateFormatValidator,
  DateTimeFormatValidator,
  EmailFormatValidator,
  HostnameFormatValidator,
  IPV4Validator,
  IPV6Validator,
  RegexFormatValidator,
  URIFormatValidator,
  URIV4FormatValidator
}
import org.everit.json.schema.loader.SchemaLoader
import org.everit.json.schema.regexp.{JavaUtilRegexpFactory, Regexp}
import org.everit.json.schema.{Schema, SchemaException, ValidationException, Validator}
import org.json.JSONException

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

class JvmPayloadValidator(val shape: Shape, val validationMode: ValidationMode, val env: Environment)
    extends PlatformPayloadValidator(shape, env) {

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

    loadJsonSchema(
      jsonSchema.toString
        .replace("x-amf-union", "anyOf")) match {
      case schemaNode: JSONObject =>
        schemaNode.remove("x-amf-fragmentType")

        val schemaBuilder = SchemaLoader
          .builder()
          .schemaJson(schemaNode)
          .draftV7Support()
          .regexpFactory(CustomJavaUtilRegexpFactory())
          .addFormatValidator(DateTimeOnlyFormatValidator)
          .addFormatValidator(Rfc2616Attribute)
          .addFormatValidator(Rfc2616AttributeLowerCase)
          .addFormatValidator(PartialTimeFormatValidator)
          // the following are everit format validators
          .addFormatValidator(new URIV4FormatValidator())
          .addFormatValidator(new DateFormatValidator())
          .addFormatValidator(new RegexFormatValidator())
          .addFormatValidator(new HostnameFormatValidator())
          .addFormatValidator(new IPV4Validator())
          .addFormatValidator(new DateTimeFormatValidator())
          .addFormatValidator(new IPV6Validator())
          .addFormatValidator(new EmailFormatValidator())

        try {
          // does not use schemaBuilder.build() as this causes formats of schema version to override custom format validators
          val loader = new SchemaLoader(schemaBuilder)
          Right(Some(loader.load().build()))
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
        loadJsonSchema(payloadText)
      }
    } catch {
      case _: ExampleUnknownException => None
      case e: JSONException           => throw new InvalidJsonObject(e)
    }
  }

  override protected def loadJsonSchema(text: String): Object = {
    withJsonExceptionCatching(() => {
      new JSONTokenerHack(text).nextValue()
    })
  }

  override protected def loadJson(text: String): Object = {
    withJsonExceptionCatching(() => {
      shape match {
        case _: ScalarShape => new ScalarTokenerHack(text).nextValue()
        case _              => new JSONTokenerHack(text).nextValue()
      }
    })
  }

  private def withJsonExceptionCatching(jsonLoading: () => Object): Object = {
    try jsonLoading()
    catch {
      case e: InvalidJSONValueException => throw new InvalidJsonValue(e)
      case e: JSONException             => throw new InvalidJsonObject(e)
    }
  }

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JvmReportValidationProcessor(profileName, shape)
}

case class JvmReportValidationProcessor(override val profileName: ProfileName, shape: Shape)
    extends ReportValidationProcessor {

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

      case e: InvalidJsonValue if shape.isInstanceOf[ScalarShape] =>
        Seq(
          invalidJsonValidation(s"expected type: ${formattedDatatype(shape.asInstanceOf[ScalarShape])}, found: String",
                                element,
                                e))

      case e: InvalidJsonValue =>
        Seq(invalidJsonValidation("Invalid json value was provided", element, e))

      case other =>
        super.processCommonException(other, element)
    }
    processResults(results)
  }

  private def invalidJsonValidation(message: String, element: Option[DomainElement], e: RuntimeException) = {
    AMFValidationResult(
      message = message,
      level = SeverityLevels.VIOLATION,
      targetNode = element.map(_.id).getOrElse(""),
      targetProperty = None,
      validationId = ExampleValidationErrorSpecification.id,
      position = element.flatMap(_.position()),
      location = element.flatMap(_.location()),
      source = e
    )
  }

  private def formattedDatatype(scalarShape: ScalarShape): String =
    scalarShape.dataType.value().split("#").last.capitalize

  private def iterateValidations(validationException: ValidationException,
                                 element: Option[DomainElement]): Seq[AMFValidationResult] = {

    var exceptionsStack: List[ValidationException] = List(validationException)

    var accumulator = Seq[AMFValidationResult]()

    while (exceptionsStack.nonEmpty) {
      val exception = exceptionsStack.head
      exceptionsStack = exceptionsStack.tail

      if (exception.getCausingExceptions.isEmpty) {
        accumulator = AMFValidationResult(
          message = makeValidationMessage(exception),
          level = SeverityLevels.VIOLATION,
          targetNode = element.map(_.id).getOrElse(""),
          targetProperty = element.map(_.id),
          validationId = ExampleValidationErrorSpecification.id,
          position = element.flatMap(_.position()),
          location = element.flatMap(_.location()),
          source = validationException
        ) +: accumulator
      } else {
        exceptionsStack = exception.getCausingExceptions.toList ::: exceptionsStack
      }
    }
    accumulator
  }

  private def makeValidationMessage(validationException: ValidationException): String = {
    val json    = validationException.toJSON
    var pointer = json.getString("pointerToViolation")
    if (pointer.startsWith("#")) pointer = pointer.replaceFirst("#", "")
    (pointer + " " + json.getString("message")).trim
  }
}
