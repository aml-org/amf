package amf.shapes.internal.convert

import amf.aml.client.platform.model.domain.DialectDomainElement
import amf.aml.client.scala.model.domain
import amf.aml.internal.metamodel.domain.DialectDomainElementModel
import amf.core.internal.convert.UniqueInitializer
import amf.core.internal.metamodel.Obj
import amf.core.internal.remote.Platform
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.client.platform.model.domain.jsonldinstance.{JsonLDObject => ClientJsonLDObject}
import amf.shapes.internal.convert.ShapesRegister.{platform, register, shouldInitialize}
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDEntityModel

object JsonLDSchemaRegister extends UniqueInitializer with PlatformSecrets {

  def register(): Unit = register(platform)

  def register(platform: Platform): Unit = if (shouldInitialize) {
    val p: (Obj) => Boolean = (x: Obj) => x.isInstanceOf[JsonLDEntityModel]
    platform.registerWrapperPredicate(p) { case m: JsonLDObject =>
      new ClientJsonLDObject(m)
    }
  }

}
