package amf.apicontract.internal.metamodel.domain

/**
  * Organization metamodel
  */
object OrganizationModel extends DomainElementModel with NameFieldSchema {

  val Url = Field(Iri, Core + "url", ModelDoc(ModelVocabularies.Core, "url", "URL identifying the organization"))

  val Email =
    Field(Str, Core + "email", ModelDoc(ModelVocabularies.Core, "email", "Contact email for the organization"))

  override val `type`: List[ValueType] = Core + "Organization" :: DomainElementModel.`type`

  override def fields: List[Field] = List(Url, Name, Email) ++ DomainElementModel.fields

  override def modelInstance = Organization()

  override val doc: ModelDoc =
    ModelDoc(ModelVocabularies.Core, "Organization", "Organization providing an good or service")
}
