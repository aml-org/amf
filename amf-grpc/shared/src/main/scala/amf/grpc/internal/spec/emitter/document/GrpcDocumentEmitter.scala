package amf.grpc.internal.spec.emitter.document

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel, Document}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.plugins.syntax.{SourceCodeBlock, StringDocBuilder}
import amf.core.internal.render.BaseEmitters.pos
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext
import amf.grpc.internal.spec.emitter.domain._
import amf.shapes.internal.annotations.WellKnownType
import org.mulesoft.common.collections._

class GrpcDocumentEmitter(document: BaseUnit, builder: StringDocBuilder) extends GrpcEmitter {

  val ctx = new GrpcEmitterContext(document)

  def emit(): Unit = {
    builder.doc { doc =>
      doc += "syntax = \"proto3\";\n"
      emitPackage()
      emitReferences()
      doc.list { l =>
        emitMessages(l)
        emitEnums(l)
        emitServices(l)
        emitExtensions(l)
        emitOptions(webApi, l, ctx)
      }
    }
  }

  def webApi: WebApi           = document.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
  def endpoints: Seq[EndPoint] = webApi.endPoints

  private def emitPackage(): SourceCodeBlock = {
    val nameField      = if (document.pkg.nonEmpty) document.pkg else webApi.name
    val name           = nameField.option().getOrElse("anonymous")
    val normalizedName = name.toLowerCase.replaceAll("-", "_").replaceAll(" ", "")
    builder += s"package $normalizedName;\n"
  }

  private def emitReferences(): Unit = {
    // make the location relative to the location of the unit if we can
    val rootLocation =
      document.location().getOrElse("").replace("file://", "").split("/").dropRight(1).mkString("/") + "/"
    document.references.collect { case r if r.location().isDefined => r }.foreach { ref =>
      val refLocation = ref.location().get.replace("file://", "").replace(rootLocation, "")
      builder += ("import \"" + refLocation + "\";")
    }

    document.annotations
      .collect { case ann: WellKnownType => ann }
      .foreach { ann => builder += "import \"" + ann.typeName + "\";" }
    builder += "" // new line after imports
  }

  private def emitMessages(l: StringDocBuilder): Unit = {
    ctx.topLevelMessages.foreach { s => GrpcMessageEmitter(s, l, ctx).emit() }
  }

  private def emitEnums(l: StringDocBuilder): Unit = {
    ctx.topLevelEnums.foreach { s => GrpcEnumEmitter(s, l, ctx).emit() }
  }

  private def emitServices(l: StringDocBuilder): Unit = {
    endpoints.foreach { ep =>
      GrpcServiceEmitter(ep, l, ctx).emit()
    }
  }

  private def emitExtensions(l: StringDocBuilder): Unit = {
    ctx.extensions.legacyGroupBy(c => c.domain.head.value()).foreach { case (domain, extensions) =>
      GrpcExtensionEmitter(extensions, l, domain, ctx).emit()
    }
  }
}
