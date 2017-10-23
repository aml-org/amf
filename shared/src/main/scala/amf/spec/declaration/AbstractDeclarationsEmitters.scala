package amf.spec.declaration

import amf.document.BaseUnit
import amf.domain.DomainElement
import amf.domain.`abstract`.AbstractDeclaration
import amf.parser.Position
import amf.spec.{EntryEmitter, PartEmitter, SpecOrdering}
import org.yaml.model.YDocument.EntryBuilder
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext

/**
  *
  */
case class AbstractDeclarationsEmitter(key: String,
                                       declarations: Seq[AbstractDeclaration],
                                       ordering: SpecOrdering,
                                       references: Seq[BaseUnit])(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(key, _.map { b =>
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
    val name = Option(declaration.name)
      .getOrElse(throw new Exception(s"Cannot declare abstract declaration without name $declaration"))

    b.entry(
      name,
      b => {
        if (declaration.isLink)
          declaration.linkTarget.foreach(l => spec.tagToReference(l, declaration.linkLabel, references).emit(b))
        else
          DataNodeEmitter(declaration.dataNode, ordering).emit(b)
      }
    )
  }

  override def position(): Position = pos(declaration.annotations)
}
