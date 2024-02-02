package amf.apicontract.internal.spec.async.emitters.domain

import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.{YDocument, YNode}

case class DefaultContentTypeEmitter(f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val maybeString: Option[String] = getValue
    maybeString.foreach(contentType => b.entry("defaultContentType", YNode(contentType)))

  }

  private def getValue: Option[String] = {
    f.value.value match {
      case array: AmfArray =>
        array.values
          .headOption
          .map(f => f.asInstanceOf[AmfScalar].value.toString)
      case _ =>
        None
    }
  }

  override def position(): Position = pos(f.value.annotations)
}
