package amf.parser

import _root_.org.scalatest.Matchers
import amf.client.AMF
import amf.client.convert.{CoreClientConverters, NativeOps}
import amf.client.parse.DefaultParserErrorHandler
import amf.client.render._
import amf.core.{AMFCompiler, CompilerContextBuilder}
import amf.core.remote.{Amf, Aml, Raml10}
import amf.io.{FileAssertionTest, MultiJsonldAsyncFunSuite}

import scala.concurrent.ExecutionContext


trait VendorExtensionTest extends MultiJsonldAsyncFunSuite with Matchers with NativeOps with FileAssertionTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  multiGoldenTest("HERE_HERE Parse vendor extension correctly",
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

  multiGoldenTest("HERE_HERE Generate vendor extension correctly", "api.%s") { config =>

    val directory = "file://amf-client/shared/src/test/resources/vendor-extensions/"
    val target = s"api.raml"
    val extensionPath = s"${directory}extension.yaml"
    val context =
      new CompilerContextBuilder(extensionPath, platform, DefaultParserErrorHandler.withRun()).build()

    val options = new RenderOptions().withCompactUris.withSourceMaps.withPrettyPrint

    for {
      _      <- AMF.init().asFuture
      _      <- new AMFCompiler(context, None, Some(Aml.name)).build()
      parsed <- amf.Core.parser(Amf.name, "application/ld+json").parseFileAsync(s"${directory}${config.golden}").asFuture
      raml <- amf.Core.generator("RAML 1.0", "application/yaml").generateString(parsed, options).asFuture
      actual <- writeTemporaryFile(config.golden)(raml)
      r      <- assertDifferences(actual, s"${directory}${target}")
    } yield {
      r
    }
  }
}
