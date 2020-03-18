package amf.plugins.document.webapi.parser.spec.raml

import amf.core.emitter.BaseEmitters.{MapEntryEmitter, pos}
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.annotations.ParsedJSONSchema
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{ExamplesEmitter, SimpleTypeEmitter}
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YType

import scala.collection.mutable

case class Raml08TypeEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends ExamplesEmitter {

  // TODO: Refactor -> Why does a TypeEmitter extend an ExampleEmitter?
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
