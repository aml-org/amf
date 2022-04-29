package amf.shapes.internal.spec.common.emitter

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DataNode
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.render.SpecOrdering
import org.yaml.model.YDocument

case class PayloadEmitter(dataNode: DataNode, ordering: SpecOrdering = SpecOrdering.Lexical)(implicit
    eh: AMFErrorHandler
) {
  def emitDocument(): YDocument = {
    val f: YDocument.PartBuilder => Unit = DataNodeEmitter(dataNode, ordering)(eh).emit
    YDocument(f)
  }
}
