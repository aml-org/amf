package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.async.emitters.Draft6ExamplesEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.{
  JSONSchemaDraft6SchemaVersion,
  JSONSchemaDraft7SchemaVersion
}
import amf.plugins.document.apicontract.parser.spec.oas.emitters.OasExampleEmitters
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, ExampleModel}
import amf.plugins.domain.shapes.models.{AnyShape, Example}

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

    super.emitters() ++ result
  }

  private def examplesEmitters(main: Option[Example], extensions: Seq[Example], isHeader: Boolean) =
    if (spec.schemaVersion.isBiggerThanOrEqualTo(JSONSchemaDraft6SchemaVersion))
      Draft6ExamplesEmitter(main.toSeq ++ extensions, ordering).emitters()
    else OasExampleEmitters.apply(isHeader, main, ordering, extensions, references).emitters()
}
