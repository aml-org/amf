package amf.plugins.document.webapi

import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels._
import amf.plugins.document.webapi.metamodel.{ExtensionModel, OverlayModel}

trait WebApiDocuments {

  def webApiDocuments = Seq(
    ExtensionModel,
    OverlayModel,
    DocumentationItemFragmentModel,
    DataTypeFragmentModel,
    NamedExampleFragmentModel,
    ResourceTypeFragmentModel,
    TraitFragmentModel,
    AnnotationTypeDeclarationFragmentModel,
    SecuritySchemeFragmentModel,
    ExternalFragmentModel
  )

  def webApiAnnotations = Map(
    "parsed-json-schema" -> ParsedJSONSchema,
    "declared-element"   -> DeclaredElement
  )
}
