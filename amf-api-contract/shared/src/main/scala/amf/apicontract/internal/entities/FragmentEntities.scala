package amf.apicontract.internal.entities

import amf.apicontract.internal.metamodel.document.FragmentsTypesModels._
import amf.apicontract.internal.metamodel.document.{ExtensionModel, OverlayModel}
import amf.core.internal.entities.Entities
import amf.core.internal.metamodel.ModelDefaultBuilder

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
    SecuritySchemeFragmentModel
  )

}
