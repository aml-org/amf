package amf.plugins.document

import amf.client.convert.WebApiRegister
import amf.core.unsafe.PlatformSecrets
import amf.client.model.document._
import amf.client.model.domain.{DataNode, Shape}
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels._
import amf.plugins.document.webapi._
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import amf.validation.AMFValidationReport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js

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

  def validatePayload(shape: Shape, payload: DataNode): js.Promise[AMFValidationReport] = {
    RAML10Plugin.validatePayload(shape._internal, payload._internal).map(new AMFValidationReport(_)).toJSPromise
  }
}
