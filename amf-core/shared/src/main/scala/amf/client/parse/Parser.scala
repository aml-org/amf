package amf.client.parse

import amf.ProfileNames.{MessageStyle, ProfileName, RAMLStyle}
import amf.client.convert.CoreClientConverters._
import amf.client.environment.{DefaultEnvironment, Environment}
import amf.client.model.document.BaseUnit
import amf.client.validate.ValidationReport
import amf.core.client.ParsingOptions
import amf.core.model.document.{BaseUnit => InternalBaseUnit}
import amf.core.remote.Context
import amf.core.services.{RuntimeCompiler, RuntimeValidator}
import amf.internal.resource.{ResourceLoader, StringResourceLoader}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * Base class for parsers.
  */
class Parser(vendor: String, mediaType: String, private val env: Option[Environment]) {

  private var parsedModel: Option[InternalBaseUnit] = None

  /**
    * Generates a BaseUnit from the api at the given url.
    * @param url : Location of the base unit.
    * @param handler Handler object to execute the success or fail functions with the result object model.
    */
  @JSExport
  def parseFile(url: String, handler: ClientResultHandler[BaseUnit]): Unit = parse(url, handler)

  /**
    * Generates the BaseUnit from a given string.
    * @param stream: The unit as string.
    * @param handler Handler object to execute the success or fail functions with the result object model.
    */
  @JSExport
  def parseString(stream: String, handler: ClientResultHandler[BaseUnit]): Unit =
    parse(DEFAULT_DOCUMENT_URL, handler, Some(fromStream(stream)))

  /**
    * Generates the BaseUnit from a given string.
    *
    * @param url: Base URL to used in the graph of information generated for the input stream of data
    * @param stream: The api as a string.
    * @param handler Handler object to execute the success or fail functions with the result object model.
    */
  @JSExport
  def parseString(url: String, stream: String, handler: ClientResultHandler[BaseUnit]): Unit =
    parse(url, handler, Some(fromStream(url, stream)))

  /**
    * Asynchronously generate a BaseUnit from the unit located in the given url.
    * @param url : Location of the api.
    * @return A future that will have a BaseUnit or an error to handle the result of such invocation.
    */
  @JSExport
  def parseFileAsync(url: String): ClientFuture[BaseUnit] = parseAsync(url).asClient

  /**
    * Asynchronously generate a BaseUnit from a given string.
    * @param stream: The unit as a string
    * @return A future that will have a BaseUnit or an error to handle the result of such invocation.
    */
  @JSExport
  def parseStringAsync(stream: String): ClientFuture[BaseUnit] =
    parseAsync(DEFAULT_DOCUMENT_URL, Some(fromStream(stream))).asClient

  @JSExport
  def parseStringAsync(url: String, stream: String): ClientFuture[BaseUnit] =
    parseAsync(url, Some(fromStream(url, stream))).asClient

  /**
    * Generates the validation report for the last parsed model.
    * @param profile the profile to be parsed
    * @param messageStyle if a RAML/OAS profile, this can be set to the preferred error reporting styl
    * @return the AMF validation report
    */
  @JSExport
  def reportValidation(profile: ProfileName, messageStyle: MessageStyle): ClientFuture[ValidationReport] =
    report(profile, messageStyle)

  @JSExport
  def reportValidation(profile: ProfileName): ClientFuture[ValidationReport] = report(profile)

  /**
    * Generates a custom validaton profile as specified in the input validation profile file
    * @param profile the profile to be parsed
    * @param customProfilePath path to the custom profile file
    * @return the AMF validation report
    */
  @JSExport
  def reportCustomValidation(profile: ProfileName, customProfilePath: String): ClientFuture[ValidationReport] =
    reportCustomValidationImplementation(profile, customProfilePath)

  private def parseAsync(url: String,
                         loader: Option[ResourceLoader] = None,
                         parsingOptions: ParsingOptions = ParsingOptions()): Future[InternalBaseUnit] = {
    RuntimeValidator.reset()

    val environment = {
      val e = env.getOrElse(DefaultEnvironment())._internal
      loader.map(e.add).getOrElse(e)
    }

    RuntimeCompiler(url, Option(mediaType), vendor, Context(platform), env = environment) map { model =>
      parsedModel = Some(model)
      model
    }
  }

  /**
    * Generates the validation report for the last parsed model.
    * @param profileName the profile to be parsed
    * @param messageStyle if a RAML/OAS profile, this can be set to the preferred error reporting style
    * @return the AMF validation report
    */
  private def report(profileName: ProfileName,
                     messageStyle: MessageStyle = RAMLStyle): ClientFuture[ValidationReport] = {

    val result = parsedModel.map(RuntimeValidator(_, profileName, messageStyle)) match {
      case Some(validation) => validation
      case None             => Future.failed(new Exception("No parsed model or current validation found, cannot validate"))
    }

    result.asClient
  }

  /**
    * Generates a custom validaton profile as specified in the input validation profile file
    * @param profileName name of the profile to be parsed
    * @param customProfilePath path to the custom profile file
    * @return the AMF validation report
    */
  private def reportCustomValidationImplementation(profileName: ProfileName,
                                                   customProfilePath: String): ClientFuture[ValidationReport] = {
    val result = parsedModel match {
      case Some(model) =>
        for {
          _      <- RuntimeValidator.loadValidationProfile(customProfilePath)
          report <- RuntimeValidator(model, profileName)
        } yield {
          report
        }
      case _ => throw new Exception("Cannot validate without parsed model")
    }

    result.asClient
  }

  private def parse(url: String, handler: ClientResultHandler[BaseUnit], loader: Option[ResourceLoader] = None): Unit =
    parseAsync(url, loader)
      .onComplete {
        case Success(result: InternalBaseUnit) => handler.success(result)
        case Failure(exception)                => handler.error(exception)
      }

  private def fromStream(url: String, stream: String): ResourceLoader =
    StringResourceLoader(platform.resolvePath(url), stream)

  private def fromStream(stream: String): ResourceLoader = fromStream(DEFAULT_DOCUMENT_URL, stream)

  private val DEFAULT_DOCUMENT_URL = "http://a.ml/amf/default_document"
}
