package amf.apicontract.internal.spec.avro.parser.document

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.apicontract.internal.spec.avro.parser.domain.AvroShapeParser
import amf.core.client.scala.model.document.BaseUnitProcessingData
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.metamodel.document.{DocumentModel, FragmentModel}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Spec
import amf.shapes.client.scala.model.document.AvroSchemaDocument
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps
import org.yaml.model.YMap

class AvroDocumentParser(root: Root)(implicit ctx: AvroSchemaContext) extends QuickFieldParserOps {

  def parseDocument(): AvroSchemaDocument = {
    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    val doc = AvroSchemaDocument(Annotations(map))
      .withLocation(ctx.loc)
      .withProcessingData(BaseUnitProcessingData().withSourceSpec(Spec.AVRO_SCHEMA))

    map.key("namespace", (DocumentModel.Package in doc).allowingAnnotations)

    val parsedShape = parseType(map)
    parsedShape.foreach(shape => doc.setWithoutId(FragmentModel.Encodes, shape, Annotations.inferred()))

    // resolve unresolved references
    ctx.futureDeclarations.resolve()
    doc
  }

  def parseType(map: YMap): Option[AnyShape] = new AvroShapeParser(map).parse()
}
