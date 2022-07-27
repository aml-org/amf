package amf.configuration

import amf.apicontract.client.scala.{AMFConfiguration, ConfigurationAdapter}
import amf.apicontract.client.scala.APIConfiguration.{API, APIWithJsonSchema}
import amf.apicontract.client.scala.AsyncAPIConfiguration.{Async20 => Async20Config}
import amf.apicontract.client.scala.OASConfiguration.{OAS => OASConfig, OAS20 => OAS20Config, OAS30 => OAS30Config}
import amf.apicontract.client.scala.RAMLConfiguration.{
  RAML => RAMLConfig,
  RAML08 => RAML08Config,
  RAML10 => RAML10Config
}
import amf.apicontract.client.scala.WebAPIConfiguration.WebAPI
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec._
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

trait ConfigurationSetupTest extends AsyncFunSuite with Matchers {

  protected val basePath                            = "file://amf-cli/shared/src/test/resources/configuration/"
  protected val apiConfig: AMFConfiguration         = API()
  protected val apiWithJsonSchema: AMFConfiguration = APIWithJsonSchema()
  protected val webApiConfig: AMFConfiguration      = WebAPI()
  protected val ramlConfig: AMFConfiguration        = RAMLConfig()
  protected val raml10Config: AMFConfiguration      = RAML10Config()
  protected val raml08Config: AMFConfiguration      = RAML08Config()
  protected val oas20Config: AMFConfiguration       = OAS20Config()
  protected val oas30Config: AMFConfiguration       = OAS30Config()
  protected val oasConfig: AMFConfiguration         = OASConfig()
  protected val async20Config: AMFConfiguration     = Async20Config()
  protected val jsonSchema: AMFConfiguration        = ConfigurationAdapter.adapt(JsonSchemaConfiguration.JsonSchema())

  protected val configs: List[AMFConfiguration] = List(
    apiConfig,
    webApiConfig,
    ramlConfig,
    raml10Config,
    raml08Config,
    oas20Config,
    oas30Config,
    oasConfig,
    async20Config,
    apiWithJsonSchema,
    jsonSchema
  )

  protected val vendors: List[Spec] = List(ASYNC20, RAML10, RAML08, OAS20, OAS30)
  protected val configNames = Map(
    apiConfig         -> "API",
    webApiConfig      -> "WebAPI",
    ramlConfig        -> "RAML",
    raml10Config      -> "RAML 1.0",
    raml08Config      -> "RAML 0.8",
    oas20Config       -> "OAS 2.0",
    oas30Config       -> "OAS 3.0",
    oasConfig         -> "OAS",
    async20Config     -> "ASYNC 2.0",
    jsonSchema        -> "JsonSchema",
    apiWithJsonSchema -> "ApiWithJsonSchema"
  )
}
