package amf.framework.metamodel.document

import amf.framework.metamodel.Field
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Fragment meta model.
  */
trait FragmentModel extends BaseUnitModel {

  val Encodes = Field(DomainElementModel, Document + "encodes")

}

object FragmentModel extends FragmentModel {

  override val `type`: List[ValueType] = List(Document + "Fragment") ++ BaseUnitModel.`type`

  override def fields: List[Field] = Encodes :: BaseUnitModel.fields
}
object FragmentsTypesModels {

  object DocumentationItemModel extends FragmentModel {

    //  val UserDocumentation = Field(UserDocumentationModel, Document + "UserDocumentation")

    override val fields: List[Field]     = FragmentModel.fields
    override val `type`: List[ValueType] = List(Document + "UserDocumentation") ++ FragmentModel.`type`
  }

  object DataTypeModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "DataType") ++ FragmentModel.`type`
  }

  object NamedExampleModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "NamedExample") ++ FragmentModel.`type`
  }

  object ResourceTypeModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "ResourceType") ++ FragmentModel.`type`
  }

  object TraitModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "Trait") ++ FragmentModel.`type`
  }

  object AnnotationTypeDeclarationModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "AnnotationTypeDeclaration") ++ FragmentModel.`type`
  }

  object DialectNodeModel extends FragmentModel {
    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "DialectNode") ++ FragmentModel.`type`
  }

  object ExternalFragmentModel extends FragmentModel {
    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "ExternalModel") ++ FragmentModel.`type`
  }

  object SecuritySchemeModel extends FragmentModel {
    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "SecurityScheme") ++ FragmentModel.`type`
  }
}
