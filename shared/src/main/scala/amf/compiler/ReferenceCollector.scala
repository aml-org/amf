package amf.compiler

import amf.dialects.DialectRegistry
import amf.document.BaseUnit
import amf.parser.{YMapOps, YValueOps}
import amf.remote._
import amf.validation.Validation
import org.yaml.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

/**
  * Reference collector. Ideally references should be collected while parsing to avoid an unnecessary iteration.
  */
class ReferenceCollector(document: YDocument, vendor: Vendor) {

  private val references = new ArrayBuffer[Reference]

  def traverse(isRamlOverlayOrExtension: Boolean): Seq[Reference] = {
    if (vendor != Amf) {
      libraries(document)
      links(document)
      if (isRamlOverlayOrExtension) overlaysAndExtensions(document)
    }
    references
  }

  private def overlaysAndExtensions(document: YDocument): Unit = {
    document.node.value match {
      case map: YMap =>
        val ext = vendor match {
          case Raml => Some("extends")
          case Oas  => Some("x-extends")
          case _    => None
        }

        ext.foreach { u =>
          map
            .key(u)
            .foreach(entry =>
              entry.value.value match {
                case _: YScalar => extension(entry)
                case _          => throw new Exception(s"Expected scalar but found: ${entry.value}")
            })
        }
    }
  }

  private def extension(entry: YMapEntry) = {
    references += Reference(entry.value, Extension, entry)
  }

  private def links(part: YPart): Unit = {
    vendor match {
      case Raml => ramlLinks(part)
      case Oas  => oasLinks(part)
      case _    => // Ignore
    }
  }

  private def libraries(document: YDocument): Unit = {
    document.value.foreach({
      case map: YMap =>
        val uses = vendor match {
          case Raml => Some("uses")
          case Oas  => Some("x-uses")
          case _    => None
        }
        uses.foreach(u => {
          map
            .key(u)
            .foreach(entry => {
              entry.value.value match {
                case libraries: YMap => libraries.entries.foreach(library)
                case _               => throw new Exception(s"Expected map but found: ${entry.value}")
              }
            })
        })
    })
  }

  private def library(entry: YMapEntry) = {
    references += Reference(entry.value, Library, entry)
  }

  def oasLinks(part: YPart): Unit = {
    part match {
      case map: YMap if map.entries.size == 1 && isRef(map.entries.head) => oasInclude(map)
      case _                                                             => part.children.foreach(oasLinks)
    }
  }

  private def oasInclude(map: YMap): Unit = {
    val ref = map.entries.head
    ref.value.value match {
      case scalar: YScalar => references += Reference(scalar.text, Link, map)
      case _               => throw new Exception(s"Unexpected $$ref with $ref")
    }
  }

  private def isRef(entry: YMapEntry) = {
    entry.key.value match {
      case scalar: YScalar => scalar.text == "$ref"
      case _               => false
    }
  }

  def ramlLinks(part: YPart): Unit = {
    part match {
      case node: YNode if !node.tag.synthesized && node.tag.text == "!include" => ramlInclude(node)
      case _                                                                   => part.children.foreach(ramlLinks)
    }
  }

  private def ramlInclude(node: YNode) = {
    node.value match {
      case scalar: YScalar => references += Reference(scalar.text, Link, node)
      case _               => throw new Exception(s"Unexpected !include with ${node.value}")
    }
  }
}

case class Reference(url: String, kind: Kind, ast: YAggregate) {

  def isRemote: Boolean = !url.startsWith("#")

  def resolve(remote: Platform,
              context: Context,
              cache: Cache,
              hint: Hint,
              currentValidation: Validation,
              dialectRegistry: DialectRegistry = DialectRegistry.default): Future[BaseUnit] = {
    AMFCompiler(url, remote, hint + kind, currentValidation, Some(context), Some(cache), dialectRegistry)
      .build()
      .map(root => {
//        target = root
        root
      })
  }
}
