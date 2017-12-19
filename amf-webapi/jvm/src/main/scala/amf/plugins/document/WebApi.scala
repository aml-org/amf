package amf.plugins.document

import java.util.concurrent.CompletableFuture

import amf.core.unsafe.PlatformSecrets
import amf.model.document._
import amf.model.domain.{DataNode, Shape}
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels._
import amf.plugins.document.webapi._
import amf.core.remote.FutureConverter._
import amf.validation.AMFValidationReport

import scala.concurrent.ExecutionContext.Implicits.global

object WebApi extends PlatformSecrets {

  def register(): Unit = {
    platform.registerWrapper(AnnotationTypeDeclarationFragmentModel) {
      case s: model.AnnotationTypeDeclarationFragment => AnnotationTypeDeclaration(s)
    }
    platform.registerWrapper(DataTypeFragmentModel) {
      case s: model.DataTypeFragment => DataType(s)
    }
    platform.registerWrapper(DocumentationItemFragmentModel) {
      case s: model.DocumentationItemFragment => DocumentationItem(s)
    }
    platform.registerWrapper(NamedExampleFragmentModel) {
      case s: model.NamedExampleFragment => NamedExample(s)
    }
    platform.registerWrapper(ResourceTypeFragmentModel) {
      case s: model.ResourceTypeFragment => ResourceTypeFragment(s)
    }
    platform.registerWrapper(SecuritySchemeFragmentModel) {
      case s: model.SecuritySchemeFragment => SecuritySchemeFragment(s)
    }
    platform.registerWrapper(TraitFragmentModel) {
      case s: model.TraitFragment => TraitFragment(s)
    }
    platform.registerWrapper(amf.plugins.document.webapi.metamodel.ExtensionModel) {
      case m: model.Extension => new Extension(m)
    }
    platform.registerWrapper(amf.plugins.document.webapi.metamodel.OverlayModel) {
      case m: model.Overlay => new Overlay(m)
    }

    // initialization of wrappers
    amf.plugins.domain.DataShapes.register()
    amf.plugins.domain.WebApi.register()

    // Initialization of plugins
    amf.Core.registerPlugin(OAS20Plugin)
    amf.Core.registerPlugin(RAML10Plugin)
    amf.Core.registerPlugin(RAML08Plugin)
    amf.Core.registerPlugin(PayloadPlugin)
  }

  def validatePayload(shape: Shape, payload: DataNode): CompletableFuture[AMFValidationReport] = {
    RAML10Plugin.validatePayload(shape.shape, payload.dataNode).map(new AMFValidationReport(_)).asJava
  }
}
