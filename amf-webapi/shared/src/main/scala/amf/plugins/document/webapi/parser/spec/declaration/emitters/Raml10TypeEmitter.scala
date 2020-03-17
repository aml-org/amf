package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, EncodesModel, ExternalFragment}
import amf.core.model.domain.{Linkable, RecursiveShape, Shape}
import amf.core.parser.Position
import amf.plugins.document.webapi.annotations.ParsedJSONSchema
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.domain.shapes.models._
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.YNode

case class Raml10TypeEmitter(shape: Shape,
                             ordering: SpecOrdering,
                             ignored: Seq[Field] = Nil,
                             references: Seq[BaseUnit],
                             forceEntry: Boolean = false)(implicit spec: RamlSpecEmitterContext) {
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
          case _ if forceEntry =>
            Seq(spec.localReferenceEntryEmitter("type", shape))
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
        declaration.emitters.RamlArrayShapeEmitter(copiedArray, ordering, references).emitters()
      case tuple: TupleShape =>
        val copiedTuple = tuple.copy(fields = tuple.fields.filter(f => !ignored.contains(f._1)))
        RamlTupleShapeEmitter(copiedTuple, ordering, references).emitters()
      case matrix: MatrixShape =>
        val copiedMatrix = matrix.copy(fields = matrix.fields.filter(f => !ignored.contains(f._1)))
        declaration.emitters.RamlArrayShapeEmitter(copiedMatrix.toArrayShape, ordering, references).emitters()
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
