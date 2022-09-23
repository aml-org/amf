package amf.configuration

import amf.apicontract.client.scala.{APIConfiguration, ConfigurationAdapter}
import amf.core.internal.plugins.AMFPlugin
import amf.core.internal.registries.PluginsRegistry
import amf.shapes.client.scala.ShapesConfiguration
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ConfigurationAdapterTest extends AnyFunSuite with Matchers {

  test("Adapter should respect root and reference parse plugins") {
    val config  = APIConfiguration.APIWithJsonSchema()
    val adapted = ConfigurationAdapter.adapt(config)
    plugins(config, _.rootParsePlugins) should not be empty
    plugins(config, _.referenceParsePlugins) should not be empty
    plugins(adapted, _.rootParsePlugins) should contain theSameElementsAs plugins(config, _.rootParsePlugins)
    plugins(adapted, _.referenceParsePlugins) should contain theSameElementsAs plugins(config, _.referenceParsePlugins)
  }

  private def plugins(config: ShapesConfiguration, specific: PluginsRegistry => List[AMFPlugin[_]]) = {
    specific(config.getRegistry.getPluginsRegistry)
  }
}
