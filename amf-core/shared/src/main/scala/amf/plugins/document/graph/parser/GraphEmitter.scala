package amf.plugins.document.graph.parser

import amf.core.annotations._
import amf.core.emitter.RenderOptions
import amf.core.metamodel.Type.{Any, Array, Bool, EncodedIri, Iri, SortedArray, Str}
import amf.core.metamodel.document.{ModuleModel, SourceMapModel}
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ShapeModel}
import amf.core.metamodel.{Field, MetaModelTypeMapping, Obj, Type}
import amf.core.model.document.{BaseUnit, SourceMap}
import amf.core.model.domain.DataNodeOps.adoptTree
import amf.core.model.domain._
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.{Annotations, FieldEntry, Value}
import amf.core.utils._
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.graph.AMFGraphPlugin.platform
import org.mulesoft.common.time.SimpleDateTime
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * AMF Graph emitter
  */
object DefaultScalarEmitter {

  def scalar(b: PartBuilder, content: String, tag: YType = YType.Str, inArray: Boolean = false): Unit = {
    def emit(b: PartBuilder): Unit = {

      val tg: YType = fixTagIfNeeded(tag, content)

      b.obj(_.entry("@value", raw(_, content, tg)))
    }

    if (inArray) emit(b) else b.list(emit)
  }

  protected def fixTagIfNeeded(tag: YType, content: String): YType = {
    val tg: YType = tag match {
      case YType.Bool =>
        if (content != "true" && content != "false") {
          YType.Str
        } else {
          tag
        }
      case YType.Int =>
        try {
          content.toInt
          tag
        } catch {
          case _: NumberFormatException => YType.Str
        }
      case YType.Float =>
        try {
          content.toDouble
          tag
        } catch {
          case _: NumberFormatException => YType.Str
        }
      case _ => tag

    }
    tg
  }

  protected def raw(b: PartBuilder, content: String, tag: YType = YType.Str): Unit =
    b.+=(YNode(YScalar(content), tag))
}

class EmissionContext(val prefixes: mutable.Map[String, String],
                      var base: String,
                      val options: RenderOptions,
                      var declares: Boolean = false) {
  var counter: Int = 1

  private val declarations: mutable.LinkedHashSet[AmfElement] = mutable.LinkedHashSet.empty

  private val typeCount: IdCounter = new IdCounter()

  def nextTypeName: String = typeCount.genId("amf_inline_type")

  def declares(d: Boolean): this.type = {
    declares = d
    this
  }

  def +(element: AmfElement): this.type = {
    declarations += element
    this
  }

  def ++(elements: Iterable[AmfElement]): this.type = {
    declarations ++= elements
    this
  }

  def isDeclared(e: AmfElement): Boolean = declarations.contains(e)

  def isDeclared(id: String): Boolean =
    declarations.collect({ case obj: AmfObject if obj.id.equals(id) => obj }).nonEmpty

  def declared: Seq[AmfElement] = declarations.toSeq

  def shouldCompact: Boolean                           = options.isCompactUris
  protected def compactAndCollect(uri: String): String = Namespace.compactAndCollect(uri, prefixes)
  def emitIri(uri: String): String                     = if (shouldCompact) compactAndCollect(uri) else uri
  def emitId(uri: String): String                      = if (shouldCompact && uri.contains(base)) uri.replace(base, "") else uri
  def setupContextBase(location: String): Unit = {
    if (Option(location).isDefined) {
      base = if (location.replace("://", "").contains("/")) {
        val basePre = if (location.contains("#")) {
          location.split("#").head
        } else {
          location
        }
        val parts = basePre.split("/").dropRight(1)
        parts.mkString("/")
      } else {
        location.split("#").head
      }
    } else {
      base = ""
    }
  }

  def emitContext(b: EntryBuilder): Unit = {
    if (shouldCompact)
      b.entry("@context", _.obj { b =>
        b.entry("@base", base)
        prefixes.foreach {
          case (p, v) =>
            b.entry(p, v)
        }
      })
  }
}

object EmissionContext {
  def apply(unit: BaseUnit, options: RenderOptions) =
    new EmissionContext(mutable.Map(), unit.id, options)
}
