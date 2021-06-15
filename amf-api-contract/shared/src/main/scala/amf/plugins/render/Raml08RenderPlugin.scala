package amf.plugins.render

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, ExternalFragment, Fragment, Module}
import amf.core.internal.plugins.render.AMFRenderPlugin.APPLICATION_YAML
import amf.core.internal.plugins.render.RenderInfo
import amf.core.internal.remote.Vendor
import amf.plugins.common.Raml08MediaTypes
import amf.shapes.internal.spec.contexts.emitter.raml.{Raml08SpecEmitterContext, RamlSpecEmitterContext}
import amf.plugins.document.apicontract.model._
import amf.plugins.document.apicontract.parser.spec.raml.{RamlDocumentEmitter, RamlFragmentEmitter}
import amf.plugins.domain.apicontract.models.api.WebApi
import org.yaml.model.YDocument

object Raml08RenderPlugin extends ApiRenderPlugin {

  override def vendor: Vendor = Vendor.RAML08

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

  override def defaultSyntax(): String = APPLICATION_YAML

  override def mediaTypes: Seq[String] = Raml08MediaTypes.mediaTypes

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
