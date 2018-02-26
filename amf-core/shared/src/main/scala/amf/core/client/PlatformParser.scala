package amf.core.client

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.services.{RuntimeCompiler, RuntimeValidator}
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.AMFValidationReport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

abstract class PlatformParser extends PlatformSecrets {

  protected val vendor: String
  protected val mediaType: String
  var parsedModel: Option[BaseUnit] = None

  protected def parseAsync(url: String,
                           overridePlatForm: Option[Platform] = None,
                           parsingOptions: ParsingOptions = ParsingOptions()): Future[BaseUnit] = {
    RuntimeValidator.reset()
    RuntimeCompiler(url, Option(mediaType), vendor, Context(overridePlatForm.getOrElse(platform))) map { model =>
      parsedModel = Some(model)
      model
    }
  }

  /**
    * Generates the validation report for the last parsed model.
    * @param profileName name of the profile to be parsed
    * @param messageStyle if a RAML/OAS profile, this can be set to the preferred error reporting styl
    * @return the AMF validation report
    */
  protected def reportValidationImplementation(
      profileName: String,
      messageStyle: String = ProfileNames.RAML): Future[AMFValidationReport] = {
    val maybeValidationReport = for {
      model <- parsedModel
    } yield {
      RuntimeValidator(model, profileName, messageStyle)
    }

    maybeValidationReport match {
      case Some(validation) => validation
      case None =>
        val promise = Promise[AMFValidationReport]()
        promise.failure(new Exception("No parsed model or current validation found, cannot validate"))
        promise.future
    }
  }

  /**
    * Generates a custom validaton profile as specified in the input validation profile file
    * @param profileName name of the profile to be parsed
    * @param customProfilePath path to the custom profile file
    * @return the AMF validation report
    */
  protected def reportCustomValidationImplementation(profileName: String,
                                                     customProfilePath: String): Future[AMFValidationReport] = {
    parsedModel match {
      case Some(model) =>
        for {
          _      <- RuntimeValidator.loadValidationProfile(customProfilePath)
          report <- RuntimeValidator(model, profileName)
        } yield {
          report
        }
      case _ => throw new Exception("Cannot validate without parsed model")
    }
  }

  protected def parse(url: String, handler: Handler[BaseUnit], overridePlatForm: Option[Platform] = None): Unit =
    parseAsync(url, overridePlatForm)
      .onComplete(callback(handler, url))

  /*
  private def effectiveVendor(): String = vendor match {
    case Raml => "RAML 1.0"
    case Oas  => "OAS 2.0"
    case Amf  => "AMF Graph"
    case _    =>  vendor.name
  }
  private def effectiveMediaType(): String = syntax match {
    case Yaml  => "application/yaml"
    case Json  => "application/json"
    case _     => s"application/${syntax.extension}"
  }
   */

  private def callback(handler: Handler[BaseUnit], url: String)(t: Try[BaseUnit]) = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }
}
