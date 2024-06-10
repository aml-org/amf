package amf.apicontract.internal.spec.avro.parser.context

import amf.core.client.scala.parse.document.ParserContext
import amf.shapes.internal.spec.common.parser.{ShapeParserContext, SpecSettings}

import scala.collection.mutable

class AvroSchemaContext(ctx: ParserContext, settings: SpecSettings)
    extends ShapeParserContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      None,
      mutable.Map.empty,
      settings
    ) {}
