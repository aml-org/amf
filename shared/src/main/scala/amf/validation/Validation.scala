package amf.validation

import amf.compiler.AMFCompiler
import amf.dialects.Dialect
import amf.document.BaseUnit
import amf.remote.{Platform, RamlYamlHint}

import scala.concurrent.Future

class Validation(platform: Platform) {

  /**
    * Loads the validation dialect from the provided URL
    */
  def loadValidationDialect(validationDialectUrl: String): Future[Dialect] = platform.dialectsRegistry.registerDialect(validationDialectUrl)


  def loadValidationProfile(validationProfilePath: String): Future[BaseUnit] = {
    val compiler = AMFCompiler(validationProfilePath, platform, RamlYamlHint, None, None, platform.dialectsRegistry)
    compiler.build()
  }
}

object Validation {
  def apply(platform: Platform): Validation = new Validation(platform)
}