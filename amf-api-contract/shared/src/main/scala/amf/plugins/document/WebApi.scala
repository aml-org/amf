package amf.plugins.document

import amf.client.convert.ApiRegister
import amf.core.client.platform.execution.BaseExecutionEnvironment
import amf.core.internal.unsafe.PlatformSecrets

object WebApi extends PlatformSecrets {

  def register(): Unit = this.register(platform.defaultExecutionEnvironment)

  def register(executionEnvironment: BaseExecutionEnvironment): Unit = {

    ApiRegister.register(platform)

    // TODO: ARM erase. Commented because of amf-core package change. Temporary
//    AMF.registerPlugin(DataShapesDomainPlugin)
//    AMF.registerPlugin(APIDomainPlugin)
  }

}
