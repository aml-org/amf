package amf.plugins.document.webapi.parser.spec.domain

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{SpecOrdering, EntryEmitter, PartEmitter}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position.ZERO
import amf.core.parser.{Annotations, Position}
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.OasTagToReferenceEmitter
import amf.plugins.domain.webapi.metamodel.TemplatedLinkModel
import amf.plugins.domain.webapi.models.{TemplatedLink, IriTemplateMapping}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YType

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class OasLinksEmitter(links: Seq[TemplatedLink],
                           ordering: SpecOrdering,
                           references: Seq[BaseUnit],
                           annotations: Annotations)(implicit spec: OasSpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    sourceOr(
      annotations,
      b.obj { b =>
        val emitters = links.map(
          link =>
            EntryPartEmitter(link.name.value(),
                             OasLinkPartEmitter(link, ordering, references),
                             YType.Str,
                             pos(link.annotations)))
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(annotations)
}

case class OasLinkPartEmitter(link: TemplatedLink, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends PartEmitter {

  override def emit(p: PartBuilder): Unit = {
    if (link.isLink) {
      link.linkTarget.foreach { l =>
        OasTagToReferenceEmitter(l, link.linkLabel.option()).emit(p)
      }
    } else {
      p.obj(traverse(ordering.sorted(emitters), _))
    }
  }

  val emitters: ListBuffer[EntryEmitter] = {
    val fs = link.fields

    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(TemplatedLinkModel.OperationId).map(f => result += ValueEmitter("operationId", f))
    fs.entry(TemplatedLinkModel.OperationRef).map(f => result += ValueEmitter("operationRef", f))
    fs.entry(TemplatedLinkModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(TemplatedLinkModel.Mapping)
      .foreach { _ =>
        result += Oas3LinkParametersEmitter(link.mapping, ordering, references)
      }

    fs.entry(TemplatedLinkModel.Server)
      .map { _ =>
        result += EntryPartEmitter("server",
                                   OasServerEmitter(link.server, ordering),
                                   position = pos(link.server.annotations))
      }

    fs.entry(TemplatedLinkModel.RequestBody).map(f => result += ValueEmitter("requestBody", f))
    result
  }

  override def position(): Position = pos(link.annotations)
}

case class Oas3LinkDeclarationEmitter(links: Seq[TemplatedLink], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "links",
      _.obj(b => {
        links.foreach(link => {
          b.entry(link.name.value(), OasLinkPartEmitter(link, ordering, references).emit(_))
        })
      })
    )
  }

  override def position(): Position = links.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
}

case class Oas3LinkParametersEmitter(mapping: Seq[IriTemplateMapping],
                                     ordering: SpecOrdering,
                                     references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {

    val entryEmitters = mapping.map { mapping =>
      MapEntryEmitter(mapping.templateVariable.value(),
                      mapping.linkExpression.value(),
                      position = pos(mapping.annotations))
    }
    b.entry("parameters", _.obj(traverse(ordering.sorted(entryEmitters), _)))
  }

  override def position(): Position = pos(mapping.head.annotations)
}
