package amf.apicontract.internal.spec.raml

import amf.apicontract.client.scala.model.document._
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.plugins.ApiRenderPlugin
import amf.apicontract.internal.spec.raml.emitter.context.{Raml10SpecEmitterContext, RamlSpecEmitterContext}
import amf.apicontract.internal.spec.raml.emitter.document.{
  RamlDocumentEmitter,
  RamlFragmentEmitter,
  RamlModuleEmitter
}
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document._
import amf.core.internal.plugins.render.{RenderConfiguration, RenderInfo}
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import org.yaml.model.{YDocument, YNode}

object Raml10RenderPlugin extends ApiRenderPlugin {

  override def spec: Spec = Spec.RAML10

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderConfig: RenderConfiguration,
                                            errorHandler: AMFErrorHandler): Option[YDocument] = unit match {
    case module: Module => Some(RamlModuleEmitter(module)(specContext(renderConfig, errorHandler)).emitModule())
    case document: Document =>
      Some(RamlDocumentEmitter(document)(specContext(renderConfig, errorHandler)).emitDocument())
    case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
    case fragment: Fragment =>
      Some(new RamlFragmentEmitter(fragment)(specContext(renderConfig, errorHandler)).emitFragment())
    case _ => None
  }

  private def specContext(config: RenderConfiguration, errorHandler: AMFErrorHandler): RamlSpecEmitterContext =
    new Raml10SpecEmitterContext(errorHandler, config = config)

  override def defaultSyntax(): String = `application/yaml`

  override def mediaTypes: Seq[String] = Seq(`application/yaml`)

  override def applies(element: RenderInfo): Boolean = element.unit match {
    case _: Overlay                           => true
    case _: Extension                         => true
    case document: Document                   => document.encodes.isInstanceOf[Api]
    case _: Module                            => true
    case _: DocumentationItemFragment         => true
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
