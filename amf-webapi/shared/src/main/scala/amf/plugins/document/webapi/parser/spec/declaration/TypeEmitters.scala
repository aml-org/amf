package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations._
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Bool
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.model.DataType
import amf.core.model.document.{BaseUnit, EncodesModel, ExternalFragment}
import amf.core.model.domain._
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Position.ZERO
import amf.core.parser.{Annotations, FieldEntry, Fields, Position, Value}
import amf.core.remote.Vendor
import amf.core.utils.AmfStrings
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.domain.{MultipleExampleEmitter, SingleExampleEmitter}
import amf.plugins.document.webapi.parser.spec.raml.CommentEmitter
import amf.plugins.document.webapi.parser.{OasTypeDefMatcher, RamlTypeDefMatcher, RamlTypeDefStringValueMatcher}
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models.TypeDef._
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.{TypeDefXsdMapping, TypeDefYTypeMapping, XsdTypeDefMapping}
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import amf.plugins.domain.webapi.metamodel.IriTemplateMappingModel
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YNode, YScalar, YType}

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
    val name = shape.name.option().getOrElse("schema") // this used to throw an exception, but with the resolution optimizacion, we use the father shape, so it could have not name (if it's from an endpoint for example, and you want to write a new single shape, like a json schema)
    b.entry(name, if (shape.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    shape.linkTarget.foreach { l =>
      spec.factory.tagToReferenceEmitter(l, shape.linkLabel.option(), references).emit(b)
    }
  }

  private def emitInline(b: PartBuilder): Unit = shape match {
    case s: Shape with ShapeHelpers => typesEmitter(s, ordering, None, Seq(), references).emit(b)
    case _ =>
      spec.eh.violation(
        ResolutionValidation,
        shape.id,
        None,
        "Cannot emit inline shape that doesnt support type expressions",
        shape.position(),
        shape.location()
      )
  }

  override def position(): Position = pos(shape.annotations)
}

case class OasNamedTypeEmitter(shape: Shape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               pointer: Seq[String] = Nil)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = shape.name.option().getOrElse("schema") // this used to throw an exception, but with the resolution optimizacion, we use the father shape, so it could have not name (if it's from an endpoint for example, and you want to write a new single shape, like a json schema)
    b.entry(name, OasTypePartEmitter(shape, ordering, references = references, pointer = pointer :+ name).emit(_))
  }

  override def position(): Position = pos(shape.annotations)
}

case class Raml10TypePartEmitter(shape: Shape,
                                 ordering: SpecOrdering,
                                 annotations: Option[AnnotationsEmitter],
                                 ignored: Seq[Field] = Nil,
                                 references: Seq[BaseUnit])(implicit spec: RamlSpecEmitterContext)
    extends RamlTypePartEmitter(shape, ordering, annotations, ignored, references) {

  override def emitters: Seq[Emitter] =
    ordering.sorted(
      Raml10TypeEmitter(shape, ordering, ignored, references).emitters() ++ annotations.map(_.emitters).getOrElse(Nil))

}

object Raml08TypePartEmitter {
  def apply(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: RamlSpecEmitterContext): Raml08TypePartEmitter =
    new Raml08TypePartEmitter(shape, ordering, None, Seq(), Seq())
}

case class Raml08TypePartEmitter(shape: Shape,
                                 ordering: SpecOrdering,
                                 annotations: Option[AnnotationsEmitter] = None,
                                 ignored: Seq[Field] = Nil,
                                 references: Seq[BaseUnit])(implicit spec: RamlSpecEmitterContext)
    extends RamlTypePartEmitter(shape, ordering, annotations, ignored, references) {
  override def emitters: Seq[Emitter] = Raml08TypeEmitter(shape, ordering).emitters()
}

abstract class RamlTypePartEmitter(shape: Shape,
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
    case other =>
      spec.eh.violation(ResolutionValidation,
                        shape.id,
                        None,
                        s"IllegalTypeDeclarations found: $other",
                        shape.position(),
                        shape.location())
      Right(Nil)
  }
}

case class RamlTypeExpressionEmitter(shape: Shape with ShapeHelpers)(implicit spec: SpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = raw(b, shape.typeExpression(spec.eh))

  override def position(): Position = pos(shape.annotations)
}

case class RamlExternalSourceEmitter(shape: Shape with ShapeHelpers, references: Seq[BaseUnit]) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    references
      .collectFirst({
        case ex: ExternalFragment if ex.encodes.id.equals(shape.externalSourceID.getOrElse("")) => ex.encodes
      })
      .flatMap(ex => ex.raw.option())
      .foreach { raw(b, _) }
  }

  override def position(): Position = pos(shape.annotations)
}

case class Raml10TypeEmitter(shape: Shape,
                             ordering: SpecOrdering,
                             ignored: Seq[Field] = Nil,
                             references: Seq[BaseUnit])(implicit spec: RamlSpecEmitterContext) {
  def emitters(): Seq[Emitter] = {
    shape match {
      case _
          if Option(shape).isDefined && shape.isInstanceOf[AnyShape]
            && shape.asInstanceOf[AnyShape].fromExternalSource
            && references.nonEmpty
            && references
              .collectFirst({
                case e: ExternalFragment
                    if e.encodes.id.equals(shape.asInstanceOf[AnyShape].externalSourceID.getOrElse("")) =>
                  e
              })
              .isDefined => // need to check ref to ask if resolution has run.
        Seq(RamlExternalSourceEmitter(shape.asInstanceOf[AnyShape], references))
//      case _
//          if Option(shape).isDefined && shape
//            .isInstanceOf[AnyShape] && shape.asInstanceOf[AnyShape].fromTypeExpression =>
//        Seq(RamlTypeExpressionEmitter(shape.asInstanceOf[AnyShape]))
      case l: Linkable if l.isLink =>
        spec.externalLink(shape, references) match {
          case Some(fragment: EncodesModel) =>
            Seq(spec.externalReference(shape.linkLabel.option().getOrElse(fragment.location().get), shape))
          case _ =>
            Seq(spec.localReference(shape))
        }
      case schema: SchemaShape => Seq(RamlSchemaShapeEmitter(schema, ordering, references))
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
      case rec: RecursiveShape =>
        RamlRecursiveShapeEmitter(rec, ordering, references).emitters()

      case _ => Seq()
    }
  }

  def entries(): Seq[EntryEmitter] = emitters() collect {
    case e: EntryEmitter => e
    case p: PartEmitter =>
      new EntryEmitter {
        override def emit(b: EntryBuilder): Unit =
          b.entry(YNode("type"), b => p.emit(b))
        override def position(): Position = p.position()
      }
  }
}

abstract class RamlShapeEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext) {

  val typeName: Option[String]
  var typeEmitted                = false
  protected val valuesTag: YType = YType.Str

  def emitters(): Seq[EntryEmitter] = {

    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    fs.entry(ShapeModel.DisplayName).map(f => result += RamlScalarEmitter("displayName", f))
    fs.entry(ShapeModel.Description).map(f => result += RamlScalarEmitter("description", f))

    fs.entry(ShapeModel.Default) match {
      case Some(f) =>
        result += EntryPartEmitter("default",
                                   DataNodeEmitter(shape.default, ordering)(spec.eh),
                                   position = pos(f.value.annotations))
      case None => fs.entry(ShapeModel.DefaultValueString).map(dv => result += ValueEmitter("default", dv))
    }

    fs.entry(ShapeModel.Values).map(f => result += EnumValuesEmitter("enum", f.value, ordering))

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
        f.array.values.map(_.asInstanceOf[Shape]).collectFirst({ case r: RecursiveShape => r }) match {
          case Some(r: RecursiveShape) =>
            typeEmitted = true
            result ++= RamlRecursiveShapeEmitter(r, ordering, references).emitters()
          case _ =>
            typeEmitted = true
            result += RamlShapeInheritsEmitter(f, ordering, references = references)
        }
      })

    if (Option(shape.and).isDefined && shape.and.nonEmpty)
      result += RamlAndConstraintEmitter(shape, ordering, references)
    if (Option(shape.or).isDefined && shape.or.nonEmpty) result += RamlOrConstraintEmitter(shape, ordering, references)
    if (Option(shape.xone).isDefined && shape.xone.nonEmpty)
      result += RamlXoneConstraintEmitter(shape, ordering, references)
    if (Option(shape.not).isDefined) result += RamlNotConstraintEmitter(shape, ordering, references)

    result
  }
}

case class RamlAndConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[Raml10TypePartEmitter] = shape.and.map { s =>
    Raml10TypePartEmitter(s, ordering, None, Nil, references)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "and".asRamlAnnotation,
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}

case class RamlOrConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[Raml10TypePartEmitter] = shape.or.map { s =>
    Raml10TypePartEmitter(s, ordering, None, Nil, references)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "or".asRamlAnnotation,
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}

case class RamlXoneConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[Raml10TypePartEmitter] = shape.xone.map { s =>
    Raml10TypePartEmitter(s, ordering, None, Nil, references)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "xone".asRamlAnnotation,
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}

case class RamlNotConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  val emitter = Raml10TypePartEmitter(shape.not.asInstanceOf[AnyShape], ordering, None, Nil, references)

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "not".asRamlAnnotation,
      p => emitter.emit(p)
    )
  }

  override def position(): Position = emitter.position()
}

case class RamlJsonShapeEmitter(shape: AnyShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                typeKey: String = "type")(implicit spec: SpecEmitterContext)
    extends PartEmitter
    with ExamplesEmitter {

  override def emit(b: PartBuilder): Unit = {
    shape.annotations.find(classOf[ParsedJSONSchema]) match {
      case Some(json) =>
        if (shape.examples.nonEmpty) {
          val results = mutable.ListBuffer[EntryEmitter]()
          emitExamples(shape, results, ordering, references)
          results += MapEntryEmitter(typeKey, json.rawText)
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
    implicit spec: RamlSpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    if (shape.examples.nonEmpty) {
      val fs     = shape.fields
      val result = mutable.ListBuffer[EntryEmitter]()
      result ++= RamlAnyShapeEmitter(shape, ordering, references).emitters()
      fs.entry(SchemaShapeModel.Raw).foreach { f =>
        result += ValueEmitter("type", f)
      }
      b.obj(traverse(ordering.sorted(result), _))
    } else {
      shape.raw.option() match {
        case Some(r) => raw(b, r)
        case None    => b += YNode.Null
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

// case class RamlNamedRecursiveShapeEmitter(recursive:RecursiveShape,ordering: SpecOrdering)(
//  implicit spec: RamlSpecEmitterContext) extends EntryEmitter {
//  override def emit(b: EntryBuilder): Unit = {
//    // todo add error handling?
//    val name = recursive.name.option().orElse(throw new Exception(s"Annotation type without name $recursive")).get
//    val emitters = RamlRecursiveShapeEmitter(recursive,ordering, Nil).emitters()
//
//    b.entry(recursive.name.value(),_.obj(traverse(ordering.sorted(emitters),_)))
//  }
//
//  override def position(): Position = pos(recursive.annotations)
// }

case class RamlRecursiveShapeEmitter(shape: RecursiveShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer()
    result += MapEntryEmitter("type", "object")
    result += MapEntryEmitter("recursive".asRamlAnnotation, shape.fixpoint.value())
    result
  }
}

case class RamlRecursiveShapeTypeEmitter(shape: RecursiveShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    RamlRecursiveShapeEmitter(shape, ordering, references).emitters().foreach(_.emit(b))
  }

  override def position(): Position = pos(shape.annotations)
}

case class RamlNodeShapeEmitter(node: NodeShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlAnyShapeEmitter(node, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs                               = node.fields

    fs.entry(NodeShapeModel.MinProperties).map(f => result += RamlScalarEmitter("minProperties", f))
    fs.entry(NodeShapeModel.MaxProperties).map(f => result += RamlScalarEmitter("maxProperties", f))

    val hasPatternProperties = node.properties.exists(_.patternName.nonEmpty)
    fs.entry(NodeShapeModel.Closed)
      .foreach { f =>
        val closed = node.closed.value()
        if (!hasPatternProperties && (closed || f.value.annotations.contains(classOf[ExplicitField]))) {
          result += MapEntryEmitter("additionalProperties",
                                    (!closed).toString,
                                    YType.Bool,
                                    position = pos(f.value.annotations))
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
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    val values: Seq[Shape] = f.array.values.map(_.asInstanceOf[Shape])
    val multiple           = values.size > 1

    b.entry(
      "type",
      b => {
        // If there are many values is a multiple inheritance which needs to be emitted as a seq
        if (multiple)
          b.list(l => values.foreach(emitShape(_, l)))
        else values.foreach(emitShape(_, b))
      }
    )
  }

  private def emitShape(value: Shape, b: PartBuilder): Unit = value match {
    case u: UnionShape if !u.isLink =>
      RamlInlinedUnionShapeEmitter(u, ordering, references).partEmitters().emitAll(b)
    case d: Shape with ShapeHelpers if d.annotations.contains(classOf[DeclaredElement]) || d.isLink =>
      emitDeclared(d, b)
    case s: AnyShape =>
      b.obj(r => traverse(Raml10TypeEmitter(s, ordering, references = references).entries(), r))
    case other =>
      spec.eh.violation(ResolutionValidation,
                        other.id,
                        None,
                        "Cannot emit for type shapes without WebAPI Shape support",
                        other.position(),
                        other.location())
  }

  private def emitDeclared(shape: Shape with ShapeHelpers, b: PartBuilder): Unit =
    if (shape.isLink) spec.localReference(shape).emit(b)
    else raw(b, shape.name.value())

  override def position(): Position = pos(f.value.annotations)
}

object RamlAnyShapeEmitter {
  def apply(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: RamlSpecEmitterContext): RamlAnyShapeEmitter =
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
          spec
            .filterLocal(shape.examples)
            .partition(e => !e.fields.fieldsMeta().contains(ExampleModel.Name) && !e.isLink)
        val examples = spec.filterLocal(f.array.values.collect({ case e: Example => e }))
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
    implicit spec: RamlSpecEmitterContext)
    extends RamlShapeEmitter(shape, ordering, references)
    with ExamplesEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val results = ListBuffer(super.emitters(): _*)

    emitExamples(shape, results, ordering, references)

    results
  }

  override val typeName: Option[String] = Some("any")
}

object RamlAnyShapeInstanceEmitter {
  def apply(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: RamlSpecEmitterContext): RamlAnyShapeInstanceEmitter =
    new RamlAnyShapeInstanceEmitter(shape, ordering, references)(spec)
}

class RamlAnyShapeInstanceEmitter(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
    implicit spec: RamlSpecEmitterContext)
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

  def processRamlPattern(f: FieldEntry): FieldEntry = {
    var rawRegex = f.value.value.asInstanceOf[AmfScalar].value.asInstanceOf[String]
    if (rawRegex.startsWith("^")) rawRegex = rawRegex.drop(1)
    if (rawRegex.endsWith("$")) rawRegex = rawRegex.dropRight(1)
    f.copy(value = Value(AmfScalar(rawRegex), f.value.annotations))
  }
}

case class RamlScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlAnyShapeEmitter(scalar, ordering, references)
    with RamlCommonOASFieldsEmitter {

  private val rawTypeDef: TypeDef = TypeDefXsdMapping.typeDef(scalar.dataType.value())
  private val (typeDef, format)   = RamlTypeDefStringValueMatcher.matchType(rawTypeDef, scalar.format.option())

  override protected val valuesTag: YType = TypeDefYTypeMapping(rawTypeDef)

  override def emitters(): Seq[EntryEmitter] = {
    val fs = scalar.fields

    val typeEmitterOption = if (scalar.inherits.isEmpty) {
      fs.entry(ScalarShapeModel.DataType)
        .flatMap(f =>
          if (!f.value.annotations.contains(classOf[Inferred])) {
            scalar.fields
              .removeField(ShapeModel.Inherits) // for scalar doesn't make any sense to write the inherits, because it will always be another scalar with the same t
            Some(MapEntryEmitter("type", typeDef, position = pos(f.value.annotations)))
          } else None) // TODO check this  - annotations of typeDef in parser
    } else {
      None
    }

    // use option for not alter the previous default order. (After resolution not any lexical info annotation remains here)

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*) ++ typeEmitterOption

    emitOASFields(fs, result)

    fs.entry(ScalarShapeModel.Pattern).map { f =>
      result += RamlScalarEmitter("pattern", processRamlPattern(f))
    }

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

  override val typeName: Option[String] = None // exceptional case for get the type (scalar) and format
}

case class RamlFileShapeEmitter(scalar: FileShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlAnyShapeEmitter(scalar, ordering, references)
    with RamlCommonOASFieldsEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    emitOASFields(fs, result)

    fs.entry(FileShapeModel.FileTypes).map(f => result += ArrayEmitter("fileTypes", f, ordering))

    fs.entry(ScalarShapeModel.Pattern).map { f =>
      result += ValueEmitter("pattern".asRamlAnnotation, processRamlPattern(f))
    }

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
    implicit spec: RamlSpecEmitterContext)
    extends RamlAnyShapeEmitter(shape, ordering, references) {

  override def emitters(): Seq[EntryEmitter] = {
    // If anyOf is empty and inherits is not empty, the shape is still not resolved. So, emit as a AnyShape
    val unionEmitters =
      if (shape.anyOf.isEmpty && shape.inherits.nonEmpty) Nil
      else Seq(RamlAnyOfShapeEmitter(shape, ordering, references = references))
    super.emitters() ++ unionEmitters
  }

  override val typeName: Option[String] = Some("union")
}

case class RamlInlinedUnionShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlAnyShapeEmitter(shape, ordering, references) {

  def partEmitters(): MixedEmitters = {
    // If anyOf is empty and inherits is not empty, the shape is still not resolved. So, emit as a AnyShape
    val unionEmitters =
      if (shape.anyOf.isEmpty && shape.inherits.nonEmpty) Nil
      else Seq(RamlInlinedAnyOfShapeEmitter(shape, ordering, references = references))
    MixedEmitters(super.emitters(), unionEmitters)
  }

  case class MixedEmitters(entries: Seq[EntryEmitter], parts: Seq[PartEmitter]) {
    def emitAll(b: PartBuilder): Unit = {
      parts.foreach(_.emit(b))
      if (entries.nonEmpty) b.obj(b => entries.foreach(_.emit(b)))
    }
  }

  override val typeName: Option[String] = Some("union")
}

case class RamlAnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    RamlUnionEmitterHelper.inlinedEmission(shape) match {
      case Some(e) => emitUnionInlined(e, b)
      case None    => emitUnionExpanded(b)
    }
  }

  def emitUnionExpanded(b: EntryBuilder): Unit = {
    b.entry(
      "anyOf",
      _.list { b =>
        val emitters = shape.anyOf.map(s => Raml10TypePartEmitter(s, ordering, None, references = references))
        // TODO add lexical information to anyOf elements in TypeExpressionParser. As a WA, the emitters are sorted by the shape id.
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  def emitUnionInlined(types: String, b: EntryBuilder): Unit = b.entry("type", types)

  override def position(): Position = pos(shape.fields.get(UnionShapeModel.AnyOf).annotations)

}

case class RamlInlinedAnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit = {
    RamlUnionEmitterHelper.inlinedEmission(shape) match {
      case Some(e) => emitUnionInlined(e, b)
      case None    => emitUnionExpanded(b)
    }
  }

  def emitUnionExpanded(b: PartBuilder): Unit = {
    b.obj(
      b =>
        b.entry(
          "anyOf",
          _.list { b =>
            val emitters = shape.anyOf.map(s => Raml10TypePartEmitter(s, ordering, None, references = references))
            // TODO add lexical information to anyOf elements in TypeExpressionParser. As a WA, the emitters are sorted by the shape id.
            traverse(ordering.sorted(emitters), b)
          }
      )
    )
  }

  def emitUnionInlined(types: String, b: PartBuilder): Unit = b += types

  override def position(): Position = pos(shape.fields.get(UnionShapeModel.AnyOf).annotations)
}

object RamlUnionEmitterHelper {
  def inlinedEmission(shape: UnionShape): Option[String] = {
    val union: Seq[String] = shape.anyOf.map {
      case scalar: ScalarShape if isSimpleScalar(scalar) =>
        RamlTypeDefStringValueMatcher
          .matchType(TypeDefXsdMapping.typeDef(scalar.dataType.value()), scalar.format.option())
          ._1
      case s: Shape if s.isLink && s.linkLabel.option().isDefined => s.linkLabel.value()
      case n: NilShape if n.fields.fields().isEmpty               => "nil"
      case a: ArrayShape if a.fields.fields().isEmpty             => "array"
      case a: NodeShape if a.fields.fields().isEmpty              => "object"
      case a: AnyShape if a.fields.fields().isEmpty               => "any"
      case _                                                      => return None
    }
    Some(union.mkString(" | "))
  }

  private def isSimpleScalar(scalar: ScalarShape): Boolean =
    scalar.fields.fields().size <= 2 && scalar.fields
      .fields()
      .map(_.field)
      .forall(f => f == ScalarShapeModel.Name || f == ScalarShapeModel.DataType)
}

case class RamlArrayShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
    implicit spec: RamlSpecEmitterContext)
    extends RamlAnyShapeEmitter(tuple, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = tuple.fields

    result += RamlTupleItemsShapeEmitter(tuple, ordering, references)
    result += MapEntryEmitter("tuple".asRamlAnnotation, "true", YType.Bool)

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += RamlScalarEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += RamlScalarEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += RamlScalarEmitter("uniqueItems", f))

    result
  }

  override val typeName: Option[String] = tuple.annotations.find(classOf[ExplicitField]).map(_ => "array")
}

case class RamlItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
      case r: RecursiveShape =>
        b.entry(
          "items",
          _.obj { b =>
            Raml10TypeEmitter(r, ordering, references = references).entries().foreach(_.emit(b))
          }
        )
      case _ => // ignore
    }
  }

  override def position(): Position = {
    pos(array.fields.getValue(ArrayShapeModel.Items).annotations)
  }
}

case class RamlTupleItemsShapeEmitter(tuple: TupleShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val result = mutable.ListBuffer[EntryEmitter]()
    tuple.items match {
      case tupleItems: AnyShape =>
        tuple.items
          .foreach { _ =>
            Raml10TypeEmitter(tupleItems, ordering, references = references).entries().foreach(result += _)
          }
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
    implicit spec: RamlSpecEmitterContext)
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

case class RequiredShapeEmitter(shape: Shape, minCount: Option[FieldEntry]) {

  def emitter(): Option[EntryEmitter] = {
    minCount.flatMap { entry =>
      if (entry.value.annotations.contains(classOf[ExplicitField])) {
        Some(
          EntryPartEmitter("required",
                           RawEmitter(if (entry.scalar.toNumber.intValue() > 0) "true" else "false", YType.Bool)))
      } else {
        None
      }
    }
  }
}

case class RamlPropertyShapeEmitter(property: PropertyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val fs = property.fields

    val name: String = fs
      .entry(PropertyShapeModel.MinCount)
      .map(f => {
        if (f.scalar.value.asInstanceOf[Int] == 0 && !f.value.annotations.contains(classOf[ExplicitField]))
          property.name.value() + "?"
        else if (property.patternName.option().isDefined && property.name.value() != "//")
          s"/${property.name.value()}/"
        else
          property.name.value()
      })
      .getOrElse(property.name.value())

    val key = YNode(YScalar(name), YType.Str)

    if (property.range.annotations.contains(classOf[SynthesizedField])) {
      b.entry(
        key,
        raw(_, "", YType.Null)
      )
    } else {

      val additionalEmitters: Seq[EntryEmitter] =
        (property.fields
          .entry(PropertyShapeModel.ReadOnly)
          .map(fe => ValueEmitter("readOnly".asRamlAnnotation, fe)) ++ RequiredShapeEmitter(
          shape = property.range,
          property.fields.entry(PropertyShapeModel.MinCount))
          .emitter()).toSeq

      property.range match {
        case range: AnyShape =>
          b.entry(
            key,
            pb => {
              Raml10TypePartEmitter(range, ordering, None, references = references).emitter match {
                case Left(p)        => p.emit(pb)
                case Right(entries) => pb.obj(traverse(ordering.sorted(entries ++ additionalEmitters), _))
              }
            }
          )
        case _ => // ignore
          b.entry(key, _.obj(e => traverse(additionalEmitters, e)))
      }
    }
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}

case class RamlSchemaEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
                              references: Seq[BaseUnit],
                              pointer: Seq[String] = Nil,
                              schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends OasTypePartCollector(shape, ordering, ignored, references)
    with PartEmitter {

  override def emit(b: PartBuilder): Unit =
    emitter(pointer, schemaPath) match {
      case Left(p)        => p.emit(b)
      case Right(entries) => b.obj(traverse(entries, _))
    }

  override def position(): Position = getEmitters.headOption.map(_.position()).getOrElse(ZERO)

}

abstract class OasTypePartCollector(shape: Shape,
                                    ordering: SpecOrdering,
                                    ignored: Seq[Field],
                                    references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext) {
  private var _emitters: Option[Seq[Emitter]]                          = None
  private var _emitter: Option[Either[PartEmitter, Seq[EntryEmitter]]] = None

  protected def getEmitters: Seq[Emitter] = _emitters.getOrElse(Nil)

  protected def emitters(pointer: Seq[String], schemaPath: Seq[(String, String)]): Seq[Emitter] = {
    _emitters match {
      case Some(ems) => ems
      case _ =>
        _emitters = Some(
          ordering.sorted(OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()))
        _emitters.get
    }
  }

  protected def emitter: Either[PartEmitter, Seq[EntryEmitter]] = emitter(Nil, Nil)

  protected def emitter(pointer: Seq[String],
                        schemaPath: Seq[(String, String)]): Either[PartEmitter, Seq[EntryEmitter]] = _emitter match {
    case Some(em) => em
    case _ =>
      _emitter = Some(
        emitters(pointer, schemaPath) match {
          case Seq(p: PartEmitter)                           => Left(p)
          case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
          case other                                         => throw new Exception(s"IllegalTypeDeclarations found: $other")
        }
      )
      _emitter.get
  }
}

class SimpleOasTypePartCollector(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field], references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends OasTypePartCollector(shape, ordering, ignored, references) {

  def computeEmitters(pointer: Seq[String], schemaPath: Seq[(String, String)]): Seq[Emitter] =
    emitters(pointer, schemaPath)
}

case class OasTypeEmitter(shape: Shape,
                          ordering: SpecOrdering,
                          ignored: Seq[Field] = Nil,
                          references: Seq[BaseUnit],
                          pointer: Seq[String] = Nil,
                          schemaPath: Seq[(String, String)] = Nil,
                          isHeader: Boolean = false)(implicit spec: OasSpecEmitterContext) {
  def emitters(): Seq[Emitter] = {

    // Adjusting JSON Schema  pointer
    val nextPointerStr = s"#${pointer.map(p => s"/$p").mkString}"
    var updatedSchemaPath: Seq[(String, String)] = {
      schemaPath :+ (shape.id, nextPointerStr)
    }

    shape match {
      // Only will add to the list if the shape is a declaration
      case chain: InheritanceChain if shape.annotations.contains(classOf[DeclaredElement]) =>
        updatedSchemaPath ++= chain.inheritedIds.map((_, nextPointerStr))
      case _ => // ignore
    }

    shape match {
      case l: Linkable if l.isLink => Seq(OasTagToReferenceEmitter(shape, l.linkLabel.option(), Nil))
      case schema: SchemaShape =>
        val copiedNode = schema.copy(fields = schema.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasSchemaShapeEmitter(copiedNode, ordering).emitters()
      case node: NodeShape =>
        val copiedNode = node.copy(fields = node.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasNodeShapeEmitter(copiedNode, ordering, references, pointer, updatedSchemaPath, isHeader).emitters()
      case union: UnionShape if nilUnion(union) =>
        OasTypeEmitter(union.anyOf.head, ordering, ignored, references).emitters()
      case union: UnionShape =>
        val copiedNode = union.copy(fields = union.fields.filter(f => !ignored.contains(f._1)))
        OasUnionShapeEmitter(copiedNode, ordering, references, pointer, updatedSchemaPath).emitters()
      case array: ArrayShape =>
        val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
        OasArrayShapeEmitter(copiedArray, ordering, references, pointer, updatedSchemaPath, isHeader).emitters()
      case matrix: MatrixShape =>
        val copiedMatrix = matrix.copy(fields = matrix.fields.filter(f => !ignored.contains(f._1))).withId(matrix.id)
        OasArrayShapeEmitter(copiedMatrix.toArrayShape, ordering, references, pointer, updatedSchemaPath, isHeader)
          .emitters()
      case array: TupleShape =>
        val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
        OasTupleShapeEmitter(copiedArray, ordering, references, pointer, updatedSchemaPath, isHeader).emitters()
      case nil: NilShape =>
        val copiedNil = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
        Seq(OasNilShapeEmitter(copiedNil, ordering))
      case file: FileShape =>
        spec match {
          // In JSON-SCHEMA the datatype file is not valid, so we 'convert it' in a string scalar
          case _: JsonSchemaEmitterContext =>
            val scalar = ScalarShape
              .apply(file.fields, file.annotations)
              .withDataType(DataType.String)
              .copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
            OasScalarShapeEmitter(scalar, ordering, references, isHeader).emitters()
          case _ =>
            val copiedScalar = file.copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
            OasFileShapeEmitter(copiedScalar, ordering, references, isHeader).emitters()
        }
      case scalar: ScalarShape =>
        val copiedScalar = scalar.copy(fields = scalar.fields.filter(f => !ignored.contains(f._1)))
        OasScalarShapeEmitter(copiedScalar, ordering, references, isHeader).emitters()
      case recursive: RecursiveShape =>
        Seq(OasRecursiveShapeEmitter(recursive, ordering, schemaPath))
      case any: AnyShape =>
        val copiedNode = any.copyAnyShape(fields = any.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasAnyShapeEmitter(copiedNode, ordering, references, pointer, updatedSchemaPath, isHeader).emitters()
      case _ => Seq()
    }
  }

  def entries(): Seq[EntryEmitter] = emitters() collect { case e: EntryEmitter => e }

  def nilUnion(union: UnionShape): Boolean =
    union.anyOf.size == 1 && union.anyOf.head.annotations.contains(classOf[NilUnion])

}

abstract class OasShapeEmitter(shape: Shape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               pointer: Seq[String] = Nil,
                               schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val emitDocumentation = spec.options.isWithDocumentation

    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    if (emitDocumentation) {
      fs.entry(ShapeModel.DisplayName).map(f => result += ValueEmitter("title", f))

      fs.entry(ShapeModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(ShapeModel.Default) match {
        case Some(f) =>
          result += EntryPartEmitter("default",
                                     DataNodeEmitter(shape.default, ordering)(spec.eh),
                                     position = pos(f.value.annotations))
        case None => fs.entry(ShapeModel.DefaultValueString).map(dv => result += ValueEmitter("default", dv))
      }

      fs.entry(AnyShapeModel.Documentation)
        .map(f =>
          result += OasEntryCreativeWorkEmitter("externalDocs", f.value.value.asInstanceOf[CreativeWork], ordering))
    }

    fs.entry(ShapeModel.Values).map(f => result += EnumValuesEmitter("enum", f.value, ordering))

    fs.entry(AnyShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    emitNullable(result)

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    fs.entry(ShapeModel.CustomShapePropertyDefinitions)
      .map(f => {
        result += spec.factory.customFacetsEmitter(f, ordering, references)
      })

    if (Option(shape.and).isDefined && shape.and.nonEmpty)
      result += OasAndConstraintEmitter(shape, ordering, references, pointer, schemaPath)
    if (Option(shape.or).isDefined && shape.or.nonEmpty)
      result += OasOrConstraintEmitter(shape, ordering, references, pointer, schemaPath)
    if (Option(shape.xone).isDefined && shape.xone.nonEmpty)
      result += OasXoneConstraintEmitter(shape, ordering, references, pointer, schemaPath)
    if (Option(shape.not).isDefined)
      result += OasNotConstraintEmitter(shape, ordering, references, pointer, schemaPath)

    if (spec.vendor == Vendor.OAS30)
      fs.entry(ShapeModel.Deprecated).map(f => result += ValueEmitter("deprecated", f))

    result
  }

  def emitNullable(result: ListBuffer[EntryEmitter]): Unit = {
    shape.annotations.find(classOf[NilUnion]) match {
      case Some(NilUnion(rangeString)) =>
        result += ValueEmitter(
          "nullable",
          FieldEntry(
            Field(Bool,
                  Namespace.Shapes + "nullable",
                  ModelDoc(ModelVocabularies.Shapes, "nullable", "This field can accept a null value")),
            Value(AmfScalar(true), Annotations(LexicalInformation(rangeString)))
          )
        )

      case _ => // ignore
    }
  }
}

case class OasUnionShapeEmitter(shape: UnionShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil,
                                isHeader: Boolean = false)(implicit spec: OasSpecEmitterContext)
    extends OasAnyShapeEmitter(shape, ordering, references, isHeader = isHeader) {

  override def emitters(): Seq[EntryEmitter] =
    super.emitters() ++ Seq(OasAnyOfShapeEmitter(shape, ordering, references, pointer, schemaPath))
}

case class OasAnyOfShapeEmitter(shape: UnionShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      spec.anyOfKey,
      _.list { b =>
        val emitters = shape.anyOf.zipWithIndex map {
          case (s: Shape, i: Int) =>
            OasTypePartEmitter(s,
                               ordering,
                               ignored = Nil,
                               references,
                               pointer = pointer ++ Seq("anyOf", s"$i"),
                               schemaPath)
        }
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}

case class OasRecursiveShapeEmitter(recursive: RecursiveShape,
                                    ordering: SpecOrdering,
                                    schemaPath: Seq[(String, String)])(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val pointer = recursive.fixpoint.option() match {
      case Some(id) =>
        findInPath(id).orElse(recursive.fixpointTarget match {
          case Some(shape) =>
            findInPath(shape.id).orElse {
              // If the fixpoint is not in the schemaPath I will assume that it is a declaration and this declaration will be present
              recursive.fixpointTarget
                .flatMap(_.name.option().map(s"#${spec.schemasDeclarationsPath}" + _))
            }
          case None => None
        })
      case _ => None
    }
    for { p <- pointer } b.entry("$ref", p)
  }

  private def findInPath(id: String): Option[String] = {
    // List of chars that generates an URISyntaxException in Java but works in JS
    // Pointers with these keys must be ignored
    val extraneousChars = Seq('^')
    schemaPath.reverse.find(_._1 == id) match {
      case Some((_, pointer)) if pointer.equals("#") && !spec.isInstanceOf[JsonSchemaEmitterContext] => None
      case Some((_, pointer)) if !extraneousChars.forall(pointer.contains(_))                        => Some(pointer)
      case _                                                                                         => None
    }
  }

  override def position(): Position = ZERO
}

case class OasOrConstraintEmitter(shape: Shape,
                                  ordering: SpecOrdering,
                                  references: Seq[BaseUnit],
                                  pointer: Seq[String] = Nil,
                                  schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[OasTypePartEmitter] = shape.or.zipWithIndex map {
    case (s: Shape, i: Int) =>
      OasTypePartEmitter(s, ordering, ignored = Nil, references, pointer = pointer ++ Seq("anyOf", s"$i"), schemaPath)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "anyOf",
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}

case class OasAndConstraintEmitter(shape: Shape,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit],
                                   pointer: Seq[String] = Nil,
                                   schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[OasTypePartEmitter] = shape.and.zipWithIndex map {
    case (s: Shape, i: Int) =>
      OasTypePartEmitter(s, ordering, ignored = Nil, references, pointer = pointer ++ Seq("allOf", s"$i"), schemaPath)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "allOf",
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}

case class OasXoneConstraintEmitter(shape: Shape,
                                    ordering: SpecOrdering,
                                    references: Seq[BaseUnit],
                                    pointer: Seq[String] = Nil,
                                    schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[OasTypePartEmitter] = shape.xone.zipWithIndex map {
    case (s: Shape, i: Int) =>
      OasTypePartEmitter(s, ordering, ignored = Nil, references, pointer = pointer ++ Seq("oneOf", s"$i"), schemaPath)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "oneOf",
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}

case class OasNotConstraintEmitter(shape: Shape,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit],
                                   pointer: Seq[String] = Nil,
                                   schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  val emitter =
    OasTypePartEmitter(shape.not, ordering, ignored = Nil, references = references, pointer :+ "not", schemaPath)

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "not",
      p => emitter.emit(p)
    )
  }

  override def position(): Position = emitter.position()
}

object OasAnyShapeEmitter {
  def apply(shape: AnyShape,
            ordering: SpecOrdering,
            references: Seq[BaseUnit],
            pointer: Seq[String] = Nil,
            schemaPath: Seq[(String, String)] = Nil,
            isHeader: Boolean = false)(implicit spec: OasSpecEmitterContext): OasAnyShapeEmitter =
    new OasAnyShapeEmitter(shape, ordering, references, pointer, schemaPath, isHeader)(spec)
}

class OasAnyShapeEmitter(shape: AnyShape,
                         ordering: SpecOrdering,
                         references: Seq[BaseUnit],
                         pointer: Seq[String] = Nil,
                         schemaPath: Seq[(String, String)] = Nil,
                         isHeader: Boolean = false)(implicit spec: OasSpecEmitterContext)
    extends OasShapeEmitter(shape, ordering, references, pointer, schemaPath) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    if (spec.options.isWithDocumentation)
      shape.fields
        .entry(AnyShapeModel.Examples)
        .map(f => {
          val examples = spec.filterLocal(f.array.values.collect({ case e: Example => e }))
          val tuple    = examples.partition(e => !e.fields.fieldsMeta().contains(ExampleModel.Name) && !e.isLink)

          result ++= (tuple match {
            case (Nil, Nil)         => Nil
            case (named, Nil)       => examplesEmitters(named.headOption, named.tail, isHeader)
            case (Nil, named)       => examplesEmitters(None, named, isHeader)
            case (anonymous, named) => examplesEmitters(anonymous.headOption, anonymous.tail ++ named, isHeader)
          })
        })

    super.emitters() ++ result
  }

  private def examplesEmitters(main: Option[Example], extentions: Seq[Example], isHeader: Boolean) = {
    val em    = ListBuffer[EntryEmitter]()
    val label = if (isHeader) "x-amf-example" else "example"
    main.foreach(a => em += SingleExampleEmitter(label, a, ordering))
    val labesl = if (isHeader) "x-amf-examples" else "examples"
    if (extentions.nonEmpty)
      em += MultipleExampleEmitter("examples".asOasExtension, extentions, ordering, references)
    em
  }
}

case class OasArrayShapeEmitter(shape: ArrayShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil,
                                isHeader: Boolean = false)(implicit spec: OasSpecEmitterContext)
    extends OasAnyShapeEmitter(shape, ordering, references, isHeader = isHeader) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter](super.emitters(): _*)
    val fs     = shape.fields

    result += spec.oasTypePropertyEmitter("array", shape)

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    fs.entry(ArrayShapeModel.CollectionFormat) match { // What happens if there is an array of an array with collectionFormat?
      case Some(f) if f.value.annotations.contains(classOf[CollectionFormatFromItems]) =>
        result += OasItemsShapeEmitter(shape,
                                       ordering,
                                       references,
                                       Some(ValueEmitter("collectionFormat", f)),
                                       pointer,
                                       schemaPath)
      case Some(f) =>
        result += OasItemsShapeEmitter(shape, ordering, references, None, pointer, schemaPath) += ValueEmitter(
          "collectionFormat",
          f)
      case None =>
        result += OasItemsShapeEmitter(shape, ordering, references, None, pointer, schemaPath)
    }

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering, references))

    result ++= FacetsEmitter(shape, ordering).emitters

    result
  }
}

case class OasTupleShapeEmitter(shape: TupleShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil,
                                isHeader: Boolean = false)(implicit spec: OasSpecEmitterContext)
    extends OasAnyShapeEmitter(shape, ordering, references, isHeader = isHeader) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter](super.emitters(): _*)
    val fs     = shape.fields

    result += spec.oasTypePropertyEmitter("array", shape)

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    fs.entry(TupleShapeModel.ClosedItems) match {
      case Some(f) => result += ValueEmitter("additionalItems", f.negated)
      case None =>
        fs.entry(TupleShapeModel.AdditionalItemsSchema)
          .map(f => result += OasEntryShapeEmitter("additionalItems", f, ordering, references))
    }

    fs.entry(ArrayShapeModel.CollectionFormat) match { // What happens if there is an array of an array with collectionFormat?
      case Some(f) if f.value.annotations.contains(classOf[CollectionFormatFromItems]) =>
        result += OasTupleItemsShapeEmitter(shape,
                                            ordering,
                                            references,
                                            Some(ValueEmitter("collectionFormat", f)),
                                            pointer,
                                            schemaPath)
      case Some(f) =>
        result += OasTupleItemsShapeEmitter(shape, ordering, references, None, pointer, schemaPath) += ValueEmitter(
          "collectionFormat",
          f)
      case None =>
        result += OasTupleItemsShapeEmitter(shape, ordering, references, None, pointer, schemaPath)
    }

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering, references))

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
                                additionalEntry: Option[ValueEmitter],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends OasTypePartCollector(array.items, ordering, Nil, references)
    with EntryEmitter {

  def emit(b: EntryBuilder): Unit = {
    if (Option(array.fields.getValue(ArrayShapeModel.Items)).isDefined) {
      b.entry("items", b => emitPart(b))
    }
  }

  def emitPart(part: PartBuilder): Unit = {
    emitter(pointer :+ "items", schemaPath) match {
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

case class OasTupleItemsShapeEmitter(array: TupleShape,
                                     ordering: SpecOrdering,
                                     references: Seq[BaseUnit],
                                     additionalEntry: Option[ValueEmitter],
                                     pointer: Seq[String] = Nil,
                                     schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  val itemEmitters: Seq[OasTypeEmitter] = {
    array.items.zipWithIndex.map {
      case (shape, i) =>
        /*
      val collector = new SimpleOasTypePartCollector(shape, ordering, Nil, references)
      collector.computeEmitters(pointer ++ Seq("items", s"$i"), schemaPath)
         */
        OasTypeEmitter(shape, ordering, Nil, references, pointer ++ Seq("items", s"$i"), schemaPath)
    }
  }

  def emit(b: EntryBuilder): Unit = {
    if (Option(array.fields.getValue(TupleShapeModel.TupleItems)).isDefined) {
      b.entry(
        "items",
        _.list { le =>
          itemEmitters.foreach { emitter =>
            val allEmitters = emitter.emitters().collect { case e: EntryEmitter => e }
            le.obj { o =>
              allEmitters.foreach(_.emit(o))
            }
          }
        }
      )
    }
  }
  override def position(): Position = {
    Option(array.fields.getValue(ArrayShapeModel.Items)) match {
      case Some(value) => pos(value.annotations)
      case _           => ZERO
    }
  }
}

case class OasNodeShapeEmitter(node: NodeShape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               pointer: Seq[String] = Nil,
                               schemaPath: Seq[(String, String)] = Nil,
                               isHeader: Boolean = false)(implicit spec: OasSpecEmitterContext)
    extends OasAnyShapeEmitter(node, ordering, references, isHeader = isHeader) {
  override def emitters(): Seq[EntryEmitter] = {
    val isOas3 = spec.vendor == Vendor.OAS30

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = node.fields

    result += spec.oasTypePropertyEmitter("object", node)

    fs.entry(NodeShapeModel.MinProperties).map(f => result += ValueEmitter("minProperties", f))

    fs.entry(NodeShapeModel.MaxProperties).map(f => result += ValueEmitter("maxProperties", f))

    fs.entry(NodeShapeModel.Closed)
      .filter(f => f.value.annotations.contains(classOf[ExplicitField]) || f.scalar.toBool) match {
      case Some(f) => result += ValueEmitter("additionalProperties", f.negated)
      case _ =>
        fs.entry(NodeShapeModel.AdditionalPropertiesSchema)
          .map(f => result += OasEntryShapeEmitter("additionalProperties", f, ordering, references))
    }

    if (isOas3) {
      fs.entry(NodeShapeModel.Discriminator)
        .orElse(fs.entry(NodeShapeModel.DiscriminatorMapping))
        .map(f => result += Oas3DiscriminatorEmitter(f, fs, ordering))
    } else {
      fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("discriminator", f))
    }

    fs.entry(NodeShapeModel.DiscriminatorValue)
      .map(f => result += ValueEmitter("discriminatorValue".asOasExtension, f))

    fs.entry(NodeShapeModel.Properties).map(f => result += OasRequiredPropertiesShapeEmitter(f, references))

    fs.entry(NodeShapeModel.Properties)
      .map(f =>
        result += OasPropertiesShapeEmitter(f, ordering, references, pointer = pointer, schemaPath = schemaPath))

    val properties = ListMap(node.properties.map(p => p.id -> p): _*)

    fs.entry(NodeShapeModel.Dependencies).map(f => result += OasShapeDependenciesEmitter(f, ordering, properties))

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering, references))

    result
  }
}

case class Oas3DiscriminatorEmitter(found: FieldEntry, fs: Fields, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "discriminator",
      _.obj { b =>
        val result: ListBuffer[EntryEmitter] = ListBuffer()
        fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("propertyName", f))
        fs.entry(NodeShapeModel.DiscriminatorMapping).map(f => result += IriTemplateEmitter("mapping", f, ordering))
        traverse(ordering.sorted(result), b)
      }
    )
  }
  override def position(): Position = pos(found.value.annotations)
}

case class IriTemplateEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj { b =>
        val emitters = f
          .arrayValues[AmfObject]
          .flatMap(iriMapping => {
            for {
              variable <- iriMapping.fields.entry(IriTemplateMappingModel.TemplateVariable)
              link     <- iriMapping.fields.entry(IriTemplateMappingModel.LinkExpression)
            } yield {
              ValueEmitter(variable.scalar.toString, link)
            }
          })
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
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
      "x-amf-merge",
      _.list(b =>
        inherits.foreach { s =>
          if (s.annotations.contains(classOf[DeclaredElement]))
            spec.ref(b, OasDefinitions.appendDefinitionsPrefix(s.name.value(), Some(spec.vendor)))
          else if (s.linkTarget.isDefined)
            spec.ref(b, OasDefinitions.appendDefinitionsPrefix(s.name.value(), Some(spec.vendor)))
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

trait RamlFormatTranslator {
  def checkRamlFormats(format: String): String = {
    format match {
      case "date-only"     => "date"
      case "time-only"     => "time"
      case "datetime-only" => "date-time"
      case "datetime"      => "date-time"
      case "rfc3339"       => "date-time"
      case other           => other
    }
  }
}

trait OasCommonOASFieldsEmitter extends RamlFormatTranslator {

  def typeDef: Option[TypeDef] = None
  implicit val spec: OasSpecEmitterContext

  def emitCommonFields(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {

    fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern", f))

    fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

    emitFormatRanges(fs, result)

    fs.entry(ScalarShapeModel.ExclusiveMinimum).map(f => result += ValueEmitter("exclusiveMinimum", f))

    fs.entry(ScalarShapeModel.ExclusiveMaximum).map(f => result += ValueEmitter("exclusiveMaximum", f))

    fs.entry(ScalarShapeModel.MultipleOf)
      .map(f => result += ValueEmitter("multipleOf", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

  }

  def emitFormatRanges(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    if (typeDef.exists(_.isNumber) && spec.isInstanceOf[JsonSchemaEmitterContext]) {
      fs.entry(ScalarShapeModel.Format) match {
        case Some(fe) =>
          val format = fe.value.toString
          val minMax: Option[(Double, Double)] = format match {
            case "int8"  => Some((-128, 127))
            case "int16" => Some((-32768, 32767))
            case "int32" => Some((-2147483648, 2147483647))
            case "int64" =>
              Some((-9223372036854775808.0, 9223372036854775807.0)) // long type // todo fix syaml for long numbers
            case _ => None
          }

          fs.entry(ScalarShapeModel.Minimum).fold(minMax.foreach(m => buildMin(m._1, result)))(f => emitMin(f, result))
          fs.entry(ScalarShapeModel.Maximum).fold(minMax.foreach(m => buildMax(m._2, result)))(f => emitMax(f, result))

        case _ =>
          emitMinAndMax(fs, result)
      }
    } else {
      fs.entry(ScalarShapeModel.Format).map { f =>
        result += RawValueEmitter("format",
                                  ScalarShapeModel.Format,
                                  checkRamlFormats(f.scalar.toString),
                                  f.value.annotations)
      }
      emitMinAndMax(fs, result)
    }
  }

  private def emitMinAndMax(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    fs.entry(ScalarShapeModel.Minimum).foreach(emitMin(_, result))
    fs.entry(ScalarShapeModel.Maximum).foreach(emitMax(_, result))
  }

  private def emitMin(f: FieldEntry, result: ListBuffer[EntryEmitter]) =
    result += ValueEmitter("minimum", f, Some(NumberTypeToYTypeConverter.convert(typeDef)))

  private def emitMax(f: FieldEntry, result: ListBuffer[EntryEmitter]) =
    result += ValueEmitter("maximum", f, Some(NumberTypeToYTypeConverter.convert(typeDef)))

  private def buildMin(min: Double, result: ListBuffer[EntryEmitter]): Unit =
    build(min, "minimum", ScalarShapeModel.Minimum, result)

  private def buildMax(max: Double, result: ListBuffer[EntryEmitter]): Unit =
    build(max, "maximum", ScalarShapeModel.Maximum, result)

  private def build(value: Double, constraint: String, f: Field, result: ListBuffer[EntryEmitter]): Unit =
    result += ValueEmitter(constraint,
                           FieldEntry(f, Value(AmfScalar(value), Annotations())),
                           Some(NumberTypeToYTypeConverter.convert(typeDef)))

}

case class OasScalarShapeEmitter(scalar: ScalarShape,
                                 ordering: SpecOrdering,
                                 references: Seq[BaseUnit],
                                 isHeader: Boolean = false)(override implicit val spec: OasSpecEmitterContext)
    extends OasAnyShapeEmitter(scalar, ordering, references, isHeader = isHeader)
    with OasCommonOASFieldsEmitter {

  override def typeDef: Option[TypeDef] = scalar.dataType.option().map(TypeDefXsdMapping.typeDef)

  override def emitters(): Seq[EntryEmitter] = {

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)
    val fs                               = scalar.fields

    fs.entry(ScalarShapeModel.DataType)
      .foreach { f =>
        val typeDefStr = spec.typeDefMatcher.matchType(typeDef.get)
        scalar.annotations.find(classOf[TypePropertyLexicalInfo]) match {
          case Some(lexicalInfo) =>
            result += MapEntryEmitter("type", typeDefStr, YType.Str, lexicalInfo.range.start)
          case _ =>
            result += MapEntryEmitter("type", typeDefStr, position = pos(f.value.annotations)) // TODO check this  - annotations of typeDef in parser
        }
      }

    fs.entry(ScalarShapeModel.Format) match {
      case Some(_) => // ignore, this will be set with the explicit information
      case None =>
        spec.typeDefMatcher.matchFormat(typeDef.getOrElse(UndefinedType)) match {
          case Some(format) =>
            result += RawValueEmitter("format", ScalarShapeModel.Format, checkRamlFormats(format))
          case None => // ignore
        }
    }
    emitCommonFields(fs, result)

    result
  }
}

case class OasFileShapeEmitter(scalar: FileShape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               isHeader: Boolean)(override implicit val spec: OasSpecEmitterContext)
    extends OasAnyShapeEmitter(scalar, ordering, references, isHeader = isHeader)
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
      case property: PropertyShape if property.patternName.isNullOrEmpty => property.minCount.value() > 0
      case _                                                             => false
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

case class OasPropertiesShapeEmitter(f: FieldEntry,
                                     ordering: SpecOrdering,
                                     references: Seq[BaseUnit],
                                     pointer: Seq[String] = Nil,
                                     schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val properties = f.array.values.partition(_.asInstanceOf[PropertyShape].patternName.option().isDefined)

    // If properties not empty, emit it
    properties._2 match {
      case Nil  =>
      case some => emitProperties(some, "properties", b)
    }

    // If patternProperties not empty, emit it
    properties._1 match {
      case Nil  =>
      case some => emitProperties(some, "patternProperties", b)
    }
  }

  override def position(): Position = pos(f.value.annotations)

  private def emitProperties(properties: Seq[AmfElement], propertiesKey: String, b: EntryBuilder) {
    b.entry(
      propertiesKey,
      _.obj { b =>
        val result =
          properties.map(
            v =>
              OasPropertyShapeEmitter(v.asInstanceOf[PropertyShape],
                                      ordering,
                                      references,
                                      propertiesKey,
                                      pointer,
                                      schemaPath))
        traverse(ordering.sorted(result), b)
      }
    )
  }
}

case class OasPropertyShapeEmitter(property: PropertyShape,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit],
                                   propertiesKey: String = "properties",
                                   pointer: Seq[String] = Nil,
                                   schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasSpecEmitterContext)
    extends OasTypePartCollector(property.range, ordering, Nil, references)
    with EntryEmitter {

  val readOnlyEmitter: Option[ValueEmitter] =
    property.fields.entry(PropertyShapeModel.ReadOnly).map(fe => ValueEmitter("readOnly", fe))

  val propertyName: String = property.patternName.option().getOrElse(property.name.value())
  val propertyKey          = YNode(YScalar(propertyName), YType.Str)

  val computedEmitters: Either[PartEmitter, Seq[EntryEmitter]] =
    emitter(pointer ++ Seq(propertiesKey, propertyName), schemaPath)

  override def emit(b: EntryBuilder): Unit = {
    property.range match {
      case _: AnyShape | _: RecursiveShape =>
        b.entry(
          propertyKey,
          pb => {
            computedEmitters match {
              case Left(p)        => p.emit(pb)
              case Right(entries) => pb.obj(traverse(ordering.sorted(entries ++ readOnlyEmitter), _))
            }
          }
        )
      case _ => // ignore
        b.entry(propertyKey, _.obj(e => traverse(readOnlyEmitter.toSeq, e)))
    }
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}

case class Raml08TypeEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends ExamplesEmitter {

  def inheritsEmitters(): Seq[Emitter] = {
    val father =
      shape.inherits.collectFirst({ case s: Shape if s.annotations.contains(classOf[ParsedJSONSchema]) => s }).get
    val emitter = new EntryEmitter {
      override def emit(b: EntryBuilder): Unit = {
        val emit: PartBuilder => Unit = Raml08TypePartEmitter(father, ordering, Nil).emit _
        b.entry("schema", emit)
      }

      override def position(): Position = pos(father.annotations)
    }

    val results = mutable.ListBuffer[EntryEmitter]()
    results += emitter
    shape match {
      case any: AnyShape if any.examples.nonEmpty => emitExamples(any, results, ordering, Nil)
      case _                                      => // ignore
    }

    results
  }

  def emitters(): Seq[Emitter] = {
    shape match {
      case shape: Shape if shape.isLink                                                     => Seq(spec.localReference(shape))
      case s: Shape if s.inherits.exists(_.annotations.contains(classOf[ParsedJSONSchema])) => inheritsEmitters()
      case shape: AnyShape if shape.annotations.find(classOf[ParsedJSONSchema]).isDefined =>
        Seq(RamlJsonShapeEmitter(shape, ordering, Nil, typeKey = "schema"))
      case scalar: ScalarShape =>
        SimpleTypeEmitter(scalar, ordering).emitters()
      case array: ArrayShape =>
        array.items match {
          case sc: ScalarShape =>
            SimpleTypeEmitter(sc, ordering).emitters() :+ MapEntryEmitter("repeat", "true", YType.Bool)
          case f: FileShape =>
            val scalar =
              ScalarShape(f.fields, f.annotations)
                .withDataType(XsdTypeDefMapping.xsdFromString("file")._1.get)
            SimpleTypeEmitter(scalar, ordering).emitters() :+ MapEntryEmitter("repeat", "true", YType.Bool)
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
      case nil: NilShape =>
        RamlNilShapeEmitter(nil, ordering, Seq()).emitters()
      case fileShape: FileShape =>
        val scalar =
          ScalarShape(fileShape.fields, fileShape.annotations)
            .withDataType(XsdTypeDefMapping.xsdFromString("file")._1.get)
        SimpleTypeEmitter(scalar, ordering).emitters()
      case shape: AnyShape =>
        RamlAnyShapeEmitter(shape, ordering, Nil).emitters()
      case other =>
        Seq(CommentEmitter(other, s"Unsupported shape class for emit raml 08 spec ${other.getClass.toString}`"))
    }
  }

}

case class SimpleTypeEmitter(shape: ScalarShape, ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends RamlCommonOASFieldsEmitter {

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

    fs.entry(ScalarShapeModel.Values)
      .map(f => result += EnumValuesEmitter("enum", f.value, ordering))

    fs.entry(ScalarShapeModel.Pattern).map { f =>
      result += RamlScalarEmitter("pattern", processRamlPattern(f))
    }

    fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.Minimum)
      .map(f => result += ValueEmitter("minimum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

    fs.entry(ScalarShapeModel.Maximum)
      .map(f => result += ValueEmitter("maximum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

    shape.examples.headOption.foreach(e => result += SingleExampleEmitter("example", e, ordering))

    fs.entry(ShapeModel.Default)
      .map(f => {
        result += EntryPartEmitter("default",
                                   DataNodeEmitter(shape.default, ordering)(spec.eh),
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

case class EnumValuesEmitter(key: String, value: Value, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val nodes = value.value.asInstanceOf[AmfArray].values.asInstanceOf[Seq[DataNode]]
    val emitters = nodes.map { d =>
      DataNodeEmitter(d, ordering)(spec.eh)
    }
    b.entry(key, _.list(traverse(ordering.sorted(emitters), _)))
  }

  override def position(): Position = pos(value.annotations)
}
