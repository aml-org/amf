package amf.plugins.document.vocabularies2.parser.instances

import amf.core.Root
import amf.core.parser.{Annotations, BaseSpecParser, Declarations, EmptyFutureDeclarations, ErrorHandler, FutureDeclarations, ParserContext}
import amf.plugins.document.vocabularies2.model.document.{Dialect, DialectInstance}
import amf.plugins.document.vocabularies2.model.domain.{DialectDomainElement, DocumentMapping, DocumentsModel, NodeMapping}
import org.yaml.model.YMap

class DialectInstanceDeclarations(errorHandler: Option[ErrorHandler],
                                  futureDeclarations: FutureDeclarations)
  extends Declarations(Map(), Map(), Map(), errorHandler, futureDeclarations){

  /** Get or create specified library. */
  override def getOrCreateLibrary(alias: String): DialectInstanceDeclarations = {
    libraries.get(alias) match {
      case Some(lib: DialectInstanceDeclarations) => lib
      case _ =>
        val result = new DialectInstanceDeclarations(errorHandler = errorHandler, futureDeclarations = EmptyFutureDeclarations())
        libraries = libraries + (alias -> result)
        result
    }
  }

  /*
  def +=(nodeMapping: NodeMapping): DialectDeclarations = {
    nodeMappings += (nodeMapping.name -> nodeMapping)
    this
  }


  def findNodeMapping(key: String, scope: SearchScope.Scope): Option[NodeMapping] =
    findForType(key, _.asInstanceOf[DialectDeclarations].nodeMappings, scope) collect {
      case nm: NodeMapping => nm
    }
  */

}


class DialectInstanceContext(private val wrapped: ParserContext, private val ds: Option[DialectInstanceDeclarations] = None)
  extends ParserContext(wrapped.rootContextDocument, wrapped.refs, wrapped.futureDeclarations) {


  val declarations: DialectInstanceDeclarations =
    ds.getOrElse(new DialectInstanceDeclarations(errorHandler = Some(this), futureDeclarations = futureDeclarations))

}


class RamlDialectInstanceParser(root: Root, dialect: Dialect)(implicit override val ctx: DialectInstanceContext) extends BaseSpecParser {
  val map: YMap = root.parsed.document.as[YMap]
  val dialectInstance: DialectInstance = DialectInstance(Annotations(map)).withLocation(root.location).withId(root.location + "#").withDefinedBy(dialect.id)

  def parseDocument(): Option[DialectInstance] = {
    parseEncoded() match {
      case Some(dialectDomainElement) => Some(dialectInstance.withEncodes(dialectDomainElement))
      case _                          => None
    }
  }

  protected def parseEncoded(): Option[DialectDomainElement] = {
    Option(dialect.documents()) flatMap {
      documents: DocumentsModel =>
        Option(documents.root()) flatMap {
          mapping =>
            findNodeMapping(mapping.encoded()) match {
              case Some(nodeMapping) => parseNode(dialectInstance.id + "/", map, nodeMapping)
              case _ => None
            }
        }
    }
  }

  protected def parseNode(id: String, map: YMap, mapping: NodeMapping): Option[DialectDomainElement] = {
    val node: DialectDomainElement = DialectDomainElement(map).withId(id).withDefinedBy(mapping)


    mapping.propertiesMapping().foreach { propertyMapping => } // TODO: HERE!

    Some(node)
  }

  protected def findNodeMapping(mappingId: String): Option[NodeMapping] = {
    dialect.declares.collectFirst {
      case mapping: NodeMapping if mapping.id == mappingId => mapping
    }
  }
}
