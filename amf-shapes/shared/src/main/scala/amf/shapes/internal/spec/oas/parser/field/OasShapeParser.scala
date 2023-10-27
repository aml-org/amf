package amf.shapes.internal.spec.oas.parser.field

import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DataNode, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.domain.Annotations.synthesized
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.{JSONSchemaId, TypePropertyLexicalInfo}
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, NodeShapeModel, ScalarShapeModel}
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import amf.shapes.internal.spec.common.{SchemaVersion, TypeDef}
import amf.shapes.internal.spec.common.parser.{
  FormatValidator,
  NodeDataNodeParser,
  OasLikeCreativeWorkParser,
  QuickFieldParserOps,
  ShapeParserContext
}
import amf.shapes.internal.spec.jsonschema.parser.{
  DependenciesParser,
  Draft4ShapeDependenciesParser,
  PropertyDependencyParser,
  SchemaDependencyParser,
  UnevaluatedParser
}
import amf.shapes.internal.spec.oas.parser
import amf.shapes.internal.spec.oas.parser.{AndConstraintParser, InnerShapeParser}
import amf.shapes.internal.spec.raml.parser.XMLSerializerParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  InvalidAdditionalPropertiesType,
  InvalidShapeFormat
}
import org.yaml.model.{YMap, YNode, YScalar, YType}

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

  val If: (SchemaVersion, Shape => Unit) => FieldParser = (version, adopt) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        InnerShapeParser("if", ShapeModel.If, map, shape, adopt, version).parse()
        shape
      }
    }

  val Then: (SchemaVersion, Shape => Unit) => FieldParser = (version, adopt) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        InnerShapeParser("then", ShapeModel.Then, map, shape, adopt, version).parse()
        shape
      }
    }

  val Else: (SchemaVersion, Shape => Unit) => FieldParser = (version, adopt) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        InnerShapeParser("else", ShapeModel.Else, map, shape, adopt, version).parse()
        shape
      }
    }

  val Const: (YNode => DataNode) => FieldParser = (dataNodeParser) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        map.key("const", (ShapeModel.Values in shape using dataNodeParser).allowingSingleValue)
        shape
      }
    }

  val Pattern: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("pattern", ScalarShapeModel.Pattern in shape)
      shape
    }
  }

  val MinLength: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("minLength", ScalarShapeModel.MinLength in shape)
      shape
    }
  }

  val MaxLength: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("maxLength", ScalarShapeModel.MaxLength in shape)
      shape
    }
  }

  val Minimum: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      setValue("minimum", map, ScalarShapeModel.Minimum, shape)
      shape
    }
  }

  val Maximum: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      setValue("maximum", map, ScalarShapeModel.Maximum, shape)
      shape
    }
  }

  val MultipleOf: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("multipleOf", ScalarShapeModel.MultipleOf in shape)
      shape
    }
  }

  val ExclusiveMinimumNumeric: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("exclusiveMinimum", ScalarShapeModel.ExclusiveMinimumNumeric in shape)
      shape
    }
  }

  val ExclusiveMaximumNumeric: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("exclusiveMaximum", ScalarShapeModel.ExclusiveMaximumNumeric in shape)
      shape
    }
  }

  val ExclusiveMaximumBoolean: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("exclusiveMaximum", ScalarShapeModel.ExclusiveMaximum in shape)
      shape
    }
  }

  val ExclusiveMinimumBoolean: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("exclusiveMinimum", ScalarShapeModel.ExclusiveMinimum in shape)
      shape
    }
  }

  val Format: TypeDef => FieldParser = (typeDef) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        map
          .key("format")
          .foreach { n =>
            val format = n.value.as[YScalar].text

            if (!FormatValidator.isValid(format, typeDef))
              ctx.eh.warning(
                InvalidShapeFormat,
                shape,
                s"Format $format is not valid for type ${XsdTypeDefMapping.xsd(typeDef)}",
                n.location
              )

            (ScalarShapeModel.Format in shape).allowingAnnotations(n)
          }
        shape
      }
    }

  val MinProperties: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("minProperties", NodeShapeModel.MinProperties in shape)
      shape
    }
  }

  val MaxProperties: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("maxProperties", NodeShapeModel.MaxProperties in shape)
      shape
    }
  }

  val AdditionalProperties: SchemaVersion => FieldParser = version =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        shape.setWithoutId(NodeShapeModel.Closed, AmfScalar(value = false), synthesized())

        map.key("additionalProperties").foreach { entry =>
          entry.value.tagType match {
            case YType.Bool => (NodeShapeModel.Closed in shape).negated.explicit(entry)
            case YType.Map =>
              parser.OasTypeParser(entry, s => Unit, version).parse().foreach { s =>
                shape.setWithoutId(NodeShapeModel.AdditionalPropertiesSchema, s, synthesized())
              }
            case _ =>
              ctx.eh.violation(
                InvalidAdditionalPropertiesType,
                shape,
                "Invalid part type for additional properties node. Should be a boolean or a map",
                entry.location
              )
          }
        }
        shape
      }
    }

  val UnevaluatedProperties: SchemaVersion => FieldParser = version =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        new UnevaluatedParser(version, UnevaluatedParser.unevaluatedPropertiesInfo).parse(map, shape)
        shape
      }
    }

  val Dependencies: SchemaVersion => FieldParser = version =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        map.key(
          "dependencies",
          entry => {
            Draft4ShapeDependenciesParser(shape, entry.value.as[YMap], shape.id, version).parse()
          }
        )
        shape
      }
    }

  val DependentRequired: String => FieldParser = parentId =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        map.key("dependentRequired").foreach { entry =>
          val propertyDependencies = entry.value
            .as[YMap]
            .entries
            .map(e => DependenciesParser(e, parentId, PropertyDependencyParser(e.value)).parse())
          shape
            .setWithoutId(
              NodeShapeModel.Dependencies,
              AmfArray(propertyDependencies, Annotations(entry.value)),
              Annotations(entry)
            )
        }
        shape
      }
    }

  val DependentSchemas: (SchemaVersion, String) => FieldParser = (version, parentId) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {

        map.key("dependentSchemas").foreach { entry =>
          val schemaDependencies = entry.value
            .as[YMap]
            .entries
            .map(e => DependenciesParser(e, parentId, SchemaDependencyParser(e.value, version)).parse())
          shape.setWithoutId(
            NodeShapeModel.SchemaDependencies,
            AmfArray(schemaDependencies, Annotations(entry.value)),
            Annotations(entry)
          )
        }
        shape
      }
    }

  def setValue(key: String, map: YMap, field: Field, shape: Shape): Unit =
    map.key(
      key,
      entry => {
        val value = amf.core.internal.parser.domain.ScalarNode(entry.value)
        shape.setWithoutId(field, value.text(), Annotations(entry))
      }
    )
}
