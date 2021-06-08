package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.client.scala.model.domain.{AnyShape, Example}
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, ExampleModel}
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import amf.shapes.internal.spec.common.{JSONSchemaDraft6SchemaVersion, JSONSchemaDraft7SchemaVersion}
import amf.shapes.internal.spec.jsonschema.emitter.Draft6ExamplesEmitter

import scala.collection.mutable.ListBuffer

object OasAnyShapeEmitter {
  def apply(shape: AnyShape,
            ordering: SpecOrdering,
            references: Seq[BaseUnit],
            pointer: Seq[String] = Nil,
            schemaPath: Seq[(String, String)] = Nil,
            isHeader: Boolean = false)(implicit spec: OasLikeShapeEmitterContext): OasAnyShapeEmitter =
    new OasAnyShapeEmitter(shape, ordering, references, pointer, schemaPath, isHeader)(spec)
}

class OasAnyShapeEmitter(shape: AnyShape,
                         ordering: SpecOrdering,
                         references: Seq[BaseUnit],
                         pointer: Seq[String] = Nil,
                         schemaPath: Seq[(String, String)] = Nil,
                         isHeader: Boolean = false)(implicit spec: OasLikeShapeEmitterContext)
    extends OasShapeEmitter(shape, ordering, references, pointer, schemaPath) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    if (spec.options.isWithDocumentation) {
      shape.fields
        .entry(AnyShapeModel.Examples)
        .map { f =>
          val examples = spec.filterLocal(f.array.values.collect({ case e: Example => e }))
          if (examples.nonEmpty) {
            val (anonymous, named) =
              examples.partition(e => !e.fields.fieldsMeta().contains(ExampleModel.Name) && !e.isLink)
            result ++= examplesEmitters(anonymous.headOption, anonymous.drop(1) ++ named, isHeader)
          }
        }
      if (spec.schemaVersion isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
        shape.fields.entry(AnyShapeModel.Comment).map(c => result += ValueEmitter("$comment", c))
    }

    result ++= semanticContextEmitter(shape)

    super.emitters() ++ result
  }

  private def semanticContextEmitter(shape: AnyShape): List[EntryEmitter] = shape.semanticContext match {
    case Some(ctx) => List(SemanticContextEmitter(ctx, ordering))
    case _         => Nil
  }

  private def examplesEmitters(main: Option[Example], extensions: Seq[Example], isHeader: Boolean) =
    if (spec.schemaVersion.isBiggerThanOrEqualTo(JSONSchemaDraft6SchemaVersion))
      Draft6ExamplesEmitter(main.toSeq ++ extensions, ordering).emitters()
    else OasExampleEmitters.apply(isHeader, main, ordering, extensions, references).emitters()
}
