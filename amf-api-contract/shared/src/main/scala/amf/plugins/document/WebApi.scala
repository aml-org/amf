package amf.plugins.document

import amf.client.convert.ApiRegister
import amf.client.execution.BaseExecutionEnvironment
import amf.core.AMF
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
    AMF.registerPlugin(DataShapesDomainPlugin)
    AMF.registerPlugin(APIDomainPlugin)
  }

}
