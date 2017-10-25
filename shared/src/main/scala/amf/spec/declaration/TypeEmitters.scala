package amf.spec.declaration

import amf.document.BaseUnit
import amf.domain.Annotation._
import amf.domain.{CreativeWork, FieldEntry, Fields, Linkable}
import amf.metadata.Field
import amf.metadata.shape._
import amf.model.AmfScalar
import amf.parser.Position
import amf.parser.Position.ZERO
import amf.shape._
import amf.spec._
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

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

  private def emitInline(b: PartBuilder): Unit = RamlTypePartEmitter(shape, ordering, None).emit(b)

  override def position(): Position = pos(shape.annotations)
}

case class OasNamedTypeEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = Option(shape.name).getOrElse(throw new Exception(s"Cannot declare shape without name $shape"))
    b.entry(name, OasTypePartEmitter(shape, ordering).emit(_))
  }

  override def position(): Position = pos(shape.annotations)
}

case class RamlTypePartEmitter(shape: Shape,
                               ordering: SpecOrdering,
                               annotations: Option[AnnotationsEmitter],
                               ignored: Seq[Field] = Nil)(implicit spec: SpecEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit =
    emitter match {
      case Left(p)        => p.emit(b)
      case Right(entries) => b.obj(traverse(entries, _))
    }

  override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)

  private val emitters =
    ordering.sorted(RamlTypeEmitter(shape, ordering, ignored).emitters() ++ annotations.map(_.emitters).getOrElse(Nil))

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

case class RamlTypeEmitter(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field] = Nil)(
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
        RamlNodeShapeEmitter(copiedNode, ordering).emitters()
      case union: UnionShape =>
        val copiedNode = union.copy(fields = union.fields.filter(f => !ignored.contains(f._1)))
        RamlUnionShapeEmitter(copiedNode, ordering).emitters()
      case file: FileShape =>
        val copiedFile = file.copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
        RamlFileShapeEmitter(copiedFile, ordering).emitters()
      case scalar: ScalarShape =>
        val copiedScalar = scalar.copy(fields = scalar.fields.filter(f => !ignored.contains(f._1)))
        RamlScalarShapeEmitter(copiedScalar, ordering).emitters()
      case array: ArrayShape =>
        val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
        RamlArrayShapeEmitter(copiedArray, ordering).emitters()
      case tuple: TupleShape =>
        val copiedTuple = tuple.copy(fields = tuple.fields.filter(f => !ignored.contains(f._1)))
        RamlTupleShapeEmitter(copiedTuple, ordering).emitters()
      case matrix: MatrixShape =>
        val copiedMatrix = matrix.copy(fields = matrix.fields.filter(f => !ignored.contains(f._1)))
        RamlArrayShapeEmitter(copiedMatrix.toArrayShape, ordering).emitters()
      case any: AnyShape =>
        val copiedNode = any.copy(fields = any.fields.filter(f => !ignored.contains(f._1)))
        RamlAnyShapeEmitter(copiedNode, ordering).emitters()
      case nil: NilShape =>
        val copiedNode = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
        RamlNilShapeEmitter(copiedNode, ordering).emitters()
      case _ => Seq()
    }
  }

  def entries(): Seq[EntryEmitter] = emitters() map { case e: EntryEmitter => e }
}

abstract class RamlShapeEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
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

    fs.entry(ShapeModel.Default).map(f => result += ValueEmitter("default", f))

    fs.entry(ShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

    fs.entry(ShapeModel.Documentation)
      .map(f =>
        result += OasEntryCreativeWorkEmitter("(externalDocs)", f.value.value.asInstanceOf[CreativeWork], ordering))

    fs.entry(ShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    result ++= AnnotationsEmitter(shape, ordering).emitters

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

case class RamlNodeShapeEmitter(node: NodeShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(node, ordering) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs                               = node.fields

    // TODO annotation for original position?
    if (node.annotations.contains(classOf[ExplicitField]))
      result += MapEntryEmitter("type", "object")

    fs.entry(NodeShapeModel.Inherits).map(f => result += RamlShapeInheritsEmitter(f, ordering))

    fs.entry(NodeShapeModel.MinProperties).map(f => result += ValueEmitter("minProperties", f))

    fs.entry(NodeShapeModel.MaxProperties).map(f => result += ValueEmitter("maxProperties", f))

    fs.entry(NodeShapeModel.Closed)
      .filter(_.value.annotations.contains(classOf[ExplicitField]))
      .map(
        f =>
          result += MapEntryEmitter("additionalProperties",
                                    (!node.closed).toString,
                                    position = pos(f.value.annotations)))

    fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("discriminator", f))

    fs.entry(NodeShapeModel.DiscriminatorValue).map(f => result += ValueEmitter("discriminatorValue", f))

    fs.entry(NodeShapeModel.ReadOnly).map(f => result += ValueEmitter("(readOnly)", f))

    fs.entry(NodeShapeModel.Properties).map(f => result += RamlPropertiesShapeEmitter(f, ordering))

    val propertiesMap = ListMap(node.properties.map(p => p.id -> p): _*)

    fs.entry(NodeShapeModel.Dependencies)
      .map(f => result += RamlShapeDependenciesEmitter(f, ordering, propertiesMap))

    result
  }
}

case class RamlShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    val (declaredShapes, inlineShapes) =
      f.array.values
        .map(_.asInstanceOf[Shape])
        .partition(s =>
          s.annotations.contains(classOf[DeclaredElement]) ||
            s.annotations.contains(classOf[ParsedFromTypeExpression]))

    b.entry(
      "type",
      b => {
        if (inlineShapes.nonEmpty) {
          b.obj(traverse(ordering.sorted(inlineShapes.flatMap(RamlTypeEmitter(_, ordering).entries())), _))
        } else {
          b.list { b =>
            declaredShapes.foreach {
              case shape: Shape if shape.annotations.contains(classOf[ParsedFromTypeExpression]) =>
                RamlTypeExpressionEmitter(shape).emit(b)
              case s => raw(b, s.name)
            }
          }
        }
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class RamlAnyShapeEmitter(shape: AnyShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(shape, ordering) {
  override def emitters(): Seq[EntryEmitter] = super.emitters() :+ MapEntryEmitter("type", "any")
}

case class RamlNilShapeEmitter(shape: NilShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(shape, ordering) {
  override def emitters(): Seq[EntryEmitter] = super.emitters() :+ MapEntryEmitter("type", "nil")
}

trait RamlCommonOASFieldsEmitter {
  def emitOASFields(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.ExclusiveMinimum).map(f => result += ValueEmitter("(exclusiveMinimum)", f))

    fs.entry(ScalarShapeModel.ExclusiveMaximum).map(f => result += ValueEmitter("(exclusiveMaximum)", f))

  }
}
case class RamlScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(scalar, ordering)
    with RamlCommonOASFieldsEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    val (typeDef, format) = RamlTypeDefStringValueMatcher.matchType(TypeDefXsdMapping.typeDef(scalar.dataType)) // TODO Check this

    fs.entry(ScalarShapeModel.DataType)
      .map(
        f =>
          result += MapEntryEmitter(
            "type",
            typeDef,
            position =
              if (f.value.annotations.contains(classOf[Inferred])) ZERO
              else
                pos(f.value.annotations))) // TODO check this  - annotations of typeDef in parser

    emitOASFields(fs, result)

    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern", f))

    fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("minimum", f))

    fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("maximum", f))

    fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("multipleOf", f))

    if (format.nonEmpty) result += MapEntryEmitter("(format)", format)
    else fs.entry(ScalarShapeModel.Format).map(f => result += ValueEmitter("format", f)) // todo mutually exclusive?

    result
  }
}

case class RamlFileShapeEmitter(scalar: FileShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(scalar, ordering)
    with RamlCommonOASFieldsEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    result += MapEntryEmitter("type", "file")

    emitOASFields(fs, result)

    fs.entry(FileShapeModel.FileTypes).map(f => result += ArrayEmitter("fileTypes", f, ordering))

    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("(pattern)", f))

    fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("(minimum)", f))

    fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("(maximum)", f))

    fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("(multipleOf)", f))

    result
  }
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

case class RamlUnionShapeEmitter(shape: UnionShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(shape, ordering) {
  override def emitters(): Seq[EntryEmitter] = {
    super.emitters() :+ MapEntryEmitter("type", "union") :+ RamlAnyOfShapeEmitter(shape, ordering)
  }
}

case class RamlAnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "anyOf",
      _.list { b =>
        val emitters = shape.anyOf.map(RamlTypePartEmitter(_, ordering, None))
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = pos(shape.fields.getValue(UnionShapeModel.AnyOf).annotations)
}

case class RamlArrayShapeEmitter(array: ArrayShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(array, ordering) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = array.fields

    if (array.annotations.contains(classOf[ExplicitField]))
      result += MapEntryEmitter("type", "array")

    result += RamlItemsShapeEmitter(array, ordering)

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    result
  }
}

case class RamlTupleShapeEmitter(tuple: TupleShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(tuple, ordering) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = tuple.fields

    if (tuple.annotations.contains(classOf[ExplicitField]))
      result += MapEntryEmitter("type", "array")

    result += RamlTupleItemsShapeEmitter(tuple, ordering)
    result += MapEntryEmitter("(tuple)", "true")

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    result
  }
}

case class RamlItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "items",
      //todo garrote review ordering
      _.obj(b => RamlTypeEmitter(array.items, ordering).entries().foreach(_.emit(b)))
    )
  }

  override def position(): Position = {
    pos(array.fields.getValue(ArrayShapeModel.Items).annotations)
  }
}

case class RamlTupleItemsShapeEmitter(tuple: TupleShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val result = mutable.ListBuffer[EntryEmitter]()

    tuple.items
      .foreach(item => {
        RamlTypeEmitter(item, ordering).entries().foreach(result += _)
      })

    //todo garrote review type
    /*b.entry(
      "items",
      _.list { b =>
        traverse(ordering.sorted(result), b)
      }
    )*/
  }

  override def position(): Position = pos(tuple.fields.getValue(ArrayShapeModel.Items).annotations)
}

case class RamlPropertiesShapeEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "properties",
      _.obj { b =>
        val result = f.array.values.map(v => RamlPropertyShapeEmitter(v.asInstanceOf[PropertyShape], ordering))
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class RamlPropertyShapeEmitter(property: PropertyShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      property.name,
      RamlTypePartEmitter(property.range, ordering, None).emit(_)
    )
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}

case class RamlSchemaEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val shape = f.value.value.asInstanceOf[Shape]
    b.entry(
      "type",
      RamlTypePartEmitter(shape, ordering, None).emit(_)
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasSchemaEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val shape = f.value.value.asInstanceOf[Shape]

    b.entry(
      "schema",
      OasTypePartEmitter(shape, ordering).emit(_)
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasTypePartEmitter(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field] = Nil)(
    implicit spec: SpecEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit =
    emitter match {
      case Left(p)        => p.emit(b)
      case Right(entries) => b.obj(traverse(entries, _))
    }

  override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)

  private val emitters = ordering.sorted(OasTypeEmitter(shape, ordering, ignored).emitters())

  private val emitter: Either[PartEmitter, Seq[EntryEmitter]] = emitters match {
    case Seq(p: PartEmitter)                           => Left(p)
    case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
    case other                                         => throw new Exception(s"IllegalTypeDeclarations found: $other")
  }
}

case class OasTypeEmitter(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field] = Nil)(
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
        OasNodeShapeEmitter(copiedNode, ordering).emitters()
      case union: UnionShape =>
        val copiedNode = union.copy(fields = union.fields.filter(f => !ignored.contains(f._1)))
        Seq(OasUnionShapeEmitter(copiedNode, ordering))
      case array: ArrayShape =>
        val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
        OasArrayShapeEmitter(copiedArray, ordering).emitters()
      case nil: NilShape =>
        val copiedNil = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
        Seq(OasNilShapeEmitter(copiedNil, ordering))
      case file: FileShape =>
        val copiedScalar = file.copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
        OasFileShapeEmitter(copiedScalar, ordering).emitters()
      case scalar: ScalarShape =>
        val copiedScalar = scalar.copy(fields = scalar.fields.filter(f => !ignored.contains(f._1)))
        OasScalarShapeEmitter(copiedScalar, ordering).emitters()
      case _ => Seq()
    }
  }

  def entries(): Seq[EntryEmitter] = emitters() map { case e: EntryEmitter => e }
}

abstract class OasShapeEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    fs.entry(ShapeModel.DisplayName).map(f => result += ValueEmitter("title", f))

    fs.entry(ShapeModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(ShapeModel.Default).map(f => result += ValueEmitter("default", f))

    fs.entry(ShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

    fs.entry(ShapeModel.Documentation)
      .map(f =>
        result += OasEntryCreativeWorkEmitter("externalDocs", f.value.value.asInstanceOf[CreativeWork], ordering))

    fs.entry(ShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result
  }
}

case class OasUnionShapeEmitter(shape: UnionShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    b.entry(
      "anyOf",
      _.list { b =>
        val emitters = shape.anyOf.map(OasTypePartEmitter(_, ordering))
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

case class OasArrayShapeEmitter(shape: ArrayShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    result += MapEntryEmitter("type", "array")

    result += OasItemsShapeEmitter(shape, ordering)

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    result ++= AnnotationsEmitter(shape, ordering).emitters

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

    result
  }
}

case class OasItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = b.entry("items", OasTypePartEmitter(array.items, ordering).emit(_))

  override def position(): Position = pos(array.items.fields.getValue(ArrayShapeModel.Items).annotations)
}

case class OasNodeShapeEmitter(node: NodeShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends OasShapeEmitter(node, ordering) {
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
      .map(
        f =>
          result += MapEntryEmitter("additionalProperties",
                                    (!node.closed).toString,
                                    position = pos(f.value.annotations)))

    fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("discriminator", f))

    fs.entry(NodeShapeModel.DiscriminatorValue).map(f => result += ValueEmitter("x-discriminator-value", f))

    fs.entry(NodeShapeModel.ReadOnly).map(f => result += ValueEmitter("readOnly", f))

    // TODO required array.

    fs.entry(NodeShapeModel.Properties).map(f => result += OasPropertiesShapeEmitter(f, ordering))

    val properties = ListMap(node.properties.map(p => p.id -> p): _*)

    fs.entry(NodeShapeModel.Dependencies).map(f => result += OasShapeDependenciesEmitter(f, ordering, properties))

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering))

    result
  }

}

case class OasShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val inherits = f.array.values.map(_.asInstanceOf[Shape])
    b.entry(
      "allOf",
      _.list(b =>
        inherits.foreach { s =>
          if (s.annotations.contains(classOf[DeclaredElement])) OasTypePartEmitter(s, ordering).emit(b)
          else spec.ref(b, OasDefinitions.appendDefinitionsPrefix(s.name))
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

case class OasScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends OasShapeEmitter(scalar, ordering)
    with OasCommonOASFieldsEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    val typeDef = OasTypeDefStringValueMatcher.matchType(TypeDefXsdMapping.typeDef(scalar.dataType)) // TODO Check this

    fs.entry(ScalarShapeModel.DataType)
      .map(
        f =>
          result += MapEntryEmitter(
            "type",
            typeDef,
            position =
              if (f.value.annotations.contains(classOf[Inferred])) ZERO
              else pos(f.value.annotations))) // TODO check this  - annotations of typeDef in parser

    emitCommonFields(fs, result)

    result
  }
}

case class OasFileShapeEmitter(scalar: FileShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends OasShapeEmitter(scalar, ordering)
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

case class OasPropertiesShapeEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    b.entry(
      "properties",
      _.obj { b =>
        val result = f.array.values.map(v => OasPropertyShapeEmitter(v.asInstanceOf[PropertyShape], ordering))
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasPropertyShapeEmitter(property: PropertyShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      property.name,
      OasTypePartEmitter(property.range, ordering).emit(_)
    )
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}
