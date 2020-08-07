package amf.plugins.document.webapi.resolution.stages

import amf.core.model.domain.DataNodeOps.adoptTree
import amf.core.model.domain.{AmfArray, AmfElement, DataNode, DomainElement}

trait InnerAdoption {

  def adoptInner(id: String, target: AmfElement, idTracker: IdTracker): AmfElement = {
    if (idTracker.notTracking(id)) {
      idTracker.track(id)
      target match {
        case array: AmfArray =>
          AmfArray(array.values.map(adoptInner(id, _, idTracker)), array.annotations)
        case dataNode: DataNode =>
          adoptTree(id, dataNode)
        case element: DomainElement =>
          element.adopted(id)
          element.fields.foreach {
            case (_, value) => adoptInner(element.id, value.value, idTracker)
          }
          element
        case _ => target
      }
    } else {
      target
    }
  }
}
