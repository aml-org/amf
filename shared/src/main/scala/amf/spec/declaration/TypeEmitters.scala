package amf.spec.declaration

import amf.domain._
import amf.framework.metamodel.Field
import amf.framework.model.document.BaseUnit
import amf.framework.model.domain.{AmfScalar, Linkable}
import amf.framework.parser.{Annotations, FieldEntry, Fields, Position}
import amf.framework.parser.Position.ZERO
import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.parser.{OasTypeDefMatcher, OasTypeDefStringValueMatcher, RamlTypeDefMatcher, RamlTypeDefStringValueMatcher}
import amf.plugins.domain.shapes.metamodel.{ExampleModel, _}
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping
import amf.plugins.domain.webapi.models._
import amf.spec._
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext
import amf.spec.domain.{MultipleExampleEmitter, SingleExampleEmitter, StringToAstEmitter}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YType

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
case class RamlNamedTypeEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit] = Nil)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = Option(shape.name).getOrElse(throw new Exception(s"Cannot declare shape without name $shape"))
    b.entry(name, if (shape.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    shape.linkTarget.foreach { l =>
      spec.tagToReference(l, shape.linkLabel, references).emit(b)
    }
  }

  private def emitInline(b: PartBuilder): Unit =
    RamlTypePartEmitter(shape, ordering, None, references = references).emit(b)

  override def position(): Position = pos(shape.annotations)
}

case class OasNamedTypeEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = Option(shape.name).getOrElse(throw new Exception(s"Cannot declare shape without name $shape"))
    b.entry(name, OasTypePartEmitter(shape, ordering, references = references).emit(_))
  }

  override def position(): Position = pos(shape.annotations)
}

case class RamlTypePartEmitter(shape: Shape,
                               ordering: SpecOrdering,
                               annotations: Option[AnnotationsEmitter],
                               ignored: Seq[Field] = Nil,
                               references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit = {
    if (Option(shape).isDefined && shape.annotations.contains(classOf[SynthesizedField])) {
      raw(b, "", YType.Null)
    } else {
      emitter match {
        case Left(p) => p.emit(b)
        case Right(entries) => b.obj(traverse(entries, _))
      }
    }
  }

  override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)

  private val emitters =
    ordering.sorted(
      RamlTypeEmitter(shape, ordering, ignored, references).emitters() ++ annotations.map(_.emitters).getOrElse(Nil))

  private val emitter: Either[PartEmitter, Seq[EntryEmitter]] = emitters match {
    case Seq(p: PartEmitter)                           => Left(p)
    case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
    case other                                         => throw new Exception(s"IllegalTypeDeclarations found: $other")
  }
}

case class RamlTypeExpressionEmitter(shape: Shape) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = raw(b, shape.typeExpression)

  override def position(): Position = pos(shape.annotations)
}

case class RamlTypeEmitter(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field] = Nil, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[Emitter] = {
    shape match {
      case _ if Option(shape).isDefined && shape.fromTypeExpression => Seq(RamlTypeExpressionEmitter(shape))
      case l: Linkable if l.isLink                                  => Seq(spec.localReference(shape))
      case schema: SchemaShape                                      => Seq(RamlSchemaShapeEmitter(schema))
      case node: NodeShape if node.annotations.find(classOf[ParsedJSONSchema]).isDefined =>
        Seq(RamlJsonShapeEmitter(node))
      case node: NodeShape =>
        val copiedNode = node.copy(fields = node.fields.filter(f => !ignored.contains(f._1)))
        RamlNodeShapeEmitter(copiedNode, ordering, references).emitters()
      case union: UnionShape =>
        val copiedNode = union.copy(fields = union.fields.filter(f => !ignored.contains(f._1)))
        RamlUnionShapeEmitter(copiedNode, ordering, references).emitters()
      case file: FileShape =>
        val copiedFile = file.copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
        RamlFileShapeEmitter(copiedFile, ordering, references).emitters()
      case scalar: ScalarShape =>
        val copiedScalar = scalar.copy(fields = scalar.fields.filter(f => !ignored.contains(f._1)))
        RamlScalarShapeEmitter(copiedScalar, ordering, references).emitters()
      case array: ArrayShape =>
        val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
        RamlArrayShapeEmitter(copiedArray, ordering, references).emitters()
      case tuple: TupleShape =>
        val copiedTuple = tuple.copy(fields = tuple.fields.filter(f => !ignored.contains(f._1)))
        RamlTupleShapeEmitter(copiedTuple, ordering, references).emitters()
      case matrix: MatrixShape =>
        val copiedMatrix = matrix.copy(fields = matrix.fields.filter(f => !ignored.contains(f._1)))
        RamlArrayShapeEmitter(copiedMatrix.toArrayShape, ordering, references).emitters()
      case any: AnyShape =>
        val copiedNode = any.copy(fields = any.fields.filter(f => !ignored.contains(f._1)))
        RamlAnyShapeEmitter(copiedNode, ordering, references).emitters()
      case nil: NilShape =>
        val copiedNode = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
        RamlNilShapeEmitter(copiedNode, ordering, references).emitters()
      case _ => Seq()
    }
  }

  def entries(): Seq[EntryEmitter] = emitters() map { case e: EntryEmitter => e }
}

abstract class RamlShapeEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext) {

  val typeName: Option[String]

  def emitters(): Seq[EntryEmitter] = {

    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    Option(fs.getValue(ShapeModel.RequiredShape)) match {
      case Some(v) =>
        if (v.annotations.contains(classOf[ExplicitField])) {
          fs.entry(ShapeModel.RequiredShape).map(f => result += ValueEmitter("required", f))
        }
      case None => // ignore
    }

    fs.entry(ShapeModel.DisplayName).map(f => result += ValueEmitter("displayName", f))

    fs.entry(ShapeModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(ShapeModel.Default)
      .map(f => {
        result += EntryPartEmitter("default",
                                   StringToAstEmitter(f.value.toString),
                                   position = pos(f.value.annotations))
      })

    fs.entry(ShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

    fs.entry(ShapeModel.Documentation)
      .map(f =>
        result += OasEntryCreativeWorkEmitter("(externalDocs)", f.value.value.asInstanceOf[CreativeWork], ordering))

    fs.entry(ShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    fs.entry(ShapeModel.Examples)
      .map(f => {
        val examples = f.array.values.collect({ case e: Example => e })
        if (examples.size == 1 && examples.head.annotations.contains(classOf[SingleValueArray]))
          result += SingleExampleEmitter("example", examples.head, ordering)
        else
          result += MultipleExampleEmitter("examples", examples, ordering, references)
      })

    fs.entry(ShapeModel.CustomShapePropertyDefinitions)
      .map(f => {
        result += CustomFacetsEmitter(f, ordering, references)
      })

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    fs.entry(ShapeModel.Inherits)
      .fold(
        typeName.foreach(value => result += MapEntryEmitter("type", value))
      )(f => result += RamlShapeInheritsEmitter(f, ordering, references = references))

    result
  }
}

case class RamlJsonShapeEmitter(shape: NodeShape) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    shape.annotations.find(classOf[ParsedJSONSchema]) match {
      case Some(json) => raw(b, json.rawText)
      case None       => // Ignore
    }
  }

  override def position(): Position = {
    pos(shape.annotations)
  }
}

case class RamlSchemaShapeEmitter(shape: SchemaShape) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = raw(b, shape.raw)

  override def position(): Position = pos(shape.annotations)
}

case class XMLSerializerEmitter(key: String, f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      f.value,
      b.entry(
        key,
        b => {
          val fs     = f.obj.fields
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(XMLSerializerModel.Attribute)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("attribute", f))

          fs.entry(XMLSerializerModel.Wrapped)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("wrapped", f))

          fs.entry(XMLSerializerModel.Name)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("name", f))

          fs.entry(XMLSerializerModel.Namespace).map(f => result += ValueEmitter("namespace", f))

          fs.entry(XMLSerializerModel.Prefix).map(f => result += ValueEmitter("prefix", f))

          result ++= AnnotationsEmitter(f.domainElement, ordering).emitters

          b.obj(traverse(ordering.sorted(result), _))
        }
      )
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class RamlNodeShapeEmitter(node: NodeShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(node, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs                               = node.fields

    fs.entry(NodeShapeModel.MinProperties).map(f => result += ValueEmitter("minProperties", f))

    fs.entry(NodeShapeModel.MaxProperties).map(f => result += ValueEmitter("maxProperties", f))

    fs.entry(NodeShapeModel.Closed)
      .foreach(f => {
        if (node.closed || f.value.annotations.contains(classOf[ExplicitField])) {
          result += MapEntryEmitter("additionalProperties",
                                    (!node.closed).toString,
                                    position = pos(f.value.annotations))
        }
      })

    fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("discriminator", f))

    fs.entry(NodeShapeModel.DiscriminatorValue).map(f => result += ValueEmitter("discriminatorValue", f))

    fs.entry(NodeShapeModel.ReadOnly).map(f => result += ValueEmitter("(readOnly)", f))

    fs.entry(NodeShapeModel.Properties).map(f => result += RamlPropertiesShapeEmitter(f, ordering, references))

    val propertiesMap = ListMap(node.properties.map(p => p.id -> p): _*)

    fs.entry(NodeShapeModel.Dependencies)
      .map(f => result += RamlShapeDependenciesEmitter(f, ordering, propertiesMap))

    result
  }

  override val typeName: Option[String] = node.annotations.find(classOf[ExplicitField]).map(_ => "object")
}

case class RamlShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    val (declaredShapes, inlineShapes) =
      f.array.values
        .map(_.asInstanceOf[Shape])
        .partition(s =>
          s.annotations.contains(classOf[DeclaredElement]) ||
            s.annotations.contains(classOf[ParsedFromTypeExpression]) || s.isLink)

    b.entry(
      "type",
      b => {
        if (inlineShapes.nonEmpty) {
          b.obj(
            traverse(
              ordering.sorted(inlineShapes.flatMap(RamlTypeEmitter(_, ordering, references = references).entries())),
              _))
        } else {
          declaredShapes match {
            case head :: Nil =>
              emitDeclared(head, b)
            case _ =>
              b.list { b =>
                declaredShapes.foreach(emitDeclared(_, b))
              }

          }
        }
      }
    )
  }

  private def emitDeclared(shape: Shape, b: PartBuilder): Unit = shape match {
    case shape: Shape if shape.annotations.contains(classOf[ParsedFromTypeExpression]) =>
      RamlTypeExpressionEmitter(shape).emit(b)
    case s: Shape =>
      if (s.isLink) spec.localReference(s).emit(b)
      else raw(b, s.name)
  }

  override def position(): Position = pos(f.value.annotations)
}

case class RamlAnyShapeEmitter(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(shape, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = super.emitters() //:+ MapEntryEmitter("type", "any")

  override val typeName: Option[String] = Some("any")
}

case class RamlNilShapeEmitter(shape: NilShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(shape, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = super.emitters() //:+ MapEntryEmitter("type", "nil")

  override val typeName: Option[String] = Some("nil")
}

trait RamlCommonOASFieldsEmitter {
  def emitOASFields(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.ExclusiveMinimum).map(f => result += ValueEmitter("(exclusiveMinimum)", f))

    fs.entry(ScalarShapeModel.ExclusiveMaximum).map(f => result += ValueEmitter("(exclusiveMaximum)", f))

  }
}
case class RamlScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(scalar, ordering, references)
    with RamlCommonOASFieldsEmitter {

  override def emitters(): Seq[EntryEmitter] = {
    val fs = scalar.fields

    val rawTypeDef        = TypeDefXsdMapping.typeDef(scalar.dataType)
    val (typeDef, format) = RamlTypeDefStringValueMatcher.matchType(rawTypeDef)

    val typeEmitterOption = if (scalar.inherits.isEmpty) {
      fs.entry(ScalarShapeModel.DataType)
        .flatMap(f =>
          if (!f.value.annotations.contains(classOf[Inferred])) {
            scalar.fields
              .remove(ShapeModel.Inherits) // for scalar doesn't make any sense to write the inherits, because it will always be another scalar with the same t
            Some(MapEntryEmitter("type", typeDef, position = pos(f.value.annotations)))
          } else None) // TODO check this  - annotations of typeDef in parser
    } else {
      None
    }

    // use option for not alter the previous default order. (After resolution not any lexical info annotation remains here)

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*) ++ typeEmitterOption

    emitOASFields(fs, result)

    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern", f))

    fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("minimum", f))

    fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("maximum", f))

    fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("multipleOf", f))

    result ++= emitFormat(rawTypeDef, fs, format)

    result
  }

  def emitFormat(rawTypeDef: TypeDef, fs: Fields, format: String): Seq[EntryEmitter] = {
    val formatKey =
      if (rawTypeDef.isNumber | rawTypeDef.isDate) "format"
      else "(format)"

    val translationFormats: Set[String] = OasTypeDefMatcher.knownFormats.diff(RamlTypeDefMatcher.knownFormats)
    var explictFormatFound              = false
    val explicitFormat = fs.entry(ScalarShapeModel.Format) match {
      case Some(entry) if entry.value.value.isInstanceOf[AmfScalar] =>
        val entryFormat = entry.value.value.asInstanceOf[AmfScalar].value.toString
        if (translationFormats(entryFormat)) {
          // this formats are here just because we parsed from OAS, the type in RAML has enough
          // information, we don't need the annotation with this format.
          // They will be re-generated correctly when translating into OAS
          format
        } else {
          explictFormatFound = true
          entryFormat
        }
      case _ => format
    }
    val finalFormat = if (explicitFormat != format) {
      explicitFormat
    } else {
      format
    }

    val annotations = fs.entry(ScalarShapeModel.Format) match {
      case Some(entry) if entry.value.value.isInstanceOf[AmfScalar] => entry.value.annotations
      case _                                                        => Annotations()
    }

    if (finalFormat.nonEmpty && finalFormat != "float" && finalFormat != "int32") {
      Seq(RawValueEmitter(formatKey, ScalarShapeModel.Format, finalFormat, annotations))
    } else if (finalFormat.nonEmpty && (finalFormat == "float" || finalFormat == "int32") && explictFormatFound) {
      // we always mapping 'number' in RAML to xsd:float, if we are to emit 'float'
      // as the format must be because it has been explicitly set in this way, not because
      // we are adding that through the number -> xsd:float mapping
      Seq(RawValueEmitter(formatKey, ScalarShapeModel.Format, finalFormat, annotations))
    } else {
      Seq()
    }
  }

  override val typeName: Option[String] = None //exceptional case for get the type (scalar) and format
}

case class RamlFileShapeEmitter(scalar: FileShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(scalar, ordering, references)
    with RamlCommonOASFieldsEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    emitOASFields(fs, result)

    fs.entry(FileShapeModel.FileTypes).map(f => result += ArrayEmitter("fileTypes", f, ordering))

    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("(pattern)", f))

    fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("(minimum)", f))

    fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("(maximum)", f))

    fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("(multipleOf)", f))

    result
  }

  override val typeName: Option[String] = Some("file")
}

case class RamlShapeDependenciesEmitter(f: FieldEntry, ordering: SpecOrdering, props: ListMap[String, PropertyShape])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "(dependencies)",
      _.obj { b =>
        val result =
          f.array.values.map(v =>
            RamlPropertyDependenciesEmitter(v.asInstanceOf[PropertyDependencies], ordering, props))
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class RamlPropertyDependenciesEmitter(
    property: PropertyDependencies,
    ordering: SpecOrdering,
    properties: ListMap[String, PropertyShape])(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    properties
      .get(property.propertySource)
      .foreach(p => {
        b.entry(
          p.name,
          b => {
            val targets = property.fields
              .entry(PropertyDependenciesModel.PropertyTarget)
              .map(f => {
                f.array.scalars.flatMap(iri =>
                  properties.get(iri.value.toString).map(p => AmfScalar(p.name, iri.annotations)))
              })

            targets.foreach(target => {
              b.list { b =>
                traverse(ordering.sorted(target.map(t => ScalarEmitter(t))), b)
              }
            })
          }
        )
      })
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}

case class RamlUnionShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(shape, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    super.emitters() :+ RamlAnyOfShapeEmitter(shape, ordering, references = references)
  }

  override val typeName: Option[String] = Some("union")
}

case class RamlAnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "anyOf",
      _.list { b =>
        val emitters = shape.anyOf.map(RamlTypePartEmitter(_, ordering, None, references = references))
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = pos(shape.fields.getValue(UnionShapeModel.AnyOf).annotations)
}

case class RamlArrayShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(array, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = array.fields

    fs.entry(ArrayShapeModel.Items)
      .foreach(_ => {
        result += RamlItemsShapeEmitter(array, ordering, references)
      })

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    result
  }

  override val typeName: Option[String] = array.annotations.find(classOf[ExplicitField]).map(_ => "array")
}

case class RamlTupleShapeEmitter(tuple: TupleShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(tuple, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = tuple.fields

    result += RamlTupleItemsShapeEmitter(tuple, ordering, references)
    result += MapEntryEmitter("(tuple)", "true")

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    result
  }

  override val typeName: Option[String] = tuple.annotations.find(classOf[ExplicitField]).map(_ => "array")
}

case class RamlItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "items",
      // todo garrote review ordering
      _.obj(b => RamlTypeEmitter(array.items, ordering, references = references).entries().foreach(_.emit(b)))
    )
  }

  override def position(): Position = {
    pos(array.fields.getValue(ArrayShapeModel.Items).annotations)
  }
}

case class RamlTupleItemsShapeEmitter(tuple: TupleShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val result = mutable.ListBuffer[EntryEmitter]()

    tuple.items
      .foreach(item => {
        RamlTypeEmitter(item, ordering, references = references).entries().foreach(result += _)
      })

    // todo garrote review type
    /* b.entry(
      "items",
      _.list { b =>
        traverse(ordering.sorted(result), b)
      }
    ) */
  }

  override def position(): Position = pos(tuple.fields.getValue(ArrayShapeModel.Items).annotations)
}

case class RamlPropertiesShapeEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "properties",
      _.obj { b =>
        val result =
          f.array.values.map(v => RamlPropertyShapeEmitter(v.asInstanceOf[PropertyShape], ordering, references))
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class RamlPropertyShapeEmitter(property: PropertyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val fs = property.fields

    val name: String = fs
      .entry(PropertyShapeModel.MinCount)
      .map(f => {
        if (f.scalar.value.asInstanceOf[Int] == 0 && !f.value.annotations.contains(classOf[ExplicitField]))
          property.name + "?"
        else property.name
      })
      .getOrElse(property.name)

    if (property.range.annotations.contains(classOf[SynthesizedField])) {
      b.entry(
        name,
        raw(_, "", YType.Null)
      )
    } else {
      b.entry(
        name,
        RamlTypePartEmitter(property.range, ordering, None, references = references).emit(_)
      )
    }
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}

case class RamlSchemaEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val shape = f.value.value.asInstanceOf[Shape]
    b.entry(
      "type",
      RamlTypePartEmitter(shape, ordering, None, references = references).emit(_)
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasSchemaEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val shape = f.value.value.asInstanceOf[Shape]

    b.entry(
      "schema",
      OasTypePartEmitter(shape, ordering, references = references).emit(_)
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasTypePartEmitter(shape: Shape,
                              ordering: SpecOrdering,
                              ignored: Seq[Field] = Nil,
                              references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit =
    emitter match {
      case Left(p)        => p.emit(b)
      case Right(entries) => b.obj(traverse(entries, _))
    }

  override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)

  private val emitters = ordering.sorted(OasTypeEmitter(shape, ordering, ignored, references).emitters())

  private val emitter: Either[PartEmitter, Seq[EntryEmitter]] = emitters match {
    case Seq(p: PartEmitter)                           => Left(p)
    case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
    case other                                         => throw new Exception(s"IllegalTypeDeclarations found: $other")
  }
}

case class OasTypeEmitter(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field] = Nil, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[Emitter] = {
    shape match {
      case l: Linkable if l.isLink => Seq(OasTagToReferenceEmitter(shape, l.linkLabel))
      case schema: SchemaShape =>
        val copiedNode = schema.copy(fields = schema.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasSchemaShapeEmitter(copiedNode, ordering).emitters()
      case any: AnyShape =>
        val copiedNode = any.copy(fields = any.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        Seq(OasAnyShapeEmitter(copiedNode, ordering))
      case node: NodeShape =>
        val copiedNode = node.copy(fields = node.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasNodeShapeEmitter(copiedNode, ordering, references).emitters()
      case union: UnionShape =>
        val copiedNode = union.copy(fields = union.fields.filter(f => !ignored.contains(f._1)))
        Seq(OasUnionShapeEmitter(copiedNode, ordering, references))
      case array: ArrayShape =>
        val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
        OasArrayShapeEmitter(copiedArray, ordering, references).emitters()
      case nil: NilShape =>
        val copiedNil = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
        Seq(OasNilShapeEmitter(copiedNil, ordering))
      case file: FileShape =>
        val copiedScalar = file.copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
        OasFileShapeEmitter(copiedScalar, ordering, references).emitters()
      case scalar: ScalarShape =>
        val copiedScalar = scalar.copy(fields = scalar.fields.filter(f => !ignored.contains(f._1)))
        OasScalarShapeEmitter(copiedScalar, ordering, references).emitters()
      case _ => Seq()
    }
  }

  def entries(): Seq[EntryEmitter] = emitters() map { case e: EntryEmitter => e }
}

abstract class OasShapeEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    fs.entry(ShapeModel.DisplayName).map(f => result += ValueEmitter("title", f))

    fs.entry(ShapeModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(ShapeModel.Default)
      .map(f => {
        result += EntryPartEmitter("default",
                                   StringToAstEmitter(f.value.toString),
                                   position = pos(f.value.annotations))
      })

    fs.entry(ShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

    fs.entry(ShapeModel.Documentation)
      .map(f =>
        result += OasEntryCreativeWorkEmitter("externalDocs", f.value.value.asInstanceOf[CreativeWork], ordering))

    fs.entry(ShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    fs.entry(ShapeModel.Examples)
      .map(f => {
        val examples = f.array.values.collect({ case e: Example => e })
        val tuple    = examples.partition(e => !e.fields.fieldsMeta().contains(ExampleModel.Name) && !e.isLink)

        result ++= (tuple match {
          case (Nil, Nil)         => Nil
          case (named, Nil)       => examplesEmitters(named.headOption, named.tail)
          case (Nil, named)       => examplesEmitters(None, named)
          case (anonymous, named) => examplesEmitters(anonymous.headOption, anonymous.tail ++ named)
        })
      })

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    fs.entry(ShapeModel.CustomShapePropertyDefinitions)
      .map(f => {
        result += CustomFacetsEmitter(f, ordering, references)
      })

    result
  }

  private def examplesEmitters(main: Option[Example], extentions: Seq[Example]) = {
    val em = ListBuffer[EntryEmitter]()
    main.foreach(a => em += SingleExampleEmitter("example", a, ordering))
    if (extentions.nonEmpty)
      em += MultipleExampleEmitter("x-examples", extentions, ordering, references)
    em
  }
}

case class OasUnionShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "anyOf",
      _.list { b =>
        val emitters = shape.anyOf.map(OasTypePartEmitter(_, ordering, references = references))
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}

case class OasAnyShapeEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    // ignore
  }
  override def position(): Position = pos(shape.annotations)
}

case class OasArrayShapeEmitter(shape: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    result += MapEntryEmitter("type", "array")

    result += OasItemsShapeEmitter(shape, ordering, references)

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    result
  }
}

case class OasSchemaShapeEmitter(shape: SchemaShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    result += MapEntryEmitter("type", "object")

    fs.entry(SchemaShapeModel.MediaType).map(f => result += ValueEmitter("x-media-type", f))

    fs.entry(SchemaShapeModel.Raw).map(f => result += ValueEmitter("x-schema", f))

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    result
  }
}

case class OasItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit =
    b.entry("items", OasTypePartEmitter(array.items, ordering, references = references).emit(_))

  override def position(): Position = pos(array.items.fields.getValue(ArrayShapeModel.Items).annotations)
}

case class OasNodeShapeEmitter(node: NodeShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends OasShapeEmitter(node, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = node.fields

    // TODO annotation for original position?
    if (node.annotations.contains(classOf[ExplicitField]))
      result += MapEntryEmitter("type", "object")

    fs.entry(NodeShapeModel.MinProperties).map(f => result += ValueEmitter("minProperties", f))

    fs.entry(NodeShapeModel.MaxProperties).map(f => result += ValueEmitter("maxProperties", f))

    fs.entry(NodeShapeModel.Closed)
      .filter(_.value.annotations.contains(classOf[ExplicitField]))
      .map(f => result += ValueEmitter("additionalProperties", f.negated))

    fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("discriminator", f))

    fs.entry(NodeShapeModel.DiscriminatorValue).map(f => result += ValueEmitter("x-discriminator-value", f))

    fs.entry(NodeShapeModel.ReadOnly).map(f => result += ValueEmitter("readOnly", f))

    // TODO required array.

    fs.entry(NodeShapeModel.Properties).map(f => result += OasPropertiesShapeEmitter(f, ordering, references))

    val properties = ListMap(node.properties.map(p => p.id -> p): _*)

    fs.entry(NodeShapeModel.Dependencies).map(f => result += OasShapeDependenciesEmitter(f, ordering, properties))

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering, references))

    result
  }

}

case class OasShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val inherits = f.array.values.map(_.asInstanceOf[Shape])
    b.entry(
      "allOf",
      _.list(b =>
        inherits.foreach { s =>
          if (s.annotations.contains(classOf[DeclaredElement]))
            spec.ref(b, OasDefinitions.appendDefinitionsPrefix(s.name))
          else OasTypePartEmitter(s, ordering, references = references).emit(b)
      })
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasShapeDependenciesEmitter(f: FieldEntry,
                                       ordering: SpecOrdering,
                                       propertiesMap: ListMap[String, PropertyShape])
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    b.entry(
      "dependencies",
      _.obj { b =>
        val result = f.array.values.map(v =>
          OasPropertyDependenciesEmitter(v.asInstanceOf[PropertyDependencies], ordering, propertiesMap))
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasPropertyDependenciesEmitter(property: PropertyDependencies,
                                          ordering: SpecOrdering,
                                          properties: ListMap[String, PropertyShape])
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    properties
      .get(property.propertySource)
      .foreach(p => {
        b.entry(
          p.name,
          _.list { b =>
            val targets = property.fields
              .entry(PropertyDependenciesModel.PropertyTarget)
              .map(f => {
                f.array.scalars.flatMap(iri =>
                  properties.get(iri.value.toString).map(p => AmfScalar(p.name, iri.annotations)))
              })

            targets.foreach(target => {
              traverse(ordering.sorted(target.map(t => ScalarEmitter(t))), b)
            })
          }
        )
      })
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}

case class OasNilShapeEmitter(nil: NilShape, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = b.entry("type", "null")

  override def position(): Position = pos(nil.annotations)
}

trait OasCommonOASFieldsEmitter {
  def emitCommonFields(fs: Fields, result: ListBuffer[EntryEmitter]): Option[result.type] = {
    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern", f))

    fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("minimum", f))

    fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("maximum", f))

    fs.entry(ScalarShapeModel.ExclusiveMinimum).map(f => result += ValueEmitter("exclusiveMinimum", f))

    fs.entry(ScalarShapeModel.ExclusiveMaximum).map(f => result += ValueEmitter("exclusiveMaximum", f))

    fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("multipleOf", f))

    fs.entry(ScalarShapeModel.Format).map(f => result += ValueEmitter("format", f))
  }
}

case class OasScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends OasShapeEmitter(scalar, ordering, references)
    with OasCommonOASFieldsEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    val typeDef = OasTypeDefStringValueMatcher.matchType(TypeDefXsdMapping.typeDef(scalar.dataType))

    fs.entry(ScalarShapeModel.DataType)
      .foreach(f =>
        if (!f.value.annotations.contains(classOf[Inferred]))
          result += MapEntryEmitter("type", typeDef, position = pos(f.value.annotations))) // TODO check this  - annotations of typeDef in parser

    fs.entry(ScalarShapeModel.Format) match {
      case Some(_) => // ignore, this will be set with the explicit information
      case None =>
        OasTypeDefStringValueMatcher.matchFormat(TypeDefXsdMapping.typeDef(scalar.dataType)) match {
          case Some(format) => result += RawValueEmitter("format", ScalarShapeModel.Format, format)
          case None         => // ignore
        }
    }
    emitCommonFields(fs, result)

    result
  }
}

case class OasFileShapeEmitter(scalar: FileShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends OasShapeEmitter(scalar, ordering, references)
    with OasCommonOASFieldsEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    result += MapEntryEmitter("type", "file")

    emitCommonFields(fs, result)

    fs.entry(FileShapeModel.FileTypes).map(f => result += ArrayEmitter("x-fileTypes", f, ordering))

    result
  }
}

case class OasPropertiesShapeEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    b.entry(
      "properties",
      _.obj { b =>
        val result =
          f.array.values.map(v => OasPropertyShapeEmitter(v.asInstanceOf[PropertyShape], ordering, references))
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasPropertyShapeEmitter(property: PropertyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      property.name,
      OasTypePartEmitter(property.range, ordering, references = references).emit(_)
    )
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}
