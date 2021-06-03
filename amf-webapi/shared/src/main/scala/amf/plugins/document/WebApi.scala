package amf.plugins.document

import amf.client.convert.WebApiRegister
import amf.client.execution.BaseExecutionEnvironment
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi._
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.APIDomainPlugin

import scala.scalajs.js.annotation.JSExportAll

object WebApi extends PlatformSecrets {

  def register(): Unit = this.register(platform.defaultExecutionEnvironment)

  def register(executionEnvironment: BaseExecutionEnvironment): Unit = {

    WebApiRegister.register(platform)

    // plugin initialization
    amf.Core.registerPlugin(DataShapesDomainPlugin)
    amf.Core.registerPlugin(APIDomainPlugin)
  }

}
