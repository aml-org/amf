package amf.shapes.internal.plugins.parser

object AMFJsonLDSchemaGraphParsePlugin extends BaseJsonLDSchemaGraphParsePlugin {

  override def mediaTypes: Seq[String] = Seq("application/schemald+json")
}
