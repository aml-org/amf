package amf.plugins.document.vocabularies.references

import amf.core.parser.{LibraryReference, LinkReference, Reference, ReferenceHandler, _}
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import org.yaml.model._

import scala.collection.mutable.ArrayBuffer

class RAMLExtensionsReferenceHandler extends ReferenceHandler {
  private val references = new ArrayBuffer[Reference]

  override def collect(parsed: ParsedDocument, ctx: ParserContext): Seq[Reference] = {
    if (parsed.comment.isDefined){
        if (referencesDialect(parsed.comment.get.metaText)){
          references+=Reference(RAMLVocabulariesPlugin.dialectDefinitionUrl(parsed.comment.get.metaText),SchemaReference,parsed.document.node)
        }
    }
    libraries(parsed.document, ctx)
    links(parsed.document)
    references
  }

  private def libraries(document: YDocument, ctx: ParserContext): Unit = {
    document.to[YMap] match {
      case Right(map) =>
        map
          .key("uses")
          .foreach(entry => {
            entry.value.to[YMap] match {
              case Right(m) => m.entries.foreach(library)
              case _        => ctx.violation("", s"Expected map but found: ${entry.value}", entry.value)
            }
          })
      case _ =>
    }
  }

  private def referencesDialect(mt:String):Boolean={
    val io=mt.indexOf("|");
    if (io>0){
      var msk=mt.substring(io+1);
      val si=msk.indexOf("<");
      val se=msk.lastIndexOf(">");
      return si>0&&se>si;
    }
    false
  }



  private def library(entry: YMapEntry) = {
    references += Reference(entry.value, LibraryReference, entry.value)
  }

  private def links(part: YPart): Unit = {
    part match {
      case entry: YMapEntry =>{
        if (entry.key.tag.text=="!extend"){
            ramlInclude(entry.value);
        }
        else{
          part.children.foreach(links)
        }
      }
      case node: YNode if (node.tagType == YType.Include)  => ramlInclude(node)
      case _                                               => part.children.foreach(links)
    }
  }

  private def ramlInclude(node: YNode) = {
    node.value match {

      case scalar: YScalar => references += Reference(scalar.text, LinkReference, node)
      case _               => throw new Exception(s"Unexpected !include with ${node.value}")
    }
  }
}
