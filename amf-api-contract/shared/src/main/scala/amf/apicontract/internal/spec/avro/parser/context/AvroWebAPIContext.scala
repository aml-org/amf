package amf.apicontract.internal.spec.avro.parser.context

import amf.apicontract.internal.spec.common.RamlWebApiDeclarations
import amf.apicontract.internal.spec.common.emitter.SpecVersionFactory
import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.shapes.internal.spec.common.parser.{ShapeParserContext, SpecSettings}

import scala.collection.mutable

class AvroWebAPIContext(ctx: ParserContext, settings: SpecSettings)
    extends ShapeParserContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      None,
      mutable.Map.empty,
      settings
    ) {}
