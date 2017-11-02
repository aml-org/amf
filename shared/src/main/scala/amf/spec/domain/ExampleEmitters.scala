package amf.spec.domain

import amf.document.BaseUnit
import amf.domain.Annotation.SynthesizedField
import amf.domain.{Example, FieldEntry}
import amf.metadata.domain.ExampleModel._
import amf.parser.Position
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext
import amf.spec.declaration.AnnotationsEmitter
import amf.spec.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import org.yaml.model.YDocument._
import org.yaml.model.{YDocument, YMap}
import org.yaml.parser.YamlParser

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
    b.entry(example.mediaType, StringToAstEmitter(example.value).emit(_))
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
            examples.head.linkTarget.foreach(l => spec.tagToReference(l, examples.head.linkLabel, references).emit(b))
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
    val explicitFielMeta = List(Strict, Description, DisplayName).filter { f =>
      fs.entry(f) match {
        case Some(entry) => !entry.value.annotations.contains(classOf[SynthesizedField])
        case None        => false
      }
    }
    val isExpanded = fs.fieldsMeta().exists(explicitFielMeta.contains(_)) || example.value
      .contains("value")

    if (isExpanded) {
      fs.entry(DisplayName).foreach(f => results += ValueEmitter("displayName", f))

      fs.entry(Description).foreach(f => results += ValueEmitter("description", f))

      if (fs.entry(Strict).isDefined && !fs.entry(Strict).get.value.annotations.contains(classOf[SynthesizedField])) {
        fs.entry(Strict).foreach(f => results += ValueEmitter("strict", f))
      }

      fs.entry(Value)
        .foreach(f => {
          results += EntryPartEmitter("value",
                                      StringToAstEmitter(f.value.toString),
                                      position = pos(f.value.annotations))
        })

      results ++= AnnotationsEmitter(example, ordering).emitters

    } else {
      fs.entry(Value).foreach(f => results += StringToAstEmitter(f.value.toString))
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

case class StringToAstEmitter(value: String) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    val parts = YamlParser(value).parse()
    parts.collect({ case d: YDocument => d }).headOption.map(_.node) match {
      case Some(node) =>
        node.value match {
          case m: YMap => b.obj(e => m.entries.foreach(m => e.entry(m.key, m.value)))
          case _       => b += node
        }
      case _ => throw new IllegalStateException(s"Could not parse string example $value")
    }

  }

  override def position(): Position = Position.ZERO
}
