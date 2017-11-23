package amf.framework.metamodel.document

import amf.framework.metamodel.Field
import amf.framework.model.document.Fragment
import amf.metadata.domain.DomainElementModel
import amf.model.AmfObject
import amf.plugins.document.webapi.parser.FragmentTypes.NamedExampleFragment
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Fragment meta model.
  */
trait FragmentModel extends BaseUnitModel {

  val Encodes = Field(DomainElementModel, Document + "encodes")

  override def modelInstance: AmfObject = throw new Exception("Fragment is an abstract cannot create model instance")

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

    override def modelInstance: AmfObject = Fragment.DocumentationItem()
  }

  object DataTypeModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "DataType") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = Fragment.DataType()
  }

  object NamedExampleModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "NamedExample") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = Fragment.NamedExample()
  }

  object ResourceTypeFragmentModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "ResourceType") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = Fragment.ResourceTypeFragment()
  }

  object TraitFragmentModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "Trait") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = Fragment.TraitFragment()
  }

  object AnnotationTypeDeclarationModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "AnnotationTypeDeclaration") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = Fragment.AnnotationTypeDeclaration()
  }

  object DialectNodeModel extends FragmentModel {
    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "DialectNode") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = Fragment.DialectFragment()
  }

  object ExternalFragmentModel extends FragmentModel {
    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "ExternalModel") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = Fragment.ExternalFragment()
  }

  object SecuritySchemeFragmentModel extends FragmentModel {
    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "SecurityScheme") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = Fragment.SecurityScheme()
  }
}
