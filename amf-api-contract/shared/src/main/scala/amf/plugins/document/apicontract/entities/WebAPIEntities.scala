package amf.plugins.document.apicontract.entities

import amf.core.internal.entities.Entities
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.plugins.document.apicontract.metamodel.FragmentsTypesModels._
import amf.plugins.document.apicontract.metamodel.{ExtensionModel, OverlayModel}

private[amf] object WebAPIEntities extends Entities {

  override protected val innerEntities: Seq[ModelDefaultBuilder] = Seq(
    ExtensionModel,
    OverlayModel,
    DocumentationItemFragmentModel,
    DataTypeFragmentModel,
    NamedExampleFragmentModel,
    ResourceTypeFragmentModel,
    TraitFragmentModel,
    AnnotationTypeDeclarationFragmentModel,
    SecuritySchemeFragmentModel
  )

}
