package amf.cli.internal.`export`

object ExportableModelAdapter {

  def adapt(models: List[Model]): List[ExportableModel] = models.map(adapt).sortBy(m => m.name)

  private def adapt(model: Model): ExportableModel = {
    val fields = model.attributes.map { case (name, attribute) =>
      adapt(name, attribute)
    }
    ExportableModel(formatModelName(model.name), fields, model.obj.doc.description, types(model))
  }

  private def types(model: Model) = model.obj.`type`.map(_.iri())

  private def adapt(name: String, attribute: Attribute): ExportableField = {
    attribute match {
      case att: TraversableAttribute =>
        ExportableField(
          name,
          formatModelName(att.name),
          isArray = false,
          linkToValue = true,
          doc = att.docDescription,
          att.namespace
        )
      case att: ArrayAttribute if att.isTraversable =>
        ExportableField(
          name,
          formatModelName(att.toString),
          isArray = true,
          linkToValue = true,
          att.docDescription,
          att.namespace
        )
      case att: ArrayAttribute if !att.isTraversable =>
        ExportableField(name, att.toString, isArray = true, linkToValue = false, att.docDescription, att.namespace)
      case att: AttributeType =>
        ExportableField(name, att.name, isArray = false, linkToValue = false, att.docDescription, att.namespace)
    }
  }

  private def formatModelName(name: String): String = name.replace("Model", "")
}

case class ExportableModel(name: String, fields: List[ExportableField], doc: String, types: List[String])
case class ExportableField(
    name: String,
    value: String,
    isArray: Boolean,
    linkToValue: Boolean,
    doc: String,
    namespace: String
)
