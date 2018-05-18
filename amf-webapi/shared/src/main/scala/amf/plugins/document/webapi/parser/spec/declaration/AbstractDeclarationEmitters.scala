package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.emitter.BaseEmitters._
import amf.core.model.document.BaseUnit
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.YType

/**
  *
  */
case class AbstractDeclarationsEmitter(key: String,
                                       declarations: Seq[AbstractDeclaration],
                                       ordering: SpecOrdering,
                                       references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(key, _.obj { b =>
      traverse(ordering.sorted(declarations.map(d => AbstractDeclarationEmitter(d, ordering, references))), b)
    })
  }

  override def position(): Position = declarations.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
}

case class AbstractDeclarationEmitter(declaration: AbstractDeclaration,
                                      ordering: SpecOrdering,
                                      references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val name = declaration.name
      .option()
      .getOrElse(throw new Exception(s"Cannot declare abstract declaration without name $declaration"))

    b.entry(
      name,
      b => {
        if (declaration.isLink)
          declaration.linkTarget.foreach(l =>
            spec.factory.tagToReferenceEmitter(l, declaration.linkLabel.option(), references).emit(b))
        else {
          var emitters = DataNodeEmitter(declaration.dataNode, ordering).emitters()
          declaration.description.option().foreach { description =>
            emitters ++= Seq(
              MapEntryEmitter("usage", description, YType.Str, pos(declaration.description.annotations())))
          }
          b.obj { b =>
            ordering.sorted(emitters).foreach(_.emit(b))
          }
        }
      }
    )
  }

  override def position(): Position = pos(declaration.annotations)
}
