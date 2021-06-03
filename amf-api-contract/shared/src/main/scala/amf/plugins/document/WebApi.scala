package amf.plugins.document

import amf.client.convert.ApiRegister
import amf.client.execution.BaseExecutionEnvironment
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.apicontract._
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.apicontract.APIDomainPlugin

import scala.scalajs.js.annotation.JSExportAll

object WebApi extends PlatformSecrets {

  def register(): Unit = this.register(platform.defaultExecutionEnvironment)

  def register(executionEnvironment: BaseExecutionEnvironment): Unit = {

    ApiRegister.register(platform)

    // plugin initialization
    amf.Core.registerPlugin(DataShapesDomainPlugin)
    amf.Core.registerPlugin(APIDomainPlugin)

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
