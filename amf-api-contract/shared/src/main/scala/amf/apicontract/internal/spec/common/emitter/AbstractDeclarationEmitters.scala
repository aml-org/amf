package amf.apicontract.internal.spec.common.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.templates.AbstractDeclaration
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.validation.RenderSideValidations.RenderValidation
import amf.shapes.internal.spec.common.emitter.ReferenceEmitterHelper.emitLinkOr
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
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
    val name = declaration.name.option() match {
      case Some(n) => n
      case _ =>
        spec.eh.violation(
          RenderValidation,
          declaration.id,
          None,
          s"Cannot declare abstract declaration without name $declaration",
          declaration.position(),
          declaration.location()
        )
        "default-name"
    }

    b.entry(
      name,
      AbstractDeclarationPartEmitter(declaration, ordering, references).emit(_)
    )
  }

  override def position(): Position = pos(declaration.annotations)
}

case class AbstractDeclarationPartEmitter(declaration: AbstractDeclaration,
                                          ordering: SpecOrdering,
                                          references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends PartEmitter {

  protected implicit val shapeCtx = AgnosticShapeEmitterContextAdapter(spec)

  override def emit(b: PartBuilder): Unit = {
    emitLinkOr(declaration, b, references) {
      var emitters =
        Option(declaration.dataNode).map(DataNodeEmitter(_, ordering)(spec.eh).emitters()).getOrElse(Nil)
      declaration.description.option().foreach { description =>
        emitters ++= Seq(MapEntryEmitter("usage", description, YType.Str, pos(declaration.description.annotations())))
      }
      b.obj { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    }
  }

  override def position(): Position = pos(declaration.annotations)
}
