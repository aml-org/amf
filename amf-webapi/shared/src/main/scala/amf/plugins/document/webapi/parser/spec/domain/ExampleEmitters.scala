package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.model.document.BaseUnit
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.{RamlScalarEmitter, SpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, DataNodeEmitter}
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.metamodel.ExampleModel._
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.YDocument._

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class OasResponseExamplesEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val examples = f.array.values.collect({ case e: Example => e })
    b.entry(key, _.obj(traverse(ordering.sorted(examples.map(OasResponseExampleEmitter(_, ordering))), _)))
  }

  override def position(): Position = f.array.values.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
}

case class OasResponseExampleEmitter(example: Example, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(example.mediaType, DataNodeEmitter(example.structuredValue, ordering).emit(_))
  }

  override def position(): Position = pos(example.annotations)
}

case class MultipleExampleEmitter(key: String,
                                  examples: Seq[Example],
                                  ordering: SpecOrdering,
                                  references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    if (examples.nonEmpty) {
      b.entry(
        key,
        b => {
          if (examples.head.isLink)
            examples.head.linkTarget.foreach(l =>
              spec.factory.tagToReferenceEmitter(l, examples.head.linkLabel, references).emit(b))
          else {
            val emitters = examples.map(NamedExampleEmitter(_, ordering))
            b.obj(traverse(ordering.sorted(emitters), _))
          }

        }
      )
    }
  }

  override def position(): Position = examples.headOption.map(h => pos(h.annotations)).getOrElse(Position.ZERO)
}

case class SingleExampleEmitter(key: String, example: Example, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {

    b.entry(
      key,
      p =>
        valueEmitter match {
          case Left(entry) => p.obj(entry.emit)
          case Right(map)  => map.emit(p)
      }
    )
  }

  private val valueEmitter: Either[EntryEmitter, PartEmitter] = example.fields.entry(Name) match {
    case Some(_) => Left(NamedExampleEmitter(example, ordering))
    case _       => Right(ExampleValuesEmitter(example, ordering))
  }

  override def position(): Position = pos(example.annotations)

}

case class NamedExampleEmitter(example: Example, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(example.name, ExampleValuesEmitter(example, ordering).emit(_))
  }

  override def position(): Position = pos(example.annotations)
}

case class ExampleValuesEmitter(example: Example, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = emitters match {
    case Left(p)       => p.emit(b)
    case Right(values) => b.obj(traverse(ordering.sorted(values), _))
  }

  val entries: Seq[Emitter] = {
    val results = ListBuffer[Emitter]()

    val fs = example.fields
    // This should remove Strict if we auto-generated it when parsing the model
    val explicitFielMeta = List(ExampleModel.Strict,
                                ExampleModel.Description,
                                ExampleModel.DisplayName,
                                ExampleModel.CustomDomainProperties).filter { f =>
      fs.entry(f) match {
        case Some(entry) => !entry.value.annotations.contains(classOf[SynthesizedField])
        case None        => false
      }
    }
    val isExpanded = fs.fieldsMeta().exists(explicitFielMeta.contains(_)) || example.value
      .contains("value")

    if (isExpanded) {
      fs.entry(ExampleModel.DisplayName).foreach(f => results += RamlScalarEmitter("displayName", f))

      fs.entry(ExampleModel.Description).foreach(f => results += RamlScalarEmitter("description", f))

      if (fs.entry(ExampleModel.Strict)
            .isDefined && !fs.entry(ExampleModel.Strict).get.value.annotations.contains(classOf[SynthesizedField])) {
        fs.entry(ExampleModel.Strict).foreach(f => results += RamlScalarEmitter("strict", f))
      }

      fs.entry(ExampleModel.StructuredValue)
        .foreach(f => {
          results += EntryPartEmitter("value",
                                      DataNodeEmitter(example.structuredValue, ordering),
                                      position = pos(f.value.annotations))
        })

      results ++= AnnotationsEmitter(example, ordering).emitters

    } else {
      fs.entry(ExampleModel.StructuredValue)
        .foreach(f => results += DataNodeEmitter(example.structuredValue, ordering))
    }

    results
  }

  private val emitters: Either[PartEmitter, Seq[EntryEmitter]] = entries match {
    case Seq(p: PartEmitter)                           => Left(p)
    case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
    case other                                         => throw new Exception(s"IllegalTypeDeclarations found: $other")
  }

  override def position(): Position = pos(example.annotations)
}
