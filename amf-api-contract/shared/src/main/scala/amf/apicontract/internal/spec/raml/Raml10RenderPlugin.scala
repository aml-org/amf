package amf.apicontract.internal.spec.raml

import amf.apicontract.client.scala.model.document._
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.plugins.ApiRenderPlugin
import amf.apicontract.internal.spec.SyntaxMediaTypes.{`APPLICATION/JSON`, `APPLICATION/YAML`}
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
import amf.core.internal.plugins.render.AMFRenderPlugin.APPLICATION_YAML
import amf.core.internal.plugins.render.RenderInfo
import amf.core.internal.remote.Vendor
import org.yaml.model.{YDocument, YNode}

object Raml10RenderPlugin extends ApiRenderPlugin {

  override def vendor: Vendor = Vendor.RAML10

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: AMFErrorHandler): Option[YDocument] = unit match {
    case module: Module => Some(RamlModuleEmitter(module)(specContext(renderOptions, errorHandler)).emitModule())
    case document: Document =>
      Some(RamlDocumentEmitter(document)(specContext(renderOptions, errorHandler)).emitDocument())
    case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
    case fragment: Fragment =>
      Some(new RamlFragmentEmitter(fragment)(specContext(renderOptions, errorHandler)).emitFragment())
    case _ => None
  }

  private def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): RamlSpecEmitterContext =
    new Raml10SpecEmitterContext(errorHandler)

  override def defaultSyntax(): String = APPLICATION_YAML

  override def mediaTypes: Seq[String] = Seq(`APPLICATION/YAML`)

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
