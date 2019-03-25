package amf.core

import java.io.StringWriter

import amf.client.plugins.AMFDocumentPlugin
import amf.core.benchmark.ExecutionLog
import amf.core.emitter.RenderOptions
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.parser.SyamlParsedDocument
import amf.core.rdf.RdfModelDocument
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.{Platform, Vendor}
import amf.core.services.RuntimeSerializer
import amf.plugins.document.graph.AMFGraphPlugin.platform
import amf.plugins.document.graph.parser.JsonLdEmitter
import amf.plugins.syntax.RdfSyntaxPlugin
import org.mulesoft.common.io.Output
import org.mulesoft.common.io.Output._
import org.yaml.builder.{DocBuilder, JsonOutputBuilder, YDocumentBuilder}

import scala.concurrent.{ExecutionContext, Future}

class AMFSerializer(unit: BaseUnit, mediaType: String, vendor: String, options: RenderOptions) {

  def renderAsYDocument(): SyamlParsedDocument = {
    val domainPlugin = getDomainPlugin
    val builder      = new YDocumentBuilder
    if (domainPlugin.emit(unit, builder, options)) SyamlParsedDocument(builder.result)
    else throw new Exception(s"Error unparsing syntax $mediaType with domain plugin ${domainPlugin.ID}")
  }

  /** Render to doc builder. */
  def renderToBuilder[T](builder: DocBuilder[T])(implicit executor: ExecutionContext): Future[Unit] = Future {
    if (vendor == Vendor.AMF.name) JsonLdEmitter.emit(unit, builder, options)
  }

  /** Print ast to writer. */
  def renderToWriter[W: Output](writer: W)(implicit executor: ExecutionContext): Future[Unit] = Future(render(writer))

  /** Print ast to string. */
  def renderToString(implicit executor: ExecutionContext): Future[String] = Future(render())

  /** Print ast to file. */
  def renderToFile(remote: Platform, path: String)(implicit executor: ExecutionContext): Future[Unit] =
    renderToString.map(remote.write(path, _))

  private def render[W: Output](writer: W): Unit = {
    ExecutionLog.log(s"AMFSerializer#render: Rendering to $mediaType ($vendor file) ${unit.location()}")
    if (vendor == Vendor.AMF.name) {
      if (!options.isAmfJsonLdSerilization) parseRdf(writer)
      else {
        val b = JsonOutputBuilder[W](writer, options.isPrettyPrint)
        JsonLdEmitter.emit(unit, b, options)
      }
      return
    }

    val ast = renderAsYDocument()
    AMFPluginsRegistry.syntaxPluginForMediaType(mediaType) match {
      case Some(syntaxPlugin) => syntaxPlugin.unparse(mediaType, ast, writer)
      case None if unit.isInstanceOf[ExternalFragment] =>
        writer.append(unit.asInstanceOf[ExternalFragment].encodes.raw.value())
      case _ => throw new Exception(s"Unsupported media type $mediaType and vendor $vendor")
    }
  }

  private def parseRdf[W: Output](writer: W): Unit =
    platform.rdfFramework match {
      case Some(r) =>
        val d = RdfModelDocument(r.unitToRdfModel(unit, options))
        RdfSyntaxPlugin.unparse(mediaType, d, writer)
      case _ => None
    }

  private def render(): String = {
    val w = new StringWriter
    render(w)
    w.toString
  }

  protected def findDomainPlugin(): Option[AMFDocumentPlugin] =
    AMFPluginsRegistry.documentPluginForVendor(vendor).find { plugin =>
      plugin.documentSyntaxes.contains(mediaType) && plugin.canUnparse(unit)
    } match {
      case Some(domainPlugin) => Some(domainPlugin)
      case None               => AMFPluginsRegistry.documentPluginForMediaType(mediaType).find(_.canUnparse(unit))
    }

  private def getDomainPlugin: AMFDocumentPlugin =
    findDomainPlugin().getOrElse {
      throw new Exception(
        s"Cannot serialize domain model '${unit.location()}' for detected media type $mediaType and vendor $vendor")
    }
}

object AMFSerializer {
  def init(): Unit = {
    if (RuntimeSerializer.serializer.isEmpty) {
      RuntimeSerializer.register(new RuntimeSerializer {
        override def dump(unit: BaseUnit, mediaType: String, vendor: String, options: RenderOptions): String =
          new AMFSerializer(unit, mediaType, vendor, options).render()

        override def dumpToFile(platform: Platform,
                                file: String,
                                unit: BaseUnit,
                                mediaType: String,
                                vendor: String,
                                options: RenderOptions): Future[Unit] = {
          import scala.concurrent.ExecutionContext.Implicits.global
          new AMFSerializer(unit, mediaType, vendor, options).renderToFile(platform, file)
        }
      })
    }
  }
}
