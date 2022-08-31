package amf.apicontract.internal.spec.oas

import amf.apicontract.client.scala.model.document.ComponentModule
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.spec.oas.emitter.context.Oas3SpecEmitterContext
import amf.apicontract.internal.spec.oas.emitter.document.Oas3DocumentEmitter
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import org.yaml.model.YDocument

object Oas30ComponentRenderPlugin extends OasRenderPlugin {

  override def spec: Spec = Spec.OAS30

  override def mediaTypes: Seq[String] = Seq(`application/json`, `application/yaml`)

  override def defaultSyntax(): String = `application/json`

  override def priority: PluginPriority = NormalPriority

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler
  ): Option[YDocument] =
    unit match {
      case module: ComponentModule =>
        Some(Oas3DocumentEmitter(wrappedDocument(module))(specContext(renderConfig, errorHandler)).emitDocument())
      case _ => None
    }

  private def specContext(renderConfig: RenderConfiguration, errorHandler: AMFErrorHandler): Oas3SpecEmitterContext =
    new Oas3SpecEmitterContext(errorHandler, config = renderConfig)

  private def wrappedDocument(module: ComponentModule): Document = {
    // This is to emit a valid OAS 3.x API, adding a dummy WebApi with empty paths and default required fields
    Document(module.annotations).withDeclares(module.declares).withEncodes(dummyWebApi(module))
  }

  private def dummyWebApi(module: ComponentModule): WebApi = {
    val dummyWebApi = WebApi()
    dummyWebApi.withEndPoints(Nil).withVersion(module.version.value()).withName(module.name.value())
    dummyWebApi
  }

}
