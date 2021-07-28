package amf.grpc.internal.spec.emitter.document

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel, Document}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.plugins.syntax.{SourceCodeBlock, StringDocBuilder}
import amf.core.internal.render.BaseEmitters.pos
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext
import amf.grpc.internal.spec.emitter.domain.{GrpcEmitter, GrpcEnumEmitter, GrpcExtensionEmitter, GrpcMessageEmitter, GrpcServiceEmitter}
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}

class GrpcDocumentEmitter(document: BaseUnit, builder: StringDocBuilder) extends GrpcEmitter {

  val ctx = new GrpcEmitterContext(document)

  def emit(): Unit = {
    builder.doc { doc =>
      doc += ("syntax = \"proto3\";\n")
      emitReferences(doc)
      doc.list { l =>
        emitPackage(l)
        emitMessages(l)
        emitEnums(l)
        emitServices(l)
        emitExtensions(l)
        emitOptions(webApi, l, ctx)
      }
    }
  }

  def webApi: WebApi = document.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
  def endpoints: Seq[EndPoint] = webApi.endPoints

  def emitReferences(b: StringDocBuilder): Unit = {
    var checkDefaultGoogleDescriptor = false
    // we make the location relative to the location of the unit if we can
    val rootLocation = document.location().getOrElse("").replace("file://", "").split("/").dropRight(1).mkString("/") + "/"
    document.references.collect{ case r if r.location().isDefined => r }.foreach { ref =>
      val refLocation = ref.location().get.replace("file://", "").replace(rootLocation, "")
      if (refLocation.contains("google/protobuf/descriptor.proto\"")) {
        checkDefaultGoogleDescriptor = true
      }
      b += ("import \"" + refLocation + "\";")
    }
    if (!checkDefaultGoogleDescriptor && declaresOptions) {
      b += ("import \"google/protobuf/descriptor.proto\";")
    }
    if (document.references.nonEmpty || declaresOptions) {
      b += "\n"
    }

  }

  def declaresOptions: Boolean = {
    document match {
      case lib: DeclaresModel =>
        lib.declares.exists(_.isInstanceOf[CustomDomainProperty])
      case _                    => false
    }
  }

  def emitPackage(l: StringDocBuilder): SourceCodeBlock = {
    val nameField = if (document.pkg.option().isDefined) {
      document.pkg
    } else {
      webApi.name
    }
    val position = pos(nameField.annotations())
    val name = nameField.option().getOrElse("anonymous")
    val normalizedName = name.toLowerCase.replaceAll("-", "_").replaceAll( " ", "")
    l += (s"package $normalizedName;", position)
  }

  private def emitMessages(l: StringDocBuilder) = {
    ctx.topLevelMessages.map { s => GrpcMessageEmitter(s, l, ctx).emit() }
  }

  private def emitEnums(l: StringDocBuilder) = {
    ctx.topLevelEnums.map { s => GrpcEnumEmitter(s, l, ctx).emit() }
  }

  private def emitServices(l: StringDocBuilder): Unit = {
    endpoints.foreach { ep =>
      GrpcServiceEmitter(ep, l, ctx).emit()
    }
  }

  private def emitExtensions(l: StringDocBuilder): Unit = {
    ctx.extensions.groupBy(c => c.domain.head.value()).foreach { case (domain, extensions) =>
      GrpcExtensionEmitter(extensions, l, domain, ctx).emit()
    }
  }
}
