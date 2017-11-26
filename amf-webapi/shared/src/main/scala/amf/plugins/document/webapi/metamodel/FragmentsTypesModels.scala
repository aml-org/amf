package amf.plugins.document.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.document._
import amf.core.model.domain.AmfObject
import amf.plugins.document.webapi.model._
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

object FragmentsTypesModels {

  object DocumentationItemFragmentModel extends FragmentModel {

    //  val UserDocumentation = Field(UserDocumentationModel, Document + "UserDocumentation")

    override val fields: List[Field]     = FragmentModel.fields
    override val `type`: List[ValueType] = List(Document + "UserDocumentation") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = DocumentationItemFragment()
  }

  object DataTypeFragmentModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "DataType") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = DataTypeFragment()
  }

  object NamedExampleFragmentModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "NamedExample") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = NamedExampleFragment()
  }

  object ResourceTypeFragmentModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "ResourceType") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = ResourceTypeFragment()
  }

  object TraitFragmentModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "Trait") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = TraitFragment()
  }

  object AnnotationTypeDeclarationFragmentModel extends FragmentModel {

    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "AnnotationTypeDeclaration") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = AnnotationTypeDeclarationFragment()
  }

  object ExternalFragmentModel extends FragmentModel {
    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "ExternalModel") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = ExternalFragment()
  }

  object SecuritySchemeFragmentModel extends FragmentModel {
    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "SecurityScheme") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = SecuritySchemeFragment()
  }
}
