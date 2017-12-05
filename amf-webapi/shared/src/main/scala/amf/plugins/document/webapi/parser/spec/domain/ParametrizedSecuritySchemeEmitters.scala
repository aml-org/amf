package amf.plugins.document.webapi.parser.spec.domain

import amf.core.emitter.{EntryEmitter, PartEmitter, SpecEmitterContext, SpecOrdering}
import amf.core.emitter.BaseEmitters._
import amf.core.model.domain.AmfScalar
import amf.core.parser.{FieldEntry, Position}
import amf.core.remote.{Oas, Raml}
import amf.plugins.document.webapi.parser.spec.declaration.RamlSecuritySettingsValuesEmitters
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.models.security.{OAuth2Settings, ParametrizedSecurityScheme}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

/**
  *
  */
case class ParametrizedSecuritiesSchemeEmitter(key: String, f: FieldEntry, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val schemes = f.array.values.collect { case p: ParametrizedSecurityScheme => p }

    b.entry(key, _.list(traverse(ordering.sorted(schemes.map(chooseParametrizedEmitter(_, ordering))), _)))
  }

  private def chooseParametrizedEmitter(parametrizedSecurityScheme: ParametrizedSecurityScheme,
                                        ordering: SpecOrdering): PartEmitter = {
    spec.vendor match {
      case Raml  => RamlParametrizedSecuritySchemeEmitter(parametrizedSecurityScheme, ordering)
      case Oas   => OasParametrizedSecuritySchemeEmitter(parametrizedSecurityScheme, ordering)
      case other => throw new IllegalArgumentException(s"Unsupported vendor $other for securedBy generation")
    }
  }
  override def position(): Position = pos(f.value.annotations)
}

case class OasParametrizedSecuritySchemeEmitter(parametrizedScheme: ParametrizedSecurityScheme, ordering: SpecOrdering)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    val fs = parametrizedScheme.fields

    fs.entry(ParametrizedSecuritySchemeModel.Settings) match {
      case Some(f) =>
        val scopes = f.element match {
          case settings: OAuth2Settings =>
            settings.scopes.map(s => ScalarEmitter(AmfScalar(s.name, s.annotations)))
          case _ => // we cant emit, if its not 2.0 isnt valid in oas.
            Nil

        }
        b.obj {
          _.entry(parametrizedScheme.name, _.list(traverse(ordering.sorted(scopes), _)))
        }

      case None =>
        b.obj(_.entry(parametrizedScheme.name, _.list(_ => {})))
    }
  }

  override def position(): Position = pos(parametrizedScheme.annotations)
}

case class RamlParametrizedSecuritySchemeEmitter(parametrizedScheme: ParametrizedSecurityScheme,
                                                 ordering: SpecOrdering)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {

    val fs = parametrizedScheme.fields

    fs.entry(ParametrizedSecuritySchemeModel.Settings) match {
      case Some(f) =>
        b.obj(
          _.entry(parametrizedScheme.name,
                  _.obj(traverse(ordering.sorted(RamlSecuritySettingsValuesEmitters(f, ordering).emitters), _))))
      case None =>
        b.scalar(parametrizedScheme.name)
    }

  }

  override def position(): Position = pos(parametrizedScheme.annotations)
}
