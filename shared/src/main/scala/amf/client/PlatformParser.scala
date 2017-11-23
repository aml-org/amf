package amf.client

import amf.ProfileNames
import amf.compiler.AMFCompiler
import amf.framework.document.BaseUnit
import amf.framework.validation.AMFValidationReport
import amf.remote.Syntax.{Json, Syntax, Yaml}
import amf.remote._
import amf.unsafe.PlatformSecrets
import amf.validation.Validation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

private[client] abstract class PlatformParser extends PlatformSecrets {

  protected val vendor: Vendor
  protected val syntax: Syntax
  var currentValidation: Option[Validation] = None
  var parsedModel: Option[BaseUnit] = None

  protected def parseAsync(url: String, overridePlatForm: Option[Platform] = None, parsingOptions: ParsingOptions = ParsingOptions()): Future[BaseUnit] = {
    val effectivePlatform = overridePlatForm.getOrElse(platform)
    currentValidation = Some(new Validation(effectivePlatform))
    AMFCompiler(url, effectivePlatform, hint(), currentValidation.get, None, None).build() map { model =>
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
  protected def reportValidationImplementation(profileName: String, messageStyle:String = ProfileNames.RAML): Future[AMFValidationReport] = {
    val maybeValidationReport = for {
      model      <- parsedModel
      validation <- currentValidation
    } yield {
      validation.validate(model, profileName, messageStyle)
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
  protected def reportCustomValidationImplementation(profileName: String, customProfilePath: String): Future[AMFValidationReport] = {
    val maybeValidationReport = for {
      model      <- parsedModel
      validation <- currentValidation
    } yield {
      validation.loadValidationDialect() flatMap   { _ =>
        validation.loadValidationProfile(customProfilePath)
      } flatMap { res =>
        validation.validate(model,profileName)
      }
    }

    maybeValidationReport match {
      case Some(validation) => validation
      case None =>
        val promise = Promise[AMFValidationReport]()
        promise.failure(new Exception("No parsed model or current validation found, cannot validate"))
        promise.future
    }
  }

  protected def parse(url: String, handler: Handler[BaseUnit], overridePlatForm: Option[Platform] = None): Unit =
    parseAsync(url, overridePlatForm)
      .onComplete(callback(handler, url))

  private def hint(): Hint = {
    (vendor, syntax) match {
      case (Raml, Yaml) => RamlYamlHint
      case (Oas, Json)  => OasJsonHint
      case (Amf, Json)  => AmfJsonHint
      case _            => throw new RuntimeException(s"Unable conbination of vendor '$vendor' and syntax '$syntax'")
    }
  }

  private def callback(handler: Handler[BaseUnit], url: String)(t: Try[BaseUnit]) = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }
}
