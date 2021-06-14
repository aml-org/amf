package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.client.common.position.Position
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, raw, sourceOr, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.contexts.emitter.raml.RamlScalarEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.shapes.models.CreativeWork
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class RamlCreativeWorkItemsEmitter(documentation: CreativeWork, ordering: SpecOrdering, withExtention: Boolean)(
    implicit spec: ShapeEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    val fs = documentation.fields

    fs.entry(CreativeWorkModel.Url)
      .map(f => result += ValueEmitter(if (withExtention) "url".asRamlAnnotation else "url", f))

    fs.entry(CreativeWorkModel.Description).map(f => result += RamlScalarEmitter("content", f))

    fs.entry(CreativeWorkModel.Title).map(f => result += RamlScalarEmitter("title", f))

    result ++= AnnotationsEmitter(documentation, ordering).emitters
    ordering.sorted(result)
  }
}

case class RamlCreativeWorkEmitter(documentation: CreativeWork, ordering: SpecOrdering, withExtension: Boolean)(
    implicit spec: ShapeEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    sourceOr(
      documentation.annotations,
      b.obj(traverse(RamlCreativeWorkItemsEmitter(documentation, ordering, withExtension).emitters(), _))
    )
  }

  override def position(): Position = pos(documentation.annotations)
}

case class OasCreativeWorkItemsEmitter(document: CreativeWork, ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val fs     = document.fields
    val result = ListBuffer[EntryEmitter]()

    fs.entry(CreativeWorkModel.Url).map(f => result += ValueEmitter("url", f))

    fs.entry(CreativeWorkModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(CreativeWorkModel.Title).map(f => result += ValueEmitter("title".asOasExtension, f))

    result ++= AnnotationsEmitter(document, ordering).emitters

    ordering.sorted(result)
  }
}

case class OasCreativeWorkEmitter(document: CreativeWork, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    if (document.isLink)
      raw(b, document.linkLabel.option().getOrElse(document.linkTarget.get.id))
    else
      b.obj(traverse(OasCreativeWorkItemsEmitter(document, ordering).emitters(), _))
  }

  override def position(): Position = pos(document.annotations)
}

case class OasEntryCreativeWorkEmitter(key: String, documentation: CreativeWork, ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      documentation.annotations,
      b.entry(
        key,
        OasCreativeWorkEmitter(documentation, ordering).emit(_)
      )
    )
  }

  override def position(): Position = pos(documentation.annotations)
}
