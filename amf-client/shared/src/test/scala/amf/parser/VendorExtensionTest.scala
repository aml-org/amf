package amf.parser

import _root_.org.scalatest.Matchers
import amf.Raml10Profile
import amf.client.AMF
import amf.client.convert.{CoreClientConverters, NativeOps}
import amf.client.parse.DefaultParserErrorHandler
import amf.client.render._
import amf.core.{AMFCompiler, CompilerContextBuilder}
import amf.core.remote.{Amf, Aml, Oas20, Raml10}
import amf.facades.Validation
import amf.internal.environment.Environment
import amf.io.{FileAssertionTest, MultiJsonldAsyncFunSuite}

import scala.concurrent.ExecutionContext


trait VendorExtensionTest extends MultiJsonldAsyncFunSuite with Matchers with NativeOps with FileAssertionTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  multiGoldenTest("Parse vendor extension correctly",
    "file://amf-client/shared/src/test/resources/vendor-extensions/api.%s") { config =>
    val apiPath = "file://amf-client/shared/src/test/resources/vendor-extensions/api.raml"
    val extensionPath = "file://amf-client/shared/src/test/resources/vendor-extensions/extension.yaml"
    val golden  = config.golden

    // TODO migrate to render options converter
    var options = new RenderOptions().withCompactUris.withSourceMaps.withPrettyPrint
    if (config.renderOptions.isFlattenedJsonLd) {
      options = options.withFlattenedJsonLd
    } else {
      options = options.withoutFlattenedJsonLd
    }

    val context =
      new CompilerContextBuilder(extensionPath, platform, DefaultParserErrorHandler.withRun()).build()

    for {
      _      <- AMF.init().asFuture
      _      <- new AMFCompiler(context, None, Some(Aml.name)).build()
      parsed <- amf.Core.parser(Raml10.name, "application/yaml").parseFileAsync(apiPath).asFuture
      //      resolved <- Future.successful(amf.Core.resolver(Raml10.name).resolve(parsed, ResolutionPipeline.DEFAULT_PIPELINE))
      jsonLd <- amf.Core.generator("AMF Graph", "application/ld+json").generateString(parsed, options).asFuture
      actual <- writeTemporaryFile(golden)(jsonLd)
      r      <- assertDifferences(actual, golden)
    } yield {
      r
    }
  }

  multiGoldenTest("Parse vendor extension correctly for the connectivity dialect",
    "file://amf-client/shared/src/test/resources/vendor-extensions/slack.%s") { config =>
    val apiPath = "file://amf-client/shared/src/test/resources/vendor-extensions/slack.yaml"
    val extensionPath = "file://amf-client/shared/src/test/resources/vendor-extensions/connector.yaml"
    val golden  = config.golden

    // TODO migrate to render options converter
    var options = new RenderOptions().withCompactUris.withSourceMaps.withPrettyPrint
    if (config.renderOptions.isFlattenedJsonLd) {
      options = options.withFlattenedJsonLd
    } else {
      options = options.withoutFlattenedJsonLd
    }

    val context =
      new CompilerContextBuilder(extensionPath, platform, DefaultParserErrorHandler.withRun()).build()

    for {
      _      <- AMF.init().asFuture
      _      <- new AMFCompiler(context, None, Some(Aml.name)).build()
      parsed <- amf.Core.parser(Oas20.name, "application/yaml").parseFileAsync(apiPath).asFuture
      //      resolved <- Future.successful(amf.Core.resolver(Raml10.name).resolve(parsed, ResolutionPipeline.DEFAULT_PIPELINE))
      jsonLd <- amf.Core.generator("AMF Graph", "application/ld+json").generateString(parsed, options).asFuture
      actual <- writeTemporaryFile(golden)(jsonLd)
      r      <- assertDifferences(actual, golden)
    } yield {
      r
    }
  }

  multiGoldenTest("Validate the extension", "api.%s") { config =>
    val apiPath = "file://amf-client/shared/src/test/resources/vendor-extensions/api.invalid.raml"
    val directory = "file://amf-client/shared/src/test/resources/vendor-extensions/"
    val extensionPath = s"${directory}extension.yaml"
    val context =
      new CompilerContextBuilder(extensionPath, platform, DefaultParserErrorHandler.withRun()).build()

    for {
      _          <- AMF.init().asFuture
      validation <- Validation(platform)
      _          <- new AMFCompiler(context, None, Some(Aml.name)).build()
      parsed     <- amf.Core.parser(Raml10.name, "application/yaml").parseFileAsync(apiPath).asFuture
      report     <- validation.validate(parsed._internal, Raml10Profile)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 5) // @todo do a proper report test
    }
  }

}
