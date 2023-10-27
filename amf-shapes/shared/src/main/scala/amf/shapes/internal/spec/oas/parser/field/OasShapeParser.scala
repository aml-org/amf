package amf.shapes.internal.spec.oas.parser.field

import amf.core.client.scala.model.domain.{DataNode, Shape}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.{JSONSchemaId, TypePropertyLexicalInfo}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.{
  NodeDataNodeParser,
  OasLikeCreativeWorkParser,
  QuickFieldParserOps,
  ShapeParserContext
}
import amf.shapes.internal.spec.oas.parser.{AndConstraintParser, InnerShapeParser}
import amf.shapes.internal.spec.raml.parser.XMLSerializerParser
import org.yaml.model.{YMap, YNode, YScalar}

trait FieldParser extends QuickFieldParserOps {
  def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape
}

object ShapeParser {

  val Title: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("title", ShapeModel.DisplayName in shape)
      shape
    }
  }

  val Description: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("description", ShapeModel.Description in shape)
      shape
    }
  }

  val Default: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key(
        "default",
        node => {
          shape.setDefaultStrValue(node)
          NodeDataNodeParser(node.value, shape.id, quiet = false).parse().dataNode.foreach { dn =>
            shape.setWithoutId(ShapeModel.Default, dn, Annotations(node))
          }

        }
      )
      shape
    }
  }

  val Enum: (YNode => DataNode) => FieldParser = enumParser =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        map.key("enum", ShapeModel.Values in shape using enumParser)
        shape
      }
    }

  val ExternalDocs: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("externalDocs", AnyShapeModel.Documentation in shape using (OasLikeCreativeWorkParser.parse(_, shape.id)))
      shape
    }
  }

  val Xml: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("xml", AnyShapeModel.XMLSerialization in shape using XMLSerializerParser.parse(shape.name.value()))
      shape
    }
  }

  val Type: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("type", entry => shape.annotations += TypePropertyLexicalInfo(entry.key.range))
      shape
    }
  }

  val AnyOf: SchemaVersion => FieldParser = (version: SchemaVersion) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        OrConstraintParser(map, shape, version).parse()
        shape
      }
    }

  val AllOf: (SchemaVersion, Shape => Unit) => FieldParser = (version: SchemaVersion, adopt: Shape => Unit) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        AndConstraintParser(map, shape, adopt, version).parse()
        shape
      }
    }

  val OneOf: (SchemaVersion, Shape => Unit) => FieldParser = (version: SchemaVersion, adopt: Shape => Unit) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        XoneConstraintParser(map, shape, adopt, version).parse()
        shape
      }
    }

  val Not: (SchemaVersion, Shape => Unit) => FieldParser = (version: SchemaVersion, adopt: Shape => Unit) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        InnerShapeParser("not", ShapeModel.Not, map, shape, adopt, version).parse()
        shape
      }
    }

  val ReadOnly: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("readOnly", ShapeModel.ReadOnly in shape)
      shape
    }
  }

  val WriteOnly: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("writeOnly", ShapeModel.WriteOnly in shape)
      shape
    }
  }

  val Deprecated: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("deprecated", ShapeModel.Deprecated in shape)
      shape
    }
  }

  val Id: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("id", node => shape.annotations += JSONSchemaId(node.value.as[YScalar].text))
      shape
    }
  }
}
