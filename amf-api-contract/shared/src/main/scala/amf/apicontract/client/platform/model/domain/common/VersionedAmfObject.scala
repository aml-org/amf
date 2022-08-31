package amf.apicontract.client.platform.model.domain.common

import amf.core.client.platform.model.StrField

/** All AmfObject supporting version
 */
trait VersionedAmfObject {

  /** Return AmfObject version. */
  def version: StrField

  /** Update AmfObject version. */
  def withVersion(version: String): this.type

}
