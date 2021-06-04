package amf.plugins.document.apicontract.parser.spec.async.emitters

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.Position
import amf.plugins.document.apicontract.contexts.SpecEmitterContext
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.shapes.models.CreativeWork
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiCreativeWorksEmitter(documentation: CreativeWork, ordering: SpecOrdering)(
    implicit val spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = documentation.fields
    fs.entry(CreativeWorkModel.Description).map(f => result += ValueEmitter("description", f))
    fs.entry(CreativeWorkModel.Url).map(f => result += ValueEmitter("url", f))
    b.entry(
      YNode("externalDocs"),
      _.obj(traverse(ordering.sorted(result), _))
    )
  }

  override def position(): Position = pos(documentation.annotations)
}
