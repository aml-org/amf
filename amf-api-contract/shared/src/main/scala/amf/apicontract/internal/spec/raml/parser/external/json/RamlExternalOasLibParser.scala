package amf.apicontract.internal.spec.raml.parser.external.json

import amf.apicontract.internal.spec.oas.parser.document.Draft4JsonSchemaDeclarationsParser
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import JsonSchemaContextAdapter.toSchemaContext
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.domain.JsonParserFactory
import amf.core.internal.plugins.syntax.SYamlAMFParserErrorHandler
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.JSONSchemaId
import org.yaml.model.{IllegalTypeHandler, YMap, YNode}

import scala.collection.mutable

case class RamlExternalOasLibParser(ctx: RamlWebApiContext, text: String, valueAST: YNode, path: String) {

  private implicit val errorHandler: IllegalTypeHandler = new SYamlAMFParserErrorHandler(ctx.eh)

  def parse(): Unit = {
    val schemaEntry       = JsonParserFactory.fromCharsWithSource(text, valueAST.sourceName)(ctx.eh).document()
    val jsonSchemaContext = toSchemaContext(ctx, valueAST)
    jsonSchemaContext.setJsonSchemaAST(schemaEntry.node)

    val shapes = Draft4JsonSchemaDeclarationsParser.parseTypeDeclarations(schemaEntry.node.as[YMap])(jsonSchemaContext)
    registerShapesAsExternalLibrary(path, shapes)
  }

  private def registerShapesAsExternalLibrary(path: String, resolvedShapes: Seq[Shape]) = {
    val shapesMap = mutable.Map[String, AnyShape]()
    resolvedShapes.map(s => (s, s.annotations.find(classOf[JSONSchemaId]))).foreach {
      case (s: AnyShape, Some(a)) if a.id.equals(s.name.value()) =>
        shapesMap += s.name.value -> s
      case (s: AnyShape, Some(a)) =>
        shapesMap += s.name.value() -> s
        shapesMap += a.id           -> s
      case (s: AnyShape, None) => shapesMap += s.name.value -> s
    }

    ctx.declarations.registerExternalLib(path, shapesMap.toMap)
  }
}
