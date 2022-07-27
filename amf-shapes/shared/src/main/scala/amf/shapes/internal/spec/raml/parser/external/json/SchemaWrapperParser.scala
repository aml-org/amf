package amf.shapes.internal.spec.raml.parser.external.json

import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, ScalarShape}
import amf.shapes.internal.annotations.ExternalSchemaWrapper
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import amf.shapes.internal.spec.common.parser.{NodeDataNodeParser, QuickFieldParserOps, ShapeParserContext}
import amf.shapes.internal.spec.raml.parser.ExampleParser
import org.yaml.model.{YMap, YNode}

object SchemaWrapperParser extends ExampleParser with QuickFieldParserOps {
  def parse(map: YMap, wrapped: AnyShape, entryKey: YNode, entryValue: YNode)(implicit
      ctx: ShapeParserContext
  ): AnyShape = {
    val wrapper = wrapped.meta.modelInstance
    wrapper.annotations ++= Annotations(entryValue)
    map.key("displayName", (ShapeModel.DisplayName in wrapper).allowingAnnotations)
    map.key("description", (ShapeModel.Description in wrapper).allowingAnnotations)
    map.key(
      "default",
      entry => {
        val dataNodeResult = NodeDataNodeParser(entry.value, wrapper.id, quiet = false).parse()
        wrapper.setDefaultStrValue(entry)
        dataNodeResult.dataNode.foreach { dataNode =>
          wrapper.setWithoutId(ShapeModel.Default, dataNode, Annotations(entry))
        }
      }
    )
    parseExamples(wrapper, entryValue.as[YMap])
    wrapperName(entryKey).foreach(wrapper.withName(_, Annotations(entryKey)))
    val typeEntryAnnotations =
      map.key("type").orElse(map.key("schema")).map(e => Annotations(e)).getOrElse(Annotations())
    wrapper.setWithoutId(ShapeModel.Inherits, AmfArray(Seq(wrapped), Annotations.virtual()), typeEntryAnnotations)
    preResolveDataType(wrapped, wrapper)
    wrapper.annotations += ExternalSchemaWrapper()
    wrapper
  }

  private def preResolveDataType(parsed: AnyShape, wrapper: AnyShape): Unit = {
    parsed match {
      case scalarShape: ScalarShape =>
        val inheritedDataType = scalarShape.dataType.value()
        wrapper.set(ScalarShapeModel.DataType, inheritedDataType)
      case _ =>
    }
  }

  private def wrapperName(key: YNode) = key.asScalar.map(_.text)
}
