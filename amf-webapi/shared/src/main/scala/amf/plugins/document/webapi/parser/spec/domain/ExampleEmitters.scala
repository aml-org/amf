package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.model.document.BaseUnit
import amf.core.parser.{FieldEntry, Position}
import amf.core.utils._
import amf.plugins.document.webapi.contexts.{Oas3SpecEmitterFactory, RamlScalarEmitter, SpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, DataNodeEmitter}
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.metamodel.ExampleModel._
import amf.plugins.domain.shapes.models.Example
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument._
import org.yaml.model._
import org.yaml.parser.YamlParser

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class OasResponseExamplesEmitter(key: String, examples: Seq[Example], ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit =
    if (examples.nonEmpty) {
      if (spec.factory.isInstanceOf[Oas3SpecEmitterFactory]) {
        b.entry(key, _.obj(traverse(ordering.sorted(examples.map(Oas3ExampleValuesEmitter(_, ordering)(spec))), _)))
      } else {
        b.entry(key, _.obj(traverse(ordering.sorted(examples.map(OasResponseExampleEmitter(_, ordering)(spec))), _)))
      }
    }

  override def position(): Position = examples.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
}

object OasResponseExamplesEmitter {
  def apply(key: String, f: FieldEntry, ordering: SpecOrdering)(
      implicit spec: SpecEmitterContext): OasResponseExamplesEmitter =
    OasResponseExamplesEmitter(key, f.array.values.collect({ case e: Example => e }), ordering)(spec)
}

case class Oas3ExampleValuesEmitter(example: Example, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(keyName(example), _.obj(traverse(ordering.sorted(emitters), _)))
  }

  val emitters: Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()

    val fs = example.fields

    fs.entry(ExampleModel.Summary).foreach(f => results += RamlScalarEmitter("summary", f))
    fs.entry(ExampleModel.Description).foreach(f => results += RamlScalarEmitter("description", f))
    fs.entry(ExampleModel.ExternalValue).foreach(f => results += RamlScalarEmitter("externalValue", f))
    fs.entry(ExampleModel.StructuredValue)
      .foreach(f => {
        results += EntryPartEmitter("value",
                                    DataNodeEmitter(example.structuredValue, ordering)(spec.eh),
                                    position = pos(f.value.annotations))
      })
    results ++= AnnotationsEmitter(example, ordering).emitters

    results
  }

  protected def keyName(example: Example): String = {
    example.name.option().getOrElse("default")
  }

  override def position(): Position = pos(example.annotations)
}

case class OasResponseExampleEmitter(example: Example, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    example.fields
      .entry(ExampleModel.StructuredValue)
      .fold({
        example.raw.option().foreach { s =>
          b.entry(keyName(example), StringToAstEmitter(s).emit(_))
        }
      })(_ => {
        b.entry(keyName(example), DataNodeEmitter(example.structuredValue, ordering)(spec.eh).emit(_))
      })
  }

  protected def keyName(example: Example) = {
    if (spec.factory.isInstanceOf[Oas3SpecEmitterFactory]) {
      example.name.value()
    } else {
      example.mediaType.value()
    }
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
              spec.factory.tagToReferenceEmitter(l, examples.head.linkLabel.option(), references).emit(b))
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
    b.entry(example.name.value(), ExampleValuesEmitter(example, ordering).emit(_))
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
    val isExpanded = fs.fieldsMeta().exists(explicitFielMeta.contains(_)) || example.raw
      .option()
      .exists(_.contains("value"))

    if (isExpanded) {
      fs.entry(ExampleModel.DisplayName).foreach(f => results += RamlScalarEmitter("displayName", f))

      fs.entry(ExampleModel.Description).foreach(f => results += RamlScalarEmitter("description", f))

      if (fs.entry(ExampleModel.Strict)
            .isDefined && !fs.entry(ExampleModel.Strict).get.value.annotations.contains(classOf[SynthesizedField])) {
        fs.entry(ExampleModel.Strict).foreach(f => results += RamlScalarEmitter("strict", f))
      }

      fs.entry(ExampleModel.StructuredValue)
        .fold({
          example.raw.option().foreach { s =>
            results += StringToAstEmitter(s)
          }
        })(f => {
          results += EntryPartEmitter("value",
                                      DataNodeEmitter(example.structuredValue, ordering)(spec.eh),
                                      position = pos(f.value.annotations))
        })

      results ++= AnnotationsEmitter(example, ordering).emitters

    } else {
      fs.entry(ExampleModel.StructuredValue)
        .fold({
          example.raw.option().foreach { s =>
            results += StringToAstEmitter(s)
          }
        })(_ => results += DataNodeEmitter(example.structuredValue, ordering)(spec.eh))
    }

    results
  }

  private val emitters: Either[PartEmitter, Seq[EntryEmitter]] = entries match {
    case Seq(p: PartEmitter)                           => Left(p)
    case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
    case other =>
      spec.eh.violation(ResolutionValidation,
                        example.id,
                        None,
                        s"IllegalTypeDeclarations found: $other",
                        example.position(),
                        example.location())
      Right(Nil)
  }

  override def position(): Position = pos(example.annotations)
}

case class StringToAstEmitter(value: String) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    val node =
      if (value.isXml || value.isJson) // check the media type for yaml examples that has been setted in memory as value
        YNode(value) // i can't parse (yamlparser) the string because i will lose the multiline string like |- token.
      else
        YamlParser(value).parse().collectFirst { case d: YDocument => d }.map(_.node).getOrElse(YNode(value))

    emitNode(node, b)
  }
  private def emitNode(node: YNode, b: PartBuilder): Unit = {

    node.tagType match {
      case YType.Map =>
        val map = node.as[YMap]
        b.obj(e => map.entries.foreach(entry => e.entry(entry.key.as[String], p => emitNode(entry.value, p))))
      case YType.Seq =>
        val seq = node.as[Seq[YNode]]
        b.list(p => seq.foreach(emitNode(_, p)))
      case _ =>
        val scalar = node.as[YScalar]
        b += scalar.text
    }
  }

  override def position(): Position = Position.ZERO
}
