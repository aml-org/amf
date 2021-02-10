package amf.client

import amf.client.`new`.AmfRegistry.{AllEntities, AllPipelines, FullPluginRegistr}
import amf.client.`new`._

object ApiContractEnvironment {

  private def fullApiRegistry() = {
    new AmfRegistry(FullPluginRegistr, AllEntities, AllPipelines)
  }

  def apply(): AmfEnvironment = {
    AmlEnvironment.withDialect()
    val default = AmfConfig.default
    new AmfEnvironment(
      new AmfResolvers(default.platform.loaders()(default.executionContext.context), None),
      DefaultErrorHandlerProvider,
      fullApiRegistry(),
      default,
      AmfOptions.default
    )
  }

  // move to raml new module
  def raml() = {}

  // move to oas new module
  def oas() = {}

  // raml & oas
  def api() = {}

  def async() = {}
}
