package amf.apicontract.internal.spec.raml

import amf.apicontract.client.scala.model.document._
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.plugins.ApiRenderPlugin
import amf.apicontract.internal.spec.raml.emitter.context.{Raml08SpecEmitterContext, RamlSpecEmitterContext}
import amf.apicontract.internal.spec.raml.emitter.document.{RamlDocumentEmitter, RamlFragmentEmitter}
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document._
import amf.core.internal.plugins.render.RenderInfo
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.SpecId
import org.yaml.model.YDocument

object Raml08RenderPlugin extends ApiRenderPlugin {

  override def vendor: SpecId = SpecId.RAML08

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: AMFErrorHandler): Option[YDocument] = unit match {
    case document: Document =>
      Some(RamlDocumentEmitter(document)(specContext(renderOptions, errorHandler)).emitDocument())
    case fragment: Fragment =>
      Some(new RamlFragmentEmitter(fragment)(specContext(renderOptions, errorHandler)).emitFragment())
    case _ => None
  }

  private def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): RamlSpecEmitterContext =
    new Raml08SpecEmitterContext(errorHandler)

  override def defaultSyntax(): String = `application/yaml`

  override def mediaTypes: Seq[String] = Seq(`application/yaml`)

  override def applies(element: RenderInfo): Boolean = element.unit match {
    case _: Overlay                           => false
    case _: Extension                         => false
    case document: Document                   => document.encodes.isInstanceOf[WebApi]
    case _: Module                            => false
    case _: DocumentationItemFragment         => true // remove raml header and write as external fragment
    case _: DataTypeFragment                  => true
    case _: NamedExampleFragment              => true
    case _: ResourceTypeFragment              => true
    case _: TraitFragment                     => true
    case _: AnnotationTypeDeclarationFragment => true
    case _: SecuritySchemeFragment            => true
    case _: ExternalFragment                  => true
    case _                                    => false
  }

  override def priority: PluginPriority = NormalPriority
}
