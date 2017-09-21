package amf.compiler

import org.yaml.model.{YDocument, YMapEntry}
import amf.parser.{YMapOps, YValueOps}

/**
  * Created by hernan.najles on 9/20/17.
  */
class OasHeader private[compiler] (val key: String, val value: String) {}

object OasHeader {

  val Oas_20: OasHeader = new OasHeader("swagger", "2.0")

  def apply(root: Root): Option[OasHeader] = {
    val entryOption = root.document.value.flatMap(v => v.toMap.key("swagger"))
    entryOption.flatMap({
      case e if e.key.value.toScalar.text.equals(Oas_20.key) && e.value.value.toScalar.text.equals(Oas_20.value) =>
        Some(Oas_20)
      case _ => None
    })
  }
}
