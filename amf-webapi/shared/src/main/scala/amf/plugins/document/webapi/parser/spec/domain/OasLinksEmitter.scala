package amf.plugins.document.webapi.parser.spec.domain

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.{Annotations, Position}
import amf.plugins.document.webapi.contexts.OasSpecEmitterContext
import amf.plugins.domain.webapi.metamodel.TemplatedLinkModel
import amf.plugins.domain.webapi.models.{IriTemplateMapping, TemplatedLink}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YType}

import scala.collection.mutable



case class OasLinksEmitter(links: Seq[TemplatedLink], ordering: SpecOrdering, references: Seq[BaseUnit], annotations: Annotations)(
  implicit spec: OasSpecEmitterContext)
  extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    sourceOr(
      annotations,
      b.obj { b =>
        val emitters = links.map(link => EntryPartEmitter(link.name.value(), OasLinkEmitter(link, ordering, references), YType.Str, pos(link.annotations)))
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(annotations)
}

case class OasLinkEmitter(link: TemplatedLink, ordering: SpecOrdering, references: Seq[BaseUnit])(
  implicit spec: OasSpecEmitterContext)
  extends PartEmitter {

  override def emit(p: PartBuilder): Unit = {
    val fs = link.fields

    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(TemplatedLinkModel.OperationId).map(f => result += ValueEmitter("operationId", f))
    fs.entry(TemplatedLinkModel.OperationRef).map(f => result += ValueEmitter("operationRef", f))
    fs.entry(TemplatedLinkModel.Description).map(f => result += ValueEmitter("description", f))

    fs
      .entry(TemplatedLinkModel.Mapping)
      .foreach { _ =>
        result += Oas3LinkParametersEmitter(link.mapping, ordering, references)
      }

    fs.entry(TemplatedLinkModel.Server)
      .map { _ =>
        result +=  EntryPartEmitter("server", OasServerEmitter(link.server, ordering), position = pos(link.server.annotations))
      }

    fs.entry(TemplatedLinkModel.RequestBody).map(f => result += ValueEmitter("requestBody", f))

    p.obj(traverse(ordering.sorted(result), _))
  }

  override def position(): Position = pos(link.annotations)
}

case class Oas3LinkParametersEmitter(mapping: Seq[IriTemplateMapping], ordering: SpecOrdering, references: Seq[BaseUnit])(
  implicit spec: OasSpecEmitterContext)
  extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {

    val entryEmitters = mapping.map { mapping =>
      MapEntryEmitter(mapping.templateVariable.value(), mapping.linkExpression.value(), position = pos(mapping.annotations))
    }
    b.entry("parameters", _.obj(traverse(ordering.sorted(entryEmitters), _)))
  }

  override def position(): Position = pos(mapping.head.annotations)
}
