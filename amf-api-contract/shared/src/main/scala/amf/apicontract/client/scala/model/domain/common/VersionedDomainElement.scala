package amf.apicontract.client.scala.model.domain.common

import amf.apicontract.internal.metamodel.domain.common.VersionField
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{AmfObject, AmfScalar, DomainElement}
import amf.core.internal.parser.domain.{ScalarNode => ScalarNodeObj}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.YNode

/** All AmfObject supporting versioning
  */
trait VersionedAmfObject extends AmfObject with VersionField {

  protected def versionField: Field = Version

  /** Return AmfObject version. */
  def version: StrField = fields.field(versionField)

  def withVersion(node: YNode): this.type = withVersion(ScalarNodeObj(node))

  def withVersion(version: String, a: Annotations): this.type =
    set(versionField, AmfScalar(version, a), Annotations.inferred())

  /** Update AmfObject version. */
  def withVersion(versionNode: ScalarNodeObj): this.type = set(versionField, versionNode.text(), Annotations.inferred())

  def withVersion(version: String): this.type = set(versionField, version)

}
