package amf.spec.domain

import amf.domain.FieldEntry
import amf.domain.security.ParametrizedSecurityScheme
import amf.metadata.domain.security.ParametrizedSecuritySchemeModel
import amf.parser.Position
import amf.remote.{Oas, Raml}
import amf.spec.{EntryEmitter, PartEmitter, SpecOrdering}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext
import amf.spec.declaration.RamlSecuritySettingsValuesEmitters

/**
  *
  */
case class ParametrizedSecuritiesSchemeEmitter(key: String, f: FieldEntry, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val schemes = f.array.values.collect({ case p: ParametrizedSecurityScheme => p })

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

    fs.entry(ParametrizedSecuritySchemeModel.Scopes) match {
      case Some(f) =>
        b.map(ArrayEmitter(parametrizedScheme.name, f, ordering).emit(_))
      case None =>
        b.map(_.entry(parametrizedScheme.name, _.list(b => {})))
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
        b.map(
          _.entry(parametrizedScheme.name,
                  _.map(traverse(ordering.sorted(RamlSecuritySettingsValuesEmitters(f, ordering).emitters), _))))
      case None =>
        b.scalar(parametrizedScheme.name)
    }

  }

  override def position(): Position = pos(parametrizedScheme.annotations)
}
