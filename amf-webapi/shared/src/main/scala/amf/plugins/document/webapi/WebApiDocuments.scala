package amf.plugins.document.webapi

import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels._
import amf.plugins.document.webapi.metamodel.{ExtensionModel, OverlayModel}
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.webapi.annotations.ParentEndPoint

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
    ExternalFragmentModel,
    DialectNodeFragmentModel
  )

  def webApiAnnotations = Map(
    "type-exprssion" -> ParsedFromTypeExpression,
    "parent-end-point" -> ParentEndPoint,
    "parsed-json-schema" -> ParsedJSONSchema,
    "source-vendor" -> SourceVendor,
    "declared-element" -> DeclaredElement,
    "synthesized-field" -> SynthesizedField,
    "single-value-array" -> SingleValueArray,
    "aliases-array" -> Aliases
  )
}
