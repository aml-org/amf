package amf.core.annotations

import amf.core.model.domain.SerializableAnnotation
import org.yaml.model.YNode.MutRef
import org.yaml.model.YTag

case class ExternalSource(origTag: YTag, origTarget: String) extends SerializableAnnotation {

  /** Extension name. */
  override val name: String = "external-source"

  /** Value as string. */
  override val value: String = origTag.text + "," + origTarget
}

object ExternalSource {
  def apply(mut: MutRef): ExternalSource = new ExternalSource(mut.origTag, mut.origValue.toString)
}
