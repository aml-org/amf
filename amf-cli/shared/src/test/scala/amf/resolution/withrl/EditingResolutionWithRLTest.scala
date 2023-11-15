package amf.resolution.withrl

import amf.core.client.common.transform._
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote._
import amf.resolution.ResolutionTest
import amf.testing.ConfigProvider.configFor

import scala.concurrent.{ExecutionContext, Future}

/** This test class tests a URI mismanagement case that is only reproducible with certain Resource Loaders (like the one
  * in AMFS).
  *
  * According to the URI spec (https://www.ietf.org/rfc/rfc2396.txt), a URI is defined like this (summarized):
  * <scheme>://<authority>/<path_segments> <scheme>:/<path_segments> where authority is either a server or a registry
  * (plain name)
  *
  * Our misuse is that we use <scheme>://<path_segments>. On other words we use the double slash '//' but we do not
  * provide an authority. This causes some bad URI resolution like this: 'file://libraries/../libraries/tr-lib.raml'
  * instead of 'file://libraries/tr-lib.raml' because 'libraries' gets interpreted as the authority rather than the
  * first path segment (and thus is not omitted by the subsequent '..').
  *
  * We are generally immune against this bug except during RT/Trait application, where RT/Traits are indexed by their
  * declaring BaseUnit location and this bad resolution causes some RT/Traits to be not found (and therefore not
  * applied).
  */
class EditingResolutionWithRLTest extends ResolutionTest {
  override val defaultPipeline: String                     = PipelineId.Editing
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val extendsPath                                          = "amf-cli/shared/src/test/resources/resolution/extends"
  override val basePath: String                            = ""

  override def build(sourcePath: String, goldenPath: String, amfConfig: AMFGraphConfiguration): Future[BaseUnit] = {
    amfConfig
      .withParsingOptions(amfConfig.options.parsingOptions.withBaseUnitUrl(goldenPath))
      .baseUnitClient()
      .parse(s"$sourcePath")
      .map(_.baseUnit)
  }

  test("Test API with complex RT Trait file structure") {
    val directory      = s"$extendsPath/api-with-complex-rt-trait-file-structure"
    val resourceLoader = TestMapResourceLoader(directory)(fs)
    val amfConfig =
      configFor(Raml10).withRenderOptions(renderOptions().withPrettyPrint).withResourceLoaders(List(resourceLoader))

    cycle(
      "file://api.raml",
      s"$directory/api.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      directory = "",
      amfConfig = Some(amfConfig),
      transformWith = Some(Raml10)
    )
  }
}
