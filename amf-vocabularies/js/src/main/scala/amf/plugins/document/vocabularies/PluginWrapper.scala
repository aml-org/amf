package amf.plugins.document.vocabularies

import amf.core.metamodel.Obj
import amf.core.unsafe.PlatformSecrets
import amf.model.DomainEntity
import amf.plugins.document.vocabularies.metamodel.domain.DialectEntityModel
import amf.plugins.document.vocabularies.model.domain

object PluginWrapper extends PlatformSecrets{

  def init() = {
    val p: (Obj) => Boolean = (x: Obj) => x.isInstanceOf[DialectEntityModel]
    platform.registerWrapperPredicate(p) {
      case m: domain.DomainEntity => DomainEntity(m)
    }
  }

}
