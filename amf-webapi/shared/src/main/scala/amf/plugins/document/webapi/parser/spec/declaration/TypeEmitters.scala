package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.{ExplicitField, ExternalSource, SynthesizedField}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.metamodel.Field
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfScalar, Linkable, RecursiveShape, Shape}
import amf.core.parser.Position.ZERO
import amf.core.parser.{Annotations, FieldEntry, Fields, Position}
import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.contexts.{OasSpecEmitterContext, RamlScalarEmitter, SpecEmitterContext}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.domain.{MultipleExampleEmitter, SingleExampleEmitter}
import amf.plugins.document.webapi.parser.spec.raml.CommentEmitter
import amf.plugins.document.webapi.parser.{
  OasTypeDefMatcher,
  OasTypeDefStringValueMatcher,
  RamlTypeDefMatcher,
  RamlTypeDefStringValueMatcher
}
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models.TypeDef.UndefinedType
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YNode, YType}
import amf.core.utils.Strings

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
case class RamlNamedTypeEmitter(shape: AnyShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit] = Nil,
                                typesEmitter: (
                                    AnyShape,
                                    SpecOrdering,
                                    Option[AnnotationsEmitter],
                                    Seq[Field],
                                    Seq[BaseUnit]) => RamlTypePartEmitter)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = shape.name.option().getOrElse(throw new Exception(s"Cannot declare shape without name $shape"))
    b.entry(name, if (shape.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    shape.linkTarget.foreach { l =>
      spec.factory.tagToReferenceEmitter(l, shape.linkLabel, references).emit(b)
    }
  }

  private def emitInline(b: PartBuilder): Unit = shape match {
    case s: Shape with ShapeHelpers => typesEmitter(s, ordering, None, Seq(), references).emit(b)
    case _                          => throw new Exception("Cannot emit inline shape that doesnt support type expressions")
  }

  override def position(): Position = pos(shape.annotations)
}

case class OasNamedTypeEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = shape.name.option().getOrElse(throw new Exception(s"Cannot declare shape without name $shape"))
    b.entry(name, OasTypePartEmitter(shape, ordering, references = references).emit(_))
  }

  override def position(): Position = pos(shape.annotations)
}

case class Raml10TypePartEmitter(shape: AnyShape,
                                 ordering: SpecOrdering,
                                 annotations: Option[AnnotationsEmitter],
                                 ignored: Seq[Field] = Nil,
                                 references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends RamlTypePartEmitter(shape, ordering, annotations, ignored, references) {

  override def emitters: Seq[Emitter] =
    ordering.sorted(
      Raml10TypeEmitter(shape, ordering, ignored, references).emitters() ++ annotations.map(_.emitters).getOrElse(Nil))

}

object Raml08TypePartEmitter {
  def apply(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: SpecEmitterContext): Raml08TypePartEmitter =
    new Raml08TypePartEmitter(shape, ordering, None, Seq(), Seq())
}

case class Raml08TypePartEmitter(shape: AnyShape,
                                 ordering: SpecOrdering,
                                 annotations: Option[AnnotationsEmitter] = None,
                                 ignored: Seq[Field] = Nil,
                                 references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends RamlTypePartEmitter(shape, ordering, annotations, ignored, references) {
  override def emitters: Seq[Emitter] = Raml08TypeEmitter(shape, ordering).emitters()
}

abstract class RamlTypePartEmitter(shape: AnyShape,
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
        case Left(p)        => p.emit(b)
        case Right(entries) => b.obj(traverse(ordering.sorted(entries), _))
      }
    }
  }

  override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)

  protected def emitters: Seq[Emitter]

  val emitter: Either[PartEmitter, Seq[EntryEmitter]] = emitters match {
    case Seq(p: PartEmitter)                           => Left(p)
    case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
    case other                                         => throw new Exception(s"IllegalTypeDeclarations found: $other")
  }
}

case class RamlTypeExpressionEmitter(shape: Shape with ShapeHelpers) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = raw(b, shape.typeExpression)

  override def position(): Position = pos(shape.annotations)
}

case class Raml10TypeEmitter(shape: AnyShape,
                             ordering: SpecOrdering,
                             ignored: Seq[Field] = Nil,
                             references: Seq[BaseUnit])(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[Emitter] = {
    shape match {
      case _ if Option(shape).isDefined && shape.fromTypeExpression => Seq(RamlTypeExpressionEmitter(shape))
      case l: Linkable if l.isLink                                  => Seq(spec.localReference(shape))
      case schema: SchemaShape                                      => Seq(RamlSchemaShapeEmitter(schema, ordering, references))
      case node: NodeShape if node.annotations.find(classOf[ParsedJSONSchema]).isDefined =>
        Seq(RamlJsonShapeEmitter(node, ordering, references))
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
      case nil: NilShape =>
        val copiedNode = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
        RamlNilShapeEmitter(copiedNode, ordering, references).emitters()
      case any: AnyShape =>
        val copiedNode = any.copyAnyShape(fields = any.fields.filter(f => !ignored.contains(f._1)))
        RamlAnyShapeInstanceEmitter(copiedNode, ordering, references).emitters()

      case _ => Seq()
    }
  }

  def entries(): Seq[EntryEmitter] = emitters() collect {
    case e: EntryEmitter => e
    case p: PartEmitter =>
      new EntryEmitter {
        override def emit(b: EntryBuilder): Unit =
          b.entry(YNode("type"), (b) => p.emit(b))
        override def position(): Position = p.position()
      }
  }
}

abstract class RamlShapeEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext) {

  val typeName: Option[String]
  var typeEmitted = false

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

    fs.entry(ShapeModel.DisplayName).map(f => result += RamlScalarEmitter("displayName", f))
    fs.entry(ShapeModel.Description).map(f => result += RamlScalarEmitter("description", f))

    fs.entry(ShapeModel.Default) match {
      case Some(f) =>
        result += EntryPartEmitter("default",
                                   DataNodeEmitter(shape.default, ordering),
                                   position = pos(f.value.annotations))
      case None => fs.entry(ShapeModel.DefaultValueString).map(dv => result += ValueEmitter("default", dv))
    }

    fs.entry(ShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

    fs.entry(AnyShapeModel.Documentation)
      .map(
        f =>
          result += OasEntryCreativeWorkEmitter("externalDocs".asRamlAnnotation,
                                                f.value.value.asInstanceOf[CreativeWork],
                                                ordering))

    fs.entry(AnyShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    fs.entry(ShapeModel.CustomShapePropertyDefinitions)
      .map(f => {
        result += spec.factory.customFacetsEmitter(f, ordering, references)
      })

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    fs.entry(ShapeModel.Inherits)
      .fold(
        typeName.foreach { value =>
          spec.ramlTypePropertyEmitter(value, shape) match {
            case Some(emitter) =>
              typeEmitted = true
              result += emitter
            case None =>
              typeEmitted = false
          }
        }
      )(f => {
        typeEmitted = true
        result += RamlShapeInheritsEmitter(f, ordering, references = references)
      })

    result
  }
}

case class RamlJsonShapeEmitter(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends PartEmitter
    with ExamplesEmitter {

  override def emit(b: PartBuilder): Unit = {
    shape.annotations.find(classOf[ParsedJSONSchema]) match {
      case Some(json) =>
        if (shape.examples.nonEmpty) {
          val results = mutable.ListBuffer[EntryEmitter]()
          emitExamples(shape, results, ordering, references)
          results += MapEntryEmitter("type", json.rawText)
          b.obj(traverse(ordering.sorted(results), _))
        } else {
          raw(b, json.rawText)
        }
      case None => // Ignore
    }
  }

  override def position(): Position = {
    pos(shape.annotations)
  }
}

case class RamlSchemaShapeEmitter(shape: SchemaShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    shape.annotations.find(classOf[ExternalSource]) match {
      case Some(externalSource) =>
        spec.ref(b, externalSource.oriLabel)
      case _ =>
        if (shape.examples.nonEmpty) {
          val fs     = shape.fields
          val result = mutable.ListBuffer[EntryEmitter]()
          result ++= RamlAnyShapeEmitter(shape, ordering, references).emitters()
          fs.entry(SchemaShapeModel.Raw).foreach { f =>
            result += ValueEmitter("type", f)
          }
          b.obj(traverse(ordering.sorted(result), _))
        } else {
          raw(b, shape.raw.value())
        }
    }
  }

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

case class RamlRecursiveShapeEmitter(shape: RecursiveShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer()
    result += MapEntryEmitter("type", "object")
    result += MapEntryEmitter("recursive".asOasExtension, shape.fixpoint.value())
    result
  }
}

case class RamlNodeShapeEmitter(node: NodeShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlAnyShapeEmitter(node, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs                               = node.fields

    fs.entry(NodeShapeModel.MinProperties).map(f => result += RamlScalarEmitter("minProperties", f))
    fs.entry(NodeShapeModel.MaxProperties).map(f => result += RamlScalarEmitter("maxProperties", f))

    fs.entry(NodeShapeModel.Closed)
      .foreach { f =>
        val closed = node.closed.value()
        if (closed || f.value.annotations.contains(classOf[ExplicitField])) {
          result += MapEntryEmitter("additionalProperties", (!closed).toString, position = pos(f.value.annotations))
        }
      }

    fs.entry(NodeShapeModel.AdditionalPropertiesSchema)
      .map(
        f =>
          result += OasEntryShapeEmitter("additionalProperties".asRamlAnnotation, f, ordering, references)(
            amf.plugins.document.webapi.parser.spec.toOas(spec)))

    fs.entry(NodeShapeModel.Discriminator).map(f => result += RamlScalarEmitter("discriminator", f))
    fs.entry(NodeShapeModel.DiscriminatorValue).map(f => result += RamlScalarEmitter("discriminatorValue", f))

    fs.entry(NodeShapeModel.Properties).map { f =>
      typeEmitted = true
      result += RamlPropertiesShapeEmitter(f, ordering, references)
    }

    val propertiesMap = ListMap(node.properties.map(p => p.id -> p): _*)

    fs.entry(NodeShapeModel.Dependencies)
      .map(f => result += RamlShapeDependenciesEmitter(f, ordering, propertiesMap))

    if (!typeEmitted)
      result += MapEntryEmitter("type", "object")

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
              ordering.sorted(inlineShapes.flatMap {
                case s: AnyShape => Raml10TypeEmitter(s, ordering, references = references).entries()
                case _           => throw new Exception("Cannot emit for type shapes without WebAPI Shape support")
              }),
              _
            ))
        } else {
          declaredShapes match {
            case (head: Shape with ShapeHelpers) :: Nil =>
              emitDeclared(head, b)
            case _ =>
              b.list { b =>
                declaredShapes.foreach {
                  case s: Shape with ShapeHelpers => emitDeclared(s, b)
                }
              }

          }
        }
      }
    )
  }

  private def emitDeclared(shape: Shape with ShapeHelpers, b: PartBuilder): Unit = shape match {
    case shape: Shape if shape.annotations.contains(classOf[ParsedFromTypeExpression]) =>
      RamlTypeExpressionEmitter(shape).emit(b)
    case s: Shape =>
      if (s.isLink) spec.localReference(s).emit(b)
      else raw(b, s.name.value())
  }

  override def position(): Position = pos(f.value.annotations)
}

object RamlAnyShapeEmitter {
  def apply(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: SpecEmitterContext): RamlAnyShapeEmitter =
    new RamlAnyShapeEmitter(shape, ordering, references)(spec)
}

trait ExamplesEmitter {
  def emitExamples(shape: AnyShape,
                   results: ListBuffer[EntryEmitter],
                   ordering: SpecOrdering,
                   references: Seq[BaseUnit])(implicit spec: SpecEmitterContext): Unit = {
    shape.fields
      .entry(AnyShapeModel.Examples)
      .map(f => {
        val (anonymous, named) =
          shape.examples.partition(e => !e.fields.fieldsMeta().contains(ExampleModel.Name) && !e.isLink)
        val examples = f.array.values.collect({ case e: Example => e })
        anonymous.headOption.foreach { a =>
          results += SingleExampleEmitter("example", a, ordering)
        }
        results += MultipleExampleEmitter("examples",
                                          named ++ (if (anonymous.lengthCompare(1) > 0) examples.tail else None),
                                          ordering,
                                          references)
      })
  }
}

class RamlAnyShapeEmitter(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(shape, ordering, references)
    with ExamplesEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    var results = ListBuffer(super.emitters(): _*)

    emitExamples(shape, results, ordering, references)

    results
  }

  override val typeName: Option[String] = Some("any")
}

object RamlAnyShapeInstanceEmitter {
  def apply(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: SpecEmitterContext): RamlAnyShapeInstanceEmitter =
    new RamlAnyShapeInstanceEmitter(shape, ordering, references)(spec)
}

class RamlAnyShapeInstanceEmitter(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlShapeEmitter(shape, ordering, references)
    with ExamplesEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    var results = ListBuffer(super.emitters(): _*)

    emitExamples(shape, results, ordering, references)

    if (!typeEmitted) {
      val entry = MapEntryEmitter("type", "any")
      results ++= Seq(entry)
    }

    results
  }

  override val typeName: Option[String] = Some("any")
}

case class RamlNilShapeEmitter(shape: NilShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlAnyShapeEmitter(shape, ordering, references) {

  override def emitters(): Seq[EntryEmitter] = {
    var result: Seq[EntryEmitter] = super.emitters()
    if (!typeEmitted) {
      val entry = MapEntryEmitter("type", "nil")
      result = result ++ Seq(entry)
    }
    result
  }

  override val typeName: Option[String] = Some("nil")
}

trait RamlCommonOASFieldsEmitter {
  def emitOASFields(fs: Fields, result: ListBuffer[EntryEmitter])(implicit spec: SpecEmitterContext): Unit = {
    fs.entry(ScalarShapeModel.MinLength).map(f => result += RamlScalarEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += RamlScalarEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.ExclusiveMinimum)
      .map(f => result += ValueEmitter("exclusiveMinimum".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.ExclusiveMaximum)
      .map(f => result += ValueEmitter("exclusiveMaximum".asRamlAnnotation, f))
  }
}
case class RamlScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlAnyShapeEmitter(scalar, ordering, references)
    with RamlCommonOASFieldsEmitter {

  override def emitters(): Seq[EntryEmitter] = {
    val fs = scalar.fields

    val rawTypeDef        = TypeDefXsdMapping.typeDef(scalar.dataType.value())
    val (typeDef, format) = RamlTypeDefStringValueMatcher.matchType(rawTypeDef, scalar.format.option())

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

    fs.entry(ScalarShapeModel.Pattern).map(f => result += RamlScalarEmitter("pattern", f))

    fs.entry(ScalarShapeModel.Minimum)
      .map(f => result += ValueEmitter("minimum", f, Some(NumberTypeToYTypeConverter.convert(rawTypeDef))))

    fs.entry(ScalarShapeModel.Maximum)
      .map(f => result += ValueEmitter("maximum", f, Some(NumberTypeToYTypeConverter.convert(rawTypeDef))))

    fs.entry(ScalarShapeModel.MultipleOf)
      .map(f => result += RamlScalarEmitter("multipleOf", f, Some(NumberTypeToYTypeConverter.convert(rawTypeDef))))

    result ++= emitFormat(rawTypeDef, fs, format)

    result
  }

  def emitFormat(rawTypeDef: TypeDef, fs: Fields, format: String): Option[EntryEmitter] = {
    val formatKey =
      if (rawTypeDef.isNumber | rawTypeDef.isDate) "format"
      else "format".asRamlAnnotation

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
      Some(RawValueEmitter(formatKey, ScalarShapeModel.Format, finalFormat, annotations))
    } else if (finalFormat.nonEmpty && (finalFormat == "float" || finalFormat == "int32") && explictFormatFound) {
      // we always mapping 'number' in RAML to xsd:float, if we are to emit 'float'
      // as the format must be because it has been explicitly set in this way, not because
      // we are adding that through the number -> xsd:float mapping
      Some(RawValueEmitter(formatKey, ScalarShapeModel.Format, finalFormat, annotations))
    } else {
      None
    }
  }

  override val typeName: Option[String] = None //exceptional case for get the type (scalar) and format
}

case class RamlFileShapeEmitter(scalar: FileShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlAnyShapeEmitter(scalar, ordering, references)
    with RamlCommonOASFieldsEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    emitOASFields(fs, result)

    fs.entry(FileShapeModel.FileTypes).map(f => result += ArrayEmitter("fileTypes", f, ordering))

    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("minimum".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("maximum".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("multipleOf".asRamlAnnotation, f))

    if (result.isEmpty || (result.size == 1 && scalar.fields.?(AnyShapeModel.Examples).nonEmpty))
      result += MapEntryEmitter("type", "file")

    result
  }

  override val typeName: Option[String] = Some("file")
}

case class RamlShapeDependenciesEmitter(f: FieldEntry, ordering: SpecOrdering, props: ListMap[String, PropertyShape])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "dependencies".asRamlAnnotation,
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
      .get(property.propertySource.value())
      .foreach(p => {
        b.entry(
          p.name.value(),
          b => {
            val targets = property.fields
              .entry(PropertyDependenciesModel.PropertyTarget)
              .map(f => {
                f.array.scalars.flatMap(iri =>
                  properties.get(iri.value.toString).map(p => AmfScalar(p.name.value(), iri.annotations)))
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
    extends RamlAnyShapeEmitter(shape, ordering, references) {
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
        val emitters = shape.anyOf.map {
          case s: AnyShape =>
            Raml10TypePartEmitter(s, ordering, None, references = references)
        }
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = pos(shape.fields.getValue(UnionShapeModel.AnyOf).annotations)
}

case class RamlArrayShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlAnyShapeEmitter(array, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = array.fields

    fs.entry(ArrayShapeModel.Items)
      .foreach(_ => {
        typeEmitted = true
        result += RamlItemsShapeEmitter(array, ordering, references)
      })

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += RamlScalarEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += RamlScalarEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += RamlScalarEmitter("uniqueItems", f))

    fs.entry(ArrayShapeModel.CollectionFormat).map(f => result += ValueEmitter("collectionFormat".asRamlAnnotation, f))

    if (!typeEmitted)
      result += MapEntryEmitter("type", "array")

    result
  }

  override val typeName: Option[String] = array.annotations.find(classOf[ExplicitField]).map(_ => "array")
}

case class RamlTupleShapeEmitter(tuple: TupleShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlAnyShapeEmitter(tuple, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = tuple.fields

    result += RamlTupleItemsShapeEmitter(tuple, ordering, references)
    result += MapEntryEmitter("tuple".asRamlAnnotation, "true")

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += RamlScalarEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += RamlScalarEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += RamlScalarEmitter("uniqueItems", f))

    result
  }

  override val typeName: Option[String] = tuple.annotations.find(classOf[ExplicitField]).map(_ => "array")
}

case class RamlItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    array.items match {
      case webapiArrayItem: AnyShape =>
        if (webapiArrayItem.isLink) {
          b.entry("items", Raml10TypePartEmitter(webapiArrayItem, ordering, None, Nil, references).emit(_))
        } else {
          // todo garrote review ordering
          b.entry(
            "items",
            _.obj { b =>
              Raml10TypeEmitter(webapiArrayItem, ordering, references = references).entries().foreach(_.emit(b))
            }
          )
        }
      case _ => // ignore
    }
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
    tuple.items match {
      case tupleItems: AnyShape =>
        tuple.items
          .foreach(item => {
            Raml10TypeEmitter(tupleItems, ordering, references = references).entries().foreach(result += _)
          })
      case _ => // ignore
    }

    // todo garrote review type
    /* b.entry(
      "items",
      _.list { b =>
        traverse(ordering.sorted(result), b)
      }
    ) */
  }

  override def position(): Position = pos(tuple.fields.getValue(TupleShapeModel.TupleItems).annotations)
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
          property.name.value() + "?"
        else property.name.value()
      })
      .getOrElse(property.name.value())

    if (property.range.annotations.contains(classOf[SynthesizedField])) {
      b.entry(
        name,
        raw(_, "", YType.Null)
      )
    } else {

      val readOnlyEmitter: Option[ValueEmitter] =
        property.fields.entry(PropertyShapeModel.ReadOnly).map(fe => ValueEmitter("readOnly".asRamlAnnotation, fe))

      property.range match {
        case range: AnyShape =>
          b.entry(
            name,
            pb => {
              Raml10TypePartEmitter(range, ordering, None, references = references).emitter match {
                case Left(p)        => p.emit(pb)
                case Right(entries) => pb.obj(traverse(ordering.sorted(entries ++ readOnlyEmitter), _))
              }
            }
          )
        case _ => // ignreo
          b.entry(name, _.obj(e => traverse(readOnlyEmitter.toSeq, e)))
      }
    }
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}

case class RamlSchemaEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    f.value.value match {
      case shape: AnyShape =>
        b.entry(
          "type",
          Raml10TypePartEmitter(shape, ordering, None, references = references).emit(_)
        )
      case _ => // ignore?
    }
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasSchemaEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
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
                              references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends OasTypePartCollector(shape, ordering, ignored, references)
    with PartEmitter {

  override def emit(b: PartBuilder): Unit =
    emitter match {
      case Left(p)        => p.emit(b)
      case Right(entries) => b.obj(traverse(entries, _))
    }

  override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)

}

abstract class OasTypePartCollector(shape: Shape,
                                    ordering: SpecOrdering,
                                    ignored: Seq[Field],
                                    references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext) {
  protected val emitters: Seq[Emitter] =
    ordering.sorted(OasTypeEmitter(shape, ordering, ignored, references).emitters())

  protected val emitter: Either[PartEmitter, Seq[EntryEmitter]] = emitters match {
    case Seq(p: PartEmitter)                           => Left(p)
    case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
    case other                                         => throw new Exception(s"IllegalTypeDeclarations found: $other")
  }
}

case class OasTypeEmitter(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field] = Nil, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext) {
  def emitters(): Seq[Emitter] = {
    shape match {
      case l: Linkable if l.isLink => Seq(OasTagToReferenceEmitter(shape, l.linkLabel, Nil))
      case schema: SchemaShape =>
        val copiedNode = schema.copy(fields = schema.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasSchemaShapeEmitter(copiedNode, ordering).emitters()
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
      case any: AnyShape =>
        val copiedNode = any.copyAnyShape(fields = any.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasAnyShapeEmitter(copiedNode, ordering, references).emitters()
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
    fs.entry(ShapeModel.Default) match {
      case Some(f) =>
        result += EntryPartEmitter("default",
                                   DataNodeEmitter(shape.default, ordering),
                                   position = pos(f.value.annotations))
      case None => fs.entry(ShapeModel.DefaultValueString).map(dv => result += ValueEmitter("default", dv))
    }

    fs.entry(ShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

    fs.entry(AnyShapeModel.Documentation)
      .map(f =>
        result += OasEntryCreativeWorkEmitter("externalDocs", f.value.value.asInstanceOf[CreativeWork], ordering))

    fs.entry(AnyShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    fs.entry(ShapeModel.CustomShapePropertyDefinitions)
      .map(f => {
        result += spec.factory.customFacetsEmitter(f, ordering, references)
      })

    result
  }
}

case class OasUnionShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
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

object OasAnyShapeEmitter {
  def apply(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: SpecEmitterContext): OasAnyShapeEmitter =
    new OasAnyShapeEmitter(shape, ordering, references)(spec)
}

class OasAnyShapeEmitter(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends OasShapeEmitter(shape, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    shape.fields
      .entry(AnyShapeModel.Examples)
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

    super.emitters() ++ result
  }

  private def examplesEmitters(main: Option[Example], extentions: Seq[Example]) = {
    val em = ListBuffer[EntryEmitter]()
    main.foreach(a => em += SingleExampleEmitter("example", a, ordering))
    if (extentions.nonEmpty)
      em += MultipleExampleEmitter("examples".asOasExtension, extentions, ordering, references)
    em
  }
}

case class OasArrayShapeEmitter(shape: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    result += spec.oasTypePropertyEmitter("array", shape)

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    fs.entry(ArrayShapeModel.CollectionFormat) match { // What happens if there is an array of an array with collectionFormat?
      case Some(f) if f.value.annotations.contains(classOf[CollectionFormatFromItems]) =>
        result += OasItemsShapeEmitter(shape, ordering, references, Some(ValueEmitter("collectionFormat", f)))
      case Some(f) =>
        result += OasItemsShapeEmitter(shape, ordering, references, None) += ValueEmitter("collectionFormat", f)
      case None =>
        result += OasItemsShapeEmitter(shape, ordering, references, None)
    }

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    result
  }
}

case class OasSchemaShapeEmitter(shape: SchemaShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    result += spec.oasTypePropertyEmitter("object", shape)

    fs.entry(SchemaShapeModel.MediaType).map(f => result += ValueEmitter("mediaType".asOasExtension, f))

    fs.entry(SchemaShapeModel.Raw).map(f => result += ValueEmitter("schema".asOasExtension, f))

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    result
  }
}

case class OasItemsShapeEmitter(array: ArrayShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                additionalEntry: Option[ValueEmitter])(implicit spec: OasSpecEmitterContext)
    extends OasTypePartCollector(array.items, ordering, Nil, references)
    with EntryEmitter {

  def emit(b: EntryBuilder): Unit = {
    b.entry("items", b => emitPart(b))
  }

  def emitPart(part: PartBuilder): Unit = {
    emitter match {
      case Left(p)        => p.emit(part) // What happens if additionalProperty is defined and is not an Seq?
      case Right(entries) => part.obj(traverse(entries ++ additionalEntry, _))
    }
  }

  override def position(): Position = {
    Option(array.fields.getValue(ArrayShapeModel.Items)) match {
      case Some(value) => pos(value.annotations)
      case _           => ZERO
    }
  }
}

case class OasNodeShapeEmitter(node: NodeShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends OasAnyShapeEmitter(node, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = node.fields

    result += spec.oasTypePropertyEmitter("object", node)

    fs.entry(NodeShapeModel.MinProperties).map(f => result += ValueEmitter("minProperties", f))

    fs.entry(NodeShapeModel.MaxProperties).map(f => result += ValueEmitter("maxProperties", f))

    fs.entry(NodeShapeModel.Closed)
      .filter(_.value.annotations.contains(classOf[ExplicitField])) match {
      case Some(f) => result += ValueEmitter("additionalProperties", f.negated)
      case _ =>
        fs.entry(NodeShapeModel.AdditionalPropertiesSchema)
          .map(f => result += OasEntryShapeEmitter("additionalProperties", f, ordering, references))
    }

    fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("discriminator", f))

    fs.entry(NodeShapeModel.DiscriminatorValue)
      .map(f => result += ValueEmitter("discriminatorValue".asOasExtension, f))

    fs.entry(NodeShapeModel.Properties).map(f => result += OasRequiredPropertiesShapeEmitter(f, references))

    fs.entry(NodeShapeModel.Properties).map(f => result += OasPropertiesShapeEmitter(f, ordering, references))

    val properties = ListMap(node.properties.map(p => p.id -> p): _*)

    fs.entry(NodeShapeModel.Dependencies).map(f => result += OasShapeDependenciesEmitter(f, ordering, properties))

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering, references))

    result
  }
}

case class OasEntryShapeEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj { b =>
        val emitters = OasTypeEmitter(f.element.asInstanceOf[Shape], ordering, references = references).entries()
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val inherits = f.array.values.map(_.asInstanceOf[Shape])
    b.entry(
      "allOf",
      _.list(b =>
        inherits.foreach { s =>
          if (s.annotations.contains(classOf[DeclaredElement]))
            spec.ref(b, OasDefinitions.appendDefinitionsPrefix(s.name.value()))
          else if (s.linkTarget.isDefined)
            spec.ref(b, OasDefinitions.appendDefinitionsPrefix(s.name.value()))
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
      .get(property.propertySource.value())
      .foreach(p => {
        b.entry(
          p.name.value(),
          _.list { b =>
            val targets = property.fields
              .entry(PropertyDependenciesModel.PropertyTarget)
              .map(f => {
                f.array.scalars.flatMap(iri =>
                  properties.get(iri.value.toString).map(p => AmfScalar(p.name.value(), iri.annotations)))
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

  def typeDef: Option[TypeDef] = None

  def emitCommonFields(fs: Fields, result: ListBuffer[EntryEmitter]): Option[result.type] = {

    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern", f))

    fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.Minimum)
      .map(f => result += ValueEmitter("minimum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

    fs.entry(ScalarShapeModel.Maximum)
      .map(f => result += ValueEmitter("maximum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

    fs.entry(ScalarShapeModel.ExclusiveMinimum).map(f => result += ValueEmitter("exclusiveMinimum", f))

    fs.entry(ScalarShapeModel.ExclusiveMaximum).map(f => result += ValueEmitter("exclusiveMaximum", f))

    fs.entry(ScalarShapeModel.MultipleOf)
      .map(f => result += ValueEmitter("multipleOf", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

    fs.entry(ScalarShapeModel.Format).map(f => result += ValueEmitter("format", f))
  }
}

case class OasScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends OasAnyShapeEmitter(scalar, ordering, references)
    with OasCommonOASFieldsEmitter {

  override def typeDef: Option[TypeDef] = scalar.dataType.option().map(TypeDefXsdMapping.typeDef)

  override def emitters(): Seq[EntryEmitter] = {

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs                               = scalar.fields

    fs.entry(ScalarShapeModel.DataType)
      .foreach { f =>
        if (!f.value.annotations.contains(classOf[Inferred])) {
          val typeDefStr = OasTypeDefStringValueMatcher.matchType(typeDef.get)
          scalar.annotations.find(classOf[TypePropertyLexicalInfo]) match {
            case Some(lexicalInfo) =>
              result += MapEntryEmitter("type", typeDefStr, YType.Str, lexicalInfo.range.start)
            case _ =>
              result += MapEntryEmitter("type", typeDefStr, position = pos(f.value.annotations)) // TODO check this  - annotations of typeDef in parser
          }
        }
      }

    fs.entry(ScalarShapeModel.Format) match {
      case Some(_) => // ignore, this will be set with the explicit information
      case None =>
        OasTypeDefStringValueMatcher.matchFormat(typeDef.getOrElse(UndefinedType)) match {
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
    extends OasAnyShapeEmitter(scalar, ordering, references)
    with OasCommonOASFieldsEmitter {

  override def typeDef: Option[TypeDef] = None

  override def emitters(): Seq[EntryEmitter] = {

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    result += spec.oasTypePropertyEmitter("file", scalar)

    emitCommonFields(fs, result)

    fs.entry(FileShapeModel.FileTypes).map(f => result += ArrayEmitter("fileTypes".asOasExtension, f, ordering))

    result
  }
}

case class OasRequiredPropertiesShapeEmitter(f: FieldEntry, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val requiredProperties = f.array.values.filter {
      case property: PropertyShape => property.minCount.value() > 0
      case _                       => false
    }
    if (requiredProperties.nonEmpty) {
      b.entry(
        "required",
        _.list { b =>
          requiredProperties.foreach {
            case property: PropertyShape =>
              TextScalarEmitter(property.name.value(), Annotations()).emit(b)
          }
        }
      )
    }
  }

  override def position(): Position = pos(f.value.annotations)
}

case class OasPropertiesShapeEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
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
    implicit spec: OasSpecEmitterContext)
    extends OasTypePartCollector(property.range, ordering, Nil, references)
    with EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val readOnlyEmitter: Option[ValueEmitter] =
      property.fields.entry(PropertyShapeModel.ReadOnly).map(fe => ValueEmitter("readOnly", fe))

    property.range match {
      case range: AnyShape =>
        b.entry(
          property.name.value(),
          pb => {
            emitter match {
              case Left(p)        => p.emit(pb)
              case Right(entries) => pb.obj(traverse(ordering.sorted(entries ++ readOnlyEmitter), _))
            }
          }
        )
      case _ => // ignreo
        b.entry(property.name.value(), _.obj(e => traverse(readOnlyEmitter.toSeq, e)))
    }
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}

case class Raml08TypeEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[Emitter] = {
    shape match {
      case shape: Shape if shape.isLink => Seq(spec.localReference(shape))
      case scalar: ScalarShape =>
        SimpleTypeEmitter(scalar, ordering).emitters()
      case array: ArrayShape =>
        array.items match {
          case sc: ScalarShape =>
            SimpleTypeEmitter(sc, ordering).emitters()
          case other =>
            Seq(CommentEmitter(other, s"Cannot emit array shape with items ${other.getClass.toString} in raml 08"))
        }
      case union: UnionShape =>
        Seq(new PartEmitter {
          override def emit(b: PartBuilder): Unit = {
            b.list(b => {
              union.anyOf
                .collect({ case s: AnyShape => s })
                .foreach(s => {
                  Raml08TypePartEmitter(s, ordering, Seq()).emit(b)
                })
            })
          }

          override def position(): Position = pos(union.annotations)
        })
      case schema: SchemaShape => Seq(RamlSchemaShapeEmitter(schema, ordering, Nil))
      case shape: AnyShape if shape.annotations.find(classOf[ParsedJSONSchema]).isDefined =>
        Seq(RamlJsonShapeEmitter(shape, ordering, Nil))
      case nil: NilShape =>
        RamlNilShapeEmitter(nil, ordering, Seq()).emitters()
      case shape: AnyShape =>
        RamlAnyShapeEmitter(shape, ordering, Nil).emitters()
      case other =>
        Seq(CommentEmitter(other, s"Unsupported shape class for emit raml 08 spec ${other.getClass.toString}`"))
    }
  }

}

case class SimpleTypeEmitter(shape: ScalarShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {

  def emitters(): Seq[EntryEmitter] = {
    val fs = shape.fields

    val result = ListBuffer[EntryEmitter]()
    fs.entry(ScalarShapeModel.DisplayName).map(f => result += ValueEmitter("displayName", f))

    val typeDef = shape.dataType.option().map(TypeDefXsdMapping.type08Def)

    fs.entry(ScalarShapeModel.DataType)
      .map { f =>
        val rawTypeDef = TypeDefXsdMapping.typeDef08(shape.dataType.value())
        shape.annotations.find(classOf[TypePropertyLexicalInfo]) match {
          case Some(lexicalInfo) =>
            result += MapEntryEmitter("type", rawTypeDef, position = lexicalInfo.range.start)
          case _ =>
            result += MapEntryEmitter("type", rawTypeDef, position = pos(f.value.annotations))
        }
      }

    fs.entry(ScalarShapeModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(ScalarShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern", f))

    fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.Minimum)
      .map(f => result += ValueEmitter("minimum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

    fs.entry(ScalarShapeModel.Maximum)
      .map(f => result += ValueEmitter("maximum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

    shape.examples.headOption.foreach(e => result += SingleExampleEmitter("example", e, ordering))
    fs.entry(ScalarShapeModel.RequiredShape).map(f => result += ValueEmitter("required", f))

    fs.entry(ShapeModel.Default)
      .map(f => {
        result += EntryPartEmitter("default",
                                   DataNodeEmitter(shape.default, ordering),
                                   position = pos(f.value.annotations))
      })

    result
  }

}

object NumberTypeToYTypeConverter {

  def convert(datatype: TypeDef): YType = {
    datatype match {
      case TypeDef.IntType => YType.Int
      case _               => YType.Float
    }
  }

  def convert(datatype: Option[TypeDef]): YType = {
    this.convert(datatype.getOrElse(TypeDef.UndefinedType))
  }
}
