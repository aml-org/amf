package amf.plugins.document

import amf.client.convert.WebApiRegister
import amf.client.execution.BaseExecutionEnvironment
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi._
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object WebApi extends PlatformSecrets {

  def register(): Unit = this.register(platform.defaultExecutionEnvironment)

  def register(executionEnvironment: BaseExecutionEnvironment): Unit = {

    WebApiRegister.register(platform)

    // plugin initialization
    amf.Core.registerPlugin(DataShapesDomainPlugin)
    amf.Core.registerPlugin(WebAPIDomainPlugin)

    // Initialization of plugins
    amf.Core.registerPlugin(Oas20Plugin)
    amf.Core.registerPlugin(Oas30Plugin)
    amf.Core.registerPlugin(Async20Plugin)
    amf.Core.registerPlugin(Raml10Plugin)
    amf.Core.registerPlugin(Raml08Plugin)
    amf.Core.registerPlugin(PayloadPlugin)
    amf.Core.registerPlugin(JsonSchemaPlugin)
  }

}
