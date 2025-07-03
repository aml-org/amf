package amf.testing

import amf.apicontract.client.scala.{AMFBaseUnitClient, AMFConfiguration, APIConfiguration, AsyncAPIConfiguration, AvroConfiguration, OASConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.graphql.client.scala.GraphQLConfiguration
import amf.grpc.client.scala.GRPCConfiguration
import amf.testing.ConfigProvider.configFor
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

/** Tests for a specific field/value/node in an API. Uses [[amf.testing.BaseUnitUtils]] to quickly get nodes.
  */
trait AMFModelTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {
  lazy val ro: RenderOptions                      = RenderOptions().withCompactUris.withPrettyPrint.withSourceMaps
  lazy val graphqlConfig: AMFConfiguration        = GraphQLConfiguration.GraphQL().withRenderOptions(ro)
  lazy val ramlConfig: AMFConfiguration           = RAMLConfiguration.RAML10().withRenderOptions(ro)
  lazy val ramlClient: AMFBaseUnitClient          = ramlConfig.baseUnitClient()
  lazy val raml08Config: AMFConfiguration         = RAMLConfiguration.RAML08().withRenderOptions(ro)
  lazy val raml08Client: AMFBaseUnitClient        = raml08Config.baseUnitClient()
  lazy val oasConfig: AMFConfiguration            = OASConfiguration.OAS30().withRenderOptions(ro)
  lazy val oasClient: AMFBaseUnitClient           = oasConfig.baseUnitClient()
  lazy val oas31Config: AMFConfiguration          = OASConfiguration.OAS31().withRenderOptions(ro)
  lazy val oas31Client: AMFBaseUnitClient         = oas31Config.baseUnitClient()
  lazy val oas2Config: AMFConfiguration           = OASConfiguration.OAS20().withRenderOptions(ro)
  lazy val oas2Client: AMFBaseUnitClient          = oas2Config.baseUnitClient()
  lazy val oasComponentsConfig: AMFConfiguration  = OASConfiguration.OAS30Component().withRenderOptions(ro)
  lazy val oasComponentsClient: AMFBaseUnitClient = oasComponentsConfig.baseUnitClient()
  lazy val asyncConfig: AMFConfiguration          = AsyncAPIConfiguration.Async20().withRenderOptions(ro)
  lazy val asyncClient: AMFBaseUnitClient         = asyncConfig.baseUnitClient()
  lazy val avroConfig: AMFConfiguration           = AvroConfiguration.Avro().withRenderOptions(ro)
  lazy val avroClient: AMFBaseUnitClient          = avroConfig.baseUnitClient()
  lazy val grpcConfig: AMFConfiguration           = GRPCConfiguration.GRPC().withRenderOptions(ro)
  lazy val grpcClient: AMFBaseUnitClient          = grpcConfig.baseUnitClient()

  def modelAssertion(
      path: String,
      pipelineId: String = PipelineId.Default,
      transform: Boolean = true
  )(assertion: BaseUnit => Assertion): Future[Assertion] = {
    val client = APIConfiguration.APIWithJsonSchema().baseUnitClient()
    client.parse(path) flatMap { parseResult =>
      if (!transform) assertion(parseResult.baseUnit)
      else {
        val specificClient  = configFor(parseResult.sourceSpec).baseUnitClient()
        val transformResult = specificClient.transform(parseResult.baseUnit, pipelineId)
        assertion(transformResult.baseUnit)
      }
    }
  }
}
