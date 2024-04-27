package amf.apicontract.internal.spec.avro.parser.document

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import amf.apicontract.internal.spec.avro.parser.domain.{AvroMessageParser, AvroMessagesParser, AvroShapeParser}
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.metamodel.document.DocumentModel
import amf.core.internal.parser.Root
import org.yaml.model.{YMap, YMapEntry, YNode, YSequence, YType}
import amf.apicontract.internal.spec.common.parser._
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidTypesType
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.{Root, YMapOps, YScalarYRead}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps

class AvroDocumentParser(root: Root)(implicit ctx: AvroWebAPIContext) extends QuickFieldParserOps {

  def parseDocument(): Document = {
    val doc = Document()
    doc.withLocation(ctx.loc)
    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    parseDeclarations(root, map)
    doc.withDeclares(ctx.declarations.shapes.values.toSeq) // TODO: review
    ctx.futureDeclarations.resolve()

    val api = parseWebApi(map)
    doc.withEncodes(api)
    map.key("namespace", (DocumentModel.Package in doc).allowingAnnotations)
    doc
  }

  def parseDeclarations(root: Root, map: YMap): Unit = {
    map.key("types", parseTypes)
  }

  def parseTypes(e: YMapEntry) = {
    e.value.tagType match {
      case YType.Seq => e.value.as[Seq[YNode]].seq.map(_.as[YMap]).foreach(parseType)
      case other => ctx.eh.violation(InvalidTypesType, "", s"Invalid type $other for 'types' node.", e.value.location)
    }
  }

  def parseType(map: YMap): Unit = new AvroShapeParser(map).parse().foreach(addDeclared)

  def addDeclared(anyShape: AnyShape) = {
    ctx.declarations += anyShape.add(DeclaredElement())
  }

  def parseWebApi(map: YMap): WebApi = {
    val api = WebApi()

    map.key("protocol", (WebApiModel.Name in api).allowingAnnotations)
    map.key("doc", (WebApiModel.Description in api).allowingAnnotations)

    // parse messages
    map.key(
      "messages",
      e => {
        api.withEndPoints(parseMessages(e))
      }
    )
    api
  }

  def parseMessages(e: YMapEntry) = new AvroMessagesParser(e.value.as[YMap]).parse()
}
