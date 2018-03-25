package amf.plugins.document

import amf.client.convert.WebApiRegister
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi._
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object WebApi extends PlatformSecrets {

  def register(): Unit = {

    WebApiRegister.register(platform)

    // plugin initialization
    amf.Core.registerPlugin(DataShapesDomainPlugin)
    amf.Core.registerPlugin(WebAPIDomainPlugin)

    // Initialization of plugins
    amf.Core.registerPlugin(OAS20Plugin)
    amf.Core.registerPlugin(OAS30Plugin)
    amf.Core.registerPlugin(RAML10Plugin)
    amf.Core.registerPlugin(RAML08Plugin)
    amf.Core.registerPlugin(PayloadPlugin)
    amf.Core.registerPlugin(JsonSchemaPlugin)
  }

}
