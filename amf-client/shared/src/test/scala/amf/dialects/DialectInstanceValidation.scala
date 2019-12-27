package amf.dialects

import amf.ProfileName
import amf.client.parse.DefaultParserErrorHandler
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.core.{AMFCompiler, CompilerContextBuilder}
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.features.validation.AMFValidatorPlugin
import org.scalatest.AsyncFunSuite

abstract class DialectInstanceValidation extends AsyncFunSuite with PlatformSecrets {

  def basePath: String

  protected def validate(dialect: String, instance: String, expectedErrorCount: Int, path: String = basePath) = {
    amf.core.AMF.registerPlugin(plugin = AMLPlugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    val dialectContext  = compilerContext(path + dialect)
    val instanceContext = compilerContext(path + instance)

    for {
      _ <- amf.core.AMF.init()
      dialect <- {
        new AMFCompiler(
          dialectContext,
          Some("application/yaml"),
          None
        ).build()
      }
      instance <- {
        new AMFCompiler(
          instanceContext,
          Some("application/yaml"),
          None
        ).build()
      }
      report <- RuntimeValidator(instance, ProfileName(dialect.asInstanceOf[Dialect].nameAndVersion()))
    } yield {
      if (expectedErrorCount == 0) {
        if (!report.conforms)
          println(report)
        assert(report.conforms)
      } else assert(report.results.length == expectedErrorCount)
    }
  }

  protected def withCustomValidationProfile(dialect: String,
                                            instance: String,
                                            profile: ProfileName,
                                            name: String,
                                            numErrors: Int,
                                            directory: String = basePath) = {
    amf.core.AMF.registerPlugin(AMLPlugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    val dialectContext  = compilerContext(directory + dialect)
    val instanceContext = compilerContext(directory + instance)

    for {
      _ <- amf.core.AMF.init()
      dialect <- {
        new AMFCompiler(
          dialectContext,
          Some("application/yaml"),
          None
        ).build()
      }
      profile <- {
        AMFValidatorPlugin.loadValidationProfile(directory + profile.profile,
                                                 errorHandler = dialectContext.parserContext.eh)
      }
      instance <- {

        new AMFCompiler(
          instanceContext,
          Some("application/yaml"),
          None
        ).build()
      }
      report <- {
        RuntimeValidator(
          instance,
          ProfileName(name)
        )
      }
    } yield {
      if (numErrors == 0) {
        if (!report.conforms)
          println(report)
        assert(report.conforms)
      } else assert(report.results.length == numErrors)
    }
  }

  private def compilerContext(url: String) =
    new CompilerContextBuilder(url, platform, eh = DefaultParserErrorHandler.withRun()).build()

}
