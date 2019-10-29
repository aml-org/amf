package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{NullSecurity, SingleValueArray}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.domain.{AmfElement, AmfScalar}
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.RamlSecuritySettingsValuesEmitters
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.models.security.{OAuth2Settings, ParametrizedSecurityScheme}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YNode, YScalar, YType}

/**
  *
  */
trait SingleValueArrayEmitter extends EntryEmitter {

  type Element <: AmfElement

  val key: String
  val f: FieldEntry
  val ordering: SpecOrdering

  override def emit(b: EntryBuilder): Unit = {
    val single = f.value.value.annotations.contains(classOf[SingleValueArray])

    sourceOr(
      f.value, {
        val elements = collect(f.array.values)

        if (single) {
          b.entry(key, emit(elements.head).emit(_))
        } else {
          b.entry(key, _.list(traverse(ordering.sorted(elements.map(emit)), _)))
        }
      }
    )
  }

  protected def collect(elements: Seq[AmfElement]): Seq[Element]

  def emit(element: Element): PartEmitter

  override def position(): Position = pos(f.value.annotations)
}

case class ParametrizedSecuritiesSchemeEmitter(key: String, f: FieldEntry, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends SingleValueArrayEmitter {

  override type Element = ParametrizedSecurityScheme

  override def emit(scheme: ParametrizedSecurityScheme): PartEmitter =
    spec.factory.parametrizedSecurityEmitter(scheme, ordering)

  override protected def collect(elements: Seq[AmfElement]): Seq[ParametrizedSecurityScheme] =
    f.array.values.collect { case p: ParametrizedSecurityScheme => p }
}

abstract class ParametrizedSecuritySchemeEmitter(parametrizedScheme: ParametrizedSecurityScheme,
                                                 ordering: SpecOrdering)
    extends PartEmitter {}

case class OasParametrizedSecuritySchemeEmitter(parametrizedScheme: ParametrizedSecurityScheme, ordering: SpecOrdering)
    extends ParametrizedSecuritySchemeEmitter(parametrizedScheme, ordering) {
  override def emit(b: PartBuilder): Unit = {
    val fs = parametrizedScheme.fields

    fs.entry(ParametrizedSecuritySchemeModel.Settings) match {
      case Some(f) =>
        val scopes = f.element match {
          case settings: OAuth2Settings =>
            settings.flows.headOption.toList
              .flatMap { flow =>
                flow.scopes.map(s => ScalarEmitter(AmfScalar(s.name.value(), s.annotations)))
              }
          case _ => // we cant emit, if its not 2.0 isnt valid in oas.
            Nil

        }
        b.obj {
          _.entry(parametrizedScheme.name.value(), _.list(traverse(ordering.sorted(scopes), _)))
        }

      case None =>
        b.obj(_.entry(parametrizedScheme.name.value(), _.list(_ => {})))
    }
  }

  override def position(): Position = pos(parametrizedScheme.annotations)
}

case class RamlParametrizedSecuritySchemeEmitter(parametrizedScheme: ParametrizedSecurityScheme,
                                                 ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends ParametrizedSecuritySchemeEmitter(parametrizedScheme, ordering) {
  override def emit(b: PartBuilder): Unit = {

    val fs = parametrizedScheme.fields

    fs.entry(ParametrizedSecuritySchemeModel.Settings) match {
      case Some(f) =>
        b.obj(
          _.entry(parametrizedScheme.name.value(),
                  _.obj(traverse(ordering.sorted(RamlSecuritySettingsValuesEmitters(f, ordering).emitters), _))))
      case None if parametrizedScheme.annotations.contains(classOf[NullSecurity]) =>
        b += YNode(YScalar.Null, YType.Null)
      case None =>
        b += parametrizedScheme.name.value()
    }
  }

  override def position(): Position = pos(parametrizedScheme.annotations)
}
