package amf.cli.internal.`export`

object MarkdownExporter {

  def exportToMarkdown(title: String, models: List[ExportableModel]): String = {
    val builder     = new MarkdownBuilder().addText(title).addLine()
    val tempBuilder = addIndex(builder, models)
    models
      .foldLeft(tempBuilder) { (builder, model) =>
        exportModel(model, builder)
      }
      .build
  }

  private def addIndex(builder: MarkdownBuilder, models: List[ExportableModel]): MarkdownBuilder = {
    val tempBuilder = builder.addHeader(2, "Table of Contents")
    models.map(x => x.name).foldLeft(tempBuilder) { (accBuilder: MarkdownBuilder, modelName: String) =>
      accBuilder.addBullet(createLink(modelName, modelName))
    }
  }

  private def exportModel(model: ExportableModel, builder: MarkdownBuilder): MarkdownBuilder = {
    val tempBuilder = builder
      .addHeader(2, model.name)
      .addText(model.doc)
      .startTable(List("Name", "Value", "Documentation", "Namespace"))
    model.fields
      .foldLeft(tempBuilder) { (builder, field) =>
        builder.addRow(List(field.name, formatFieldValue(field), field.doc, field.namespace))
      }
      .addText("")
  }

  private def createLink(linkName: String, linkTo: String) = s"[$linkName](#${formatToAnchor(linkTo)})"

  private def formatFieldValue(field: ExportableField): String = {
    var value = field.value
    if (field.linkToValue) value = s"[$value](#${formatToAnchor(value)})"
    if (field.isArray) value = s"[$value]"
    value
  }

  private def formatToAnchor(value: String): String = value.replace(" ", "-").toLowerCase
}
