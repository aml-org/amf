package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.JSONSchemaDraft7SchemaVersion
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, ExampleModel}
import amf.plugins.domain.shapes.models.{AnyShape, Example}

import scala.collection.mutable.ListBuffer

object OasAnyShapeEmitter {
  def apply(shape: AnyShape,
            ordering: SpecOrdering,
            references: Seq[BaseUnit],
            pointer: Seq[String] = Nil,
            schemaPath: Seq[(String, String)] = Nil,
            isHeader: Boolean = false)(implicit spec: OasLikeSpecEmitterContext): OasAnyShapeEmitter =
    new OasAnyShapeEmitter(shape, ordering, references, pointer, schemaPath, isHeader)(spec)
}

class OasAnyShapeEmitter(shape: AnyShape,
                         ordering: SpecOrdering,
                         references: Seq[BaseUnit],
                         pointer: Seq[String] = Nil,
                         schemaPath: Seq[(String, String)] = Nil,
                         isHeader: Boolean = false)(implicit spec: OasLikeSpecEmitterContext)
    extends OasShapeEmitter(shape, ordering, references, pointer, schemaPath) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    if (spec.options.isWithDocumentation) {
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
      if (spec.schemaVersion isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
        shape.fields.entry(AnyShapeModel.Comment).map(c => result += ValueEmitter("$comment", c))
    }

    super.emitters() ++ result
  }

  private def examplesEmitters(main: Option[Example], extensions: Seq[Example], isHeader: Boolean) =
    spec.factory.exampleEmitter(isHeader, main, ordering, extensions, references).emitters()
}
