package amf.plugins.document.webapi.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.document._
import amf.framework.model.domain.AmfObject
import amf.plugins.document.webapi.model._
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

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

  object DialectNodeFragmentModel extends FragmentModel {
    override def fields: List[Field] = FragmentModel.fields

    override val `type`: List[ValueType] = List(Document + "DialectNode") ++ FragmentModel.`type`

    override def modelInstance: AmfObject = DialectFragment()
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
