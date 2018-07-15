package amf.client.parse

import amf.client.convert.CoreClientConverters._
import amf.client.environment.{DefaultEnvironment, Environment}
import amf.client.model.document.BaseUnit
import amf.client.validate.ValidationReport
import amf.core.client.ParsingOptions
import amf.core.model.document.{BaseUnit => InternalBaseUnit}
import amf.core.remote.Context
import amf.core.services.{RuntimeCompiler, RuntimeValidator}
import amf.internal.resource.{ResourceLoader, StringResourceLoader}
import amf.{MessageStyle, ProfileName, RAMLStyle}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExport

/**
  * Base class for parsers.
  */
class Parser(vendor: String, mediaType: String, private val env: Option[Environment]) {

  private var parsedModel: Option[InternalBaseUnit] = None

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
      val e = internalEnv()
      loader.map(e.add).getOrElse(e)
    }

    RuntimeCompiler(url, Option(mediaType), vendor, Context(platform), env = environment) map { model =>
      parsedModel = Some(model)
      model
    }
  }

  private def internalEnv() = env.getOrElse(DefaultEnvironment())._internal

  /**
    * Generates the validation report for the last parsed model.
    *
    * @param profileName the profile to be parsed
    * @param messageStyle if a RAML/OAS profile, this can be set to the preferred error reporting style
    * @return the AMF validation report
    */
  private def report(profileName: ProfileName,
                     messageStyle: MessageStyle = RAMLStyle): ClientFuture[ValidationReport] = {

    val result = parsedModel.map(RuntimeValidator(_, profileName, messageStyle, internalEnv())) match {
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
          report <- RuntimeValidator(model, profileName, env = internalEnv())
        } yield {
          report
        }
      case _ => throw new Exception("Cannot validate without parsed model")
    }

    result.asClient
  }

  private def fromStream(url: String, stream: String): ResourceLoader =
    StringResourceLoader(platform.resolvePath(url), stream)

  private def fromStream(stream: String): ResourceLoader = fromStream(DEFAULT_DOCUMENT_URL, stream)

  private val DEFAULT_DOCUMENT_URL = "http://a.ml/amf/default_document"
}
