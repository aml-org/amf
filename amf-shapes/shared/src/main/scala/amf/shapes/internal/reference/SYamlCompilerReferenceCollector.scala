package amf.shapes.internal.reference

import amf.core.client.scala.parse.document.{
  CompilerReferenceCollector,
  Reference,
  ReferenceFragmentPartition,
  ReferenceKind,
  SYamlRefContainer
}
import org.yaml.model.YPart

class SYamlCompilerReferenceCollector() extends CompilerReferenceCollector {

  def +=(key: String, kind: ReferenceKind, node: YPart): Unit = {
    val (url, fragment) = ReferenceFragmentPartition(key)
    collector.get(url) match {
      case Some(reference: Reference) =>
        collector += (url, reference + SYamlRefContainer(kind, node, fragment))
      case None => collector += (url, new Reference(url, Seq(SYamlRefContainer(kind, node, fragment))))
    }
  }

}

object SYamlCompilerReferenceCollector {
  def apply(): SYamlCompilerReferenceCollector = new SYamlCompilerReferenceCollector()
}
