package amf.core.registries
import amf.plugins.document.webapi.{Async20Plugin, Raml10Plugin}
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class WebApiPluginsRegistryTest extends FunSuite {

  test("obtain raml plugin from registry when raml and async plugins are registered") {
    AMFPluginsRegistry.registerDocumentPlugin(Async20Plugin)
    AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)

    val plugins = AMFPluginsRegistry.documentPluginForVendor(Raml10Plugin.vendors.head)

    plugins.size shouldBe 1
    plugins.head shouldBe Raml10Plugin
  }
}
