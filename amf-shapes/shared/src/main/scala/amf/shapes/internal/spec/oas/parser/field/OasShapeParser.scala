package amf.shapes.internal.spec.oas.parser.field

import amf.aml.internal.parse.common.AnnotationsParser
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DataNode, Shape}
import amf.core.client.scala.vocabulary.Namespace
import amf.core.internal.annotations.{ExplicitField, InferredProperty}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.{YMapOps, YNodeLikeOps}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized, virtual}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, SemanticContext}
import amf.shapes.internal.annotations.{JSONSchemaId, TypePropertyLexicalInfo}
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, ArrayShapeModel, NodeShapeModel, ScalarShapeModel}
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import amf.shapes.internal.spec.SemanticContextParser
import amf.shapes.internal.spec.common.{
  JSONSchemaDraft3SchemaVersion,
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  SchemaVersion,
  TypeDef
}
import amf.shapes.internal.spec.common.parser.{
  AnnotationParser,
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
  DuplicateRequiredItem,
  InvalidAdditionalPropertiesType,
  InvalidRequiredArrayForSchemaVersion,
  InvalidRequiredValue,
  InvalidShapeFormat
}
import org.mulesoft.common.collections.Group
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YType}

import scala.collection.mutable

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

  val FacetsExtension: SchemaVersion => FieldParser = version =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        map.key(
          "facets".asOasExtension,
          entry =>
            PropertiesParser(entry.value.as[YMap], version, shape.withCustomShapePropertyDefinition, Map()).parse()
        )
        shape
      }
    }

  val Type: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      // Explicit annotation for the type property
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

  val `$Comment`: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("$comment", AnyShapeModel.Comment in shape)
      shape
    }
  }

  val `@Context`: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      shape match {
        case any: AnyShape =>
          val semanticContext = parseSemanticContext(map, any)
          semanticContext.foreach(any.withSemanticContext)
          if (ctx.getSemanticContext.isEmpty) ctx.withSemanticContext(semanticContext)
        case _ => // ignore
      }
      shape
    }

    private def parseSemanticContext(map: YMap, shape: AnyShape)(implicit
        ctx: ShapeParserContext
    ): Option[SemanticContext] = {
      SemanticContextParser(map, shape)(ctx).parse()
    }
  }

  val Properties: (SchemaVersion, Shape => Unit) => FieldParser = (version, adopt) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = shape match {
        case node: NodeShape =>
          parseProperties(version, adopt, map, node)
          node
        case _ => shape
      }
    }

  private def generateUndefinedRequiredProperties(
      requiredFields: Map[String, YNode],
      properties: mutable.LinkedHashMap[String, PropertyShape]
  ): Unit = {
    val undefinedRequiredProperties = requiredFields.keySet.filter(!properties.keySet.contains(_))
    val generatedRequiredProperties = undefinedRequiredProperties
      .map(propertyName => {
        PropertyShape(virtual() += InferredProperty())
          .withName(propertyName)
          .set(PropertyShapeModel.MinCount, AmfScalar(1), synthesized())
          .set(PropertyShapeModel.Range, AnyShape(), synthesized())
          .set(PropertyShapeModel.Path, AmfScalar((Namespace.Data + propertyName).iri()), synthesized())
      })
    properties ++= generatedRequiredProperties.map(p => p.name.value() -> p)
  }

  private def parseRequiredFields(version: SchemaVersion, adopt: Shape => Unit, map: YMap, shape: NodeShape)(implicit
      ctx: ShapeParserContext
  ): Map[String, YNode] = {

    def parse(field: YMapEntry): Map[String, YNode] = {
      val defaultValue = Map[String, YNode]()
      val requiredSeq  = field.value.asOption[Seq[YNode]]
      requiredSeq match {
        case Some(required) =>
          val requiredGroup = required.legacyGroupBy(_.as[String])
          validateRequiredFields(requiredGroup, shape)
          requiredGroup.map { case (key, nodes) =>
            key -> nodes.head
          }
        case None =>
          ctx.eh.violation(InvalidRequiredValue, shape, "'required' field has to be an array", loc = field.location)
          defaultValue
      }
    }

    val defaultValue = Map[String, YNode]()
    map
      .key("required")
      .map { field =>
        (field.value.tagType, version) match {
          case (YType.Seq, JSONSchemaDraft3SchemaVersion) =>
            ctx.eh.violation(
              InvalidRequiredArrayForSchemaVersion,
              shape,
              "Required arrays of properties not supported in JSON Schema below version draft-4",
              field.value.location
            )
            defaultValue
          case (_, JSONSchemaDraft3SchemaVersion)        => defaultValue
          case (YType.Seq, JSONSchemaUnspecifiedVersion) => parse(field)
          case (_, JSONSchemaUnspecifiedVersion)         => defaultValue
          case (_, _)                                    => parse(field)
        }
      }
      .getOrElse(defaultValue)
  }

  private def validateRequiredFields(required: Map[String, Seq[YNode]], shape: NodeShape)(implicit
      ctx: ShapeParserContext
  ): Unit =
    required
      .foreach {
        case (name, nodes) if nodes.size > 1 =>
          ctx.eh.violation(
            DuplicateRequiredItem,
            shape,
            s"'$name' is duplicated in 'required' property",
            nodes.last.location
          )
        case _ => // ignore
      }

  private def parseProperties(version: SchemaVersion, adopt: Shape => Unit, map: YMap, shape: NodeShape)(implicit
      ctx: ShapeParserContext
  ) = {
    val requiredFields = parseRequiredFields(version, adopt, map, shape)

    val properties      = mutable.LinkedHashMap[String, PropertyShape]()
    val propertiesEntry = map.key("properties")
    propertiesEntry.foreach(entry => {
      Option(entry.value.as[YMap]) match {
        case Some(m) =>
          val props = PropertiesParser(m, version, shape.withProperty, requiredFields).parse()
          properties ++= props.map(p => p.name.value() -> p)
        case _ => // Empty properties node.
      }
    })
    generateUndefinedRequiredProperties(requiredFields, properties)
    if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
      InnerShapeParser("propertyNames", NodeShapeModel.PropertyNames, map, shape, adopt, version).parse()

    val patternPropEntry = map.key("patternProperties")

    patternPropEntry.foreach(entry => {
      entry.value.toOption[YMap] match {
        case Some(m) =>
          properties ++=
            PropertiesParser(m, version, shape.withProperty, requiredFields, patterned = true)
              .parse()
              .map(p => p.name.value() -> p)
        case _ => // Empty properties node.
      }
    })

    val (propertiesAnnotations, propertiesFieldAnnotations) = propertiesEntry.map { pe =>
      (Annotations(pe.value), Annotations(pe))
    } orElse {
      patternPropEntry.map { pp =>
        (Annotations(pp.value), Annotations(pp))
      }
    } getOrElse {
      (virtual(), inferred())
    }

    if (properties.nonEmpty || propertiesEntry.nonEmpty)
      shape.setWithoutId(
        NodeShapeModel.Properties,
        AmfArray(properties.values.toSeq, propertiesAnnotations),
        propertiesFieldAnnotations
      )
  }

  val OasExtensions: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      AnnotationParser(shape, map).parse()
      shape
    }
  }

  val ExplicitTypeAnnotation: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("type", _ => shape.add(ExplicitField())) // todo lexical of type?? new annotation?
      shape
    }
  }

  val MinItems: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("minItems", ArrayShapeModel.MinItems in shape)
      shape
    }
  }

  val MaxItems: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("maxItems", ArrayShapeModel.MaxItems in shape)
      shape
    }
  }

  val UniqueItems: FieldParser = new FieldParser {
    override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
      map.key("uniqueItems", ArrayShapeModel.UniqueItems in shape)
      shape
    }
  }

  val Contains: (SchemaVersion, Shape => Unit) => FieldParser = (version, adopt) =>
    new FieldParser {
      override def parse(map: YMap, shape: Shape)(implicit ctx: ShapeParserContext): Shape = {
        InnerShapeParser("contains", ArrayShapeModel.Contains, map, shape, adopt, version).parse()
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
