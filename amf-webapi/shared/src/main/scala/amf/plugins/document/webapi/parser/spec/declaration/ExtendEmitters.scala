package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.domain.templates.ParametrizedDeclaration
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType, ParametrizedTrait}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class ExtendsEmitter(prefix: String, field: FieldEntry, ordering: SpecOrdering) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    val resourceTypes: Seq[ParametrizedResourceType] = field.array.values.collect {
      case a: ParametrizedResourceType => a
    }
    if (resourceTypes.nonEmpty) result += EndPointExtendsEmitter(prefix, resourceTypes, ordering)

    val traits: Seq[ParametrizedTrait] = field.array.values.collect { case a: ParametrizedTrait => a }
    if (traits.nonEmpty) result += TraitExtendsEmitter(prefix, traits, ordering)

    result
  }
}

case class TraitExtendsEmitter(prefix: String, traits: Seq[ParametrizedTrait], ordering: SpecOrdering)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      prefix + "is",
      _.list(traverse(ordering.sorted(traits.map(ParametrizedDeclarationEmitter(_, ordering))), _))
    )
  }

  override def position(): Position = traits.headOption.map(rt => pos(rt.annotations)).getOrElse(Position.ZERO)
}

case class EndPointExtendsEmitter(prefix: String, resourceTypes: Seq[ParametrizedResourceType], ordering: SpecOrdering)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      prefix + "type",
      ParametrizedDeclarationEmitter(resourceTypes.head, ordering).emit(_)
    )
  }

  override def position(): Position =
    resourceTypes.headOption.map(rt => pos(rt.annotations)).getOrElse(Position.ZERO)
}

case class ParametrizedDeclarationEmitter(declaration: ParametrizedDeclaration, ordering: SpecOrdering)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    if (declaration.variables.nonEmpty) {
      b.obj {
        _.entry(
          declaration.name,
          _.obj { b =>
            val result = declaration.variables.map(variable =>
              MapEntryEmitter(variable.name, variable.value, position = pos(variable.annotations)))

            traverse(ordering.sorted(result), b)
          }
        )
      }
    } else {
      raw(b, declaration.name)
    }
  }

  override def position(): Position = pos(declaration.annotations)
}
