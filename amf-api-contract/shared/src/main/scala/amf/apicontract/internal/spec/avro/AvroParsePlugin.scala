package amf.apicontract.internal.spec.avro

import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.apicontract.internal.spec.avro.parser.document.AvroDocumentParser
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Mimes, Spec}

object AvroParsePlugin extends ApiParsePlugin {

  override def spec: Spec = Spec.AVRO_SCHEMA

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    new AvroDocumentParser(document)(new AvroSchemaContext(ctx, AvroSettings)).parseDocument()
  }

  /** media types which specifies vendors that are parsed by this plugin. */
  override def mediaTypes: Seq[String] = Seq(Mimes.`application/json`)

  override def applies(element: Root): Boolean = element.mediatype == Mimes.`application/json`
}
