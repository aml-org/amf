package amf.apicontract.internal.entities

import amf.apicontract.internal.metamodel.document.FragmentsTypesModels._
import amf.apicontract.internal.metamodel.document.{
  APIContractProcessingDataModel,
  ExtensionModel,
  JsonSchemaDocumentModel,
  OverlayModel
}
import amf.core.internal.entities.Entities
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.shapes.internal.document.metamodel.DataTypeFragmentModel

private[amf] object FragmentEntities extends Entities {

  override protected val innerEntities: Seq[ModelDefaultBuilder] = Seq(
    ExtensionModel,
    OverlayModel,
    DocumentationItemFragmentModel,
    DataTypeFragmentModel,
    NamedExampleFragmentModel,
    ResourceTypeFragmentModel,
    TraitFragmentModel,
    AnnotationTypeDeclarationFragmentModel,
    SecuritySchemeFragmentModel,
    APIContractProcessingDataModel,
    JsonSchemaDocumentModel
  )

}
