package amf.metadata.domain
import amf.metadata.Field
import amf.metadata.Type.Str
import amf.vocabulary.Namespace.{Document, Schema}
import amf.vocabulary.{Namespace, ValueType}

/**
  * Created by hernan.najles on 9/25/17.
  */
object UserDocumentationModel extends DomainElementModel {

  val Title   = Field(Str, Schema + "title")
  val Content = Field(Str, Document + "content")

  override val fields: List[Field]     = List(Title, Content) ++ DomainElementModel.fields
  override val `type`: List[ValueType] = Schema + "UserDocumentation" :: DomainElementModel.`type`
}
