package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.{AmfArray, ArrayNode, DataNode, Shape}
import amf.core.internal.datanode.DataNodeParser
import amf.core.internal.metamodel.domain.{ArrayNodeModel, ShapeModel}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.AVROSchemaType
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps
import org.yaml.model._

abstract class AvroComplexShapeParser(map: YMap)(implicit ctx: AvroSchemaContext)
    extends QuickFieldParserOps
    with AvroKeyExtractor {
  val shape: AnyShape

  def parse(): AnyShape = {
    addTypeToCache()
    parseCommonFields()
    parseSpecificFields()
    parseDefault()
    shape
  }

  def parseCommonFields(): Unit = {
    map.key("name", AnyShapeModel.Name in shape)
    map.key("namespace", (AnyShapeModel.AvroNamespace in shape).allowingAnnotations)
    map.key("aliases", (AnyShapeModel.Aliases in shape).allowingAnnotations)
    map.key("doc", (AnyShapeModel.Description in shape).allowingAnnotations)
  }

  // each specific parser should override and parse it's specific fields
  def parseSpecificFields(): Unit

  def parseDefault(): Unit = parseDefault(map, shape)

  def parseDefault(map: YMap, shape: Shape): Unit = {
    map.key(
      "default",
      entry => {
        val dataNode: DataNode = DataNodeParser(entry.value).parse()
        dataNode match {
          // todo: by default the DataNodeParser returns ArrayNodes without annotations (we should add them in amf-core?)
          case arrayNode: ArrayNode =>
            arrayNode.setWithoutId(
              ArrayNodeModel.Member,
              AmfArray(arrayNode.members, Annotations.inferred()),
              Annotations.inferred()
            )
            shape.set(ShapeModel.Default, arrayNode, Annotations(entry))
          case node => shape.set(ShapeModel.Default, node, Annotations(entry))
        }
      }
    )
  }

  private def addTypeToCache(): Unit = {
    def getText(node: YNode)                             = node.as[YScalar].text
    def getAliases(entry: YMapEntry): IndexedSeq[String] = entry.value.as[YSequence].nodes.map(getText)
    val name                                             = map.key("name").map(name => getText(name.value))
    val aliases                                          = map.key("aliases").map(getAliases)
    name.foreach(ctx.globalSpace.put(_, shape))
    aliases.foreach(_.foreach(alias => ctx.globalSpace.put(alias, shape)))
  }
}

trait AvroKeyExtractor {
  implicit class YMapKeys(map: YMap) {
    def typeValue: Option[YNode] = map.key("type").map(_.value)
    def `type`: Option[String]   = typeValue.flatMap(_.asScalar).map(_.text)
  }

  implicit class StringAvroOps(value: String) {
    def isPrimitive: Boolean =
      Seq("null", "boolean", "int", "long", "float", "double", "bytes", "string").contains(value)
  }

  def getAvroType(shape: Shape): Option[AVROSchemaType] = shape.annotations.find(classOf[AVROSchemaType])
}
