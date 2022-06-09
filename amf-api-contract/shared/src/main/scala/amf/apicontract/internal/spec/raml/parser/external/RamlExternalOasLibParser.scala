package amf.apicontract.internal.spec.raml.parser.external

import amf.apicontract.internal.spec.oas.parser.document
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.external.SharedStuff.toSchemaContext
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.document.{InferredLinkReference, SyamlParsedDocument}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.JsonParserFactory
import amf.core.internal.plugins.syntax.SYamlAMFParserErrorHandler
import amf.core.internal.remote.Mimes.`application/json`
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.JSONSchemaId
import org.yaml.model.{IllegalTypeHandler, YMap, YNode}
import amf.core.internal.utils.AmfStrings

import scala.collection.mutable

case class RamlExternalOasLibParser(ctx: RamlWebApiContext, text: String, valueAST: YNode, path: String) {

  private implicit val errorHandler: IllegalTypeHandler = new SYamlAMFParserErrorHandler(ctx.eh)

  def parse(): Unit = {
    // todo: should we add string begin position to each node position? in order to have the positions relatives to root api intead of absolut to text
    // todo: this should be migrated to JsonSchemaParser
    val url = path.normalizeUrl + (if (!path.endsWith("/")) "/"
                                   else "") // alwarys add / to avoid ask if there is any one before add #
    val schemaEntry       = JsonParserFactory.fromCharsWithSource(text, valueAST.sourceName)(ctx.eh).document()
    val jsonSchemaContext = toSchemaContext(ctx, valueAST)
    jsonSchemaContext.setJsonSchemaAST(schemaEntry.node)

    document
      .Oas2DocumentParser(
        Root(SyamlParsedDocument(schemaEntry), url, `application/json`, Nil, InferredLinkReference, text)
      )(jsonSchemaContext)
      .parseTypeDeclarations(schemaEntry.node.as[YMap], url + "#/definitions/", None)(jsonSchemaContext)
    val resolvedShapes = jsonSchemaContext.declarations.shapes.values.toSeq
    registerShapesAsExternalLibrary(path, resolvedShapes)
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
