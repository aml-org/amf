package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.AmfElement
import amf.core.model.domain.templates.{ParametrizedDeclaration, VariableValue}
import amf.core.parser.{FieldEntry, Position}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.parser.spec.domain.SingleValueArrayEmitter
import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType, ParametrizedTrait}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YMap

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class ExtendsEmitter(field: FieldEntry, ordering: SpecOrdering, oasExtension: Boolean = false)(
    implicit eh: ErrorHandler) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    val resourceTypes: Seq[ParametrizedResourceType] = field.array.values.collect {
      case a: ParametrizedResourceType => a
    }
    if (resourceTypes.nonEmpty) result += EndPointExtendsEmitter(extension("type"), resourceTypes, ordering)

    val traits: Seq[ParametrizedTrait] = field.array.values.collect { case a: ParametrizedTrait => a }
    if (traits.nonEmpty) result += TraitExtendsEmitter(extension("is"), field, ordering)

    result
  }

  private def extension(key: String) = if (oasExtension) key.asOasExtension else key
}

case class TraitExtendsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering)(implicit eh: ErrorHandler)
    extends SingleValueArrayEmitter {
  override type Element = ParametrizedTrait

  override protected def collect(elements: Seq[AmfElement]): Seq[ParametrizedTrait] = elements.collect {
    case a: ParametrizedTrait => a
  }

  override def emit(element: ParametrizedTrait): PartEmitter = ParametrizedDeclarationEmitter(element, ordering)
}

case class EndPointExtendsEmitter(key: String, resourceTypes: Seq[ParametrizedResourceType], ordering: SpecOrdering)(
    implicit eh: ErrorHandler)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      ParametrizedDeclarationEmitter(resourceTypes.head, ordering)(eh).emit(_)
    )
  }

  override def position(): Position =
    resourceTypes.headOption.map(rt => pos(rt.annotations)).getOrElse(Position.ZERO)
}

case class ParametrizedDeclarationEmitter(declaration: ParametrizedDeclaration, ordering: SpecOrdering)(
    implicit eh: ErrorHandler)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    if (declaration.variables.nonEmpty) {
      b.obj {
        _.entry(
          declaration.name.value(),
          _.obj { b =>
            val result = declaration.variables.map(variable => VariableEmitter(variable, ordering)(eh))

            traverse(ordering.sorted(result), b)
          }
        )
      }
    } else {
      raw(b, declaration.name.value())
    }
  }

  override def position(): Position = pos(declaration.annotations)
}

case class VariableEmitter(variable: VariableValue, ordering: SpecOrdering)(implicit eh: ErrorHandler)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      variable.name.value(),
      p => {
        Option(variable.value).fold(p += YMap.empty)(v => { DataNodeEmitter(v, ordering).emit(p) })
      }
    )
  }

  override def position(): Position = pos(variable.annotations)
}
