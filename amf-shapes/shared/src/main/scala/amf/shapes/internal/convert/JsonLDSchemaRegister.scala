package amf.shapes.internal.convert

import amf.core.internal.convert.UniqueInitializer
import amf.core.internal.metamodel.Obj
import amf.core.internal.remote.Platform
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.platform.model.document.JsonLDInstanceDocument
import amf.shapes.client.platform.model.domain.jsonldinstance.{JsonLDObject => ClientJsonLDObject}
import amf.shapes.client.scala.model.document.{JsonLDInstanceDocument => InternalJsonLDInstanceDocument}
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.internal.document.metamodel.JsonLDInstanceDocumentModel
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDEntityModel

object JsonLDSchemaRegister extends UniqueInitializer with PlatformSecrets {

  def register(): Unit = register(platform)

  def register(platform: Platform): Unit = if (shouldInitialize) {

    platform.registerWrapper(JsonLDInstanceDocumentModel) { case s: InternalJsonLDInstanceDocument =>
      new JsonLDInstanceDocument(s)
    }
    val p: (Obj) => Boolean = (x: Obj) => x.isInstanceOf[JsonLDEntityModel]
    platform.registerWrapperPredicate(p) { case m: JsonLDObject =>
      new ClientJsonLDObject(m)
    }
  }

}
