package amf.tools

import amf.core.metamodel.Obj
import org.reflections.Reflections
import scala.collection.JavaConverters._

object ObjLoader {

  def loadObjs(reflections: List[Reflections]): List[Obj] = {
    reflections.flatMap(metaObjects)
  }

  def metaObjects(reflections: Reflections): List[Obj] = {
    reflections.getSubTypesOf(classOf[Obj]).asScala.toList.map(x => x.getName).flatMap { className =>
      if (className.endsWith("$")) {
        val singleton = Class.forName(className)
        singleton.getField("MODULE$").get(singleton) match {
          case modelObject: Obj => Some(modelObject)
          case other            => None
        }
      } else {
        None
      }
    }
  }
}
