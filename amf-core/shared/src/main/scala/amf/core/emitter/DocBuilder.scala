package amf.core.emitter
import amf.core.parser.SyamlParsedDocument
import amf.core.rdf.{RdfModel, RdfModelDocument}
import org.yaml.model.{YComment, YDocument}

abstract class DocBuilder[T](val renderOptions: RenderOptions) {
  def result: T
  def isDefined: Boolean
}

abstract class ParsedDocBuilder[T, D](ro: RenderOptions) extends DocBuilder[T](ro) {
  private var _document: Option[D] = None
  def document: D                  = _document.get
  def document_=(doc: D): Unit     = _document = Some(doc)
  override def isDefined: Boolean  = _document.isDefined
}

class SyamlBuilder(ro: RenderOptions = new RenderOptions) extends ParsedDocBuilder[SyamlParsedDocument, YDocument](ro) {
  var comment: Option[YComment]            = None
  override def result: SyamlParsedDocument = SyamlParsedDocument(document, comment)
}

class RdfModelBuilder(ro: RenderOptions = new RenderOptions) extends ParsedDocBuilder[RdfModelDocument, RdfModel](ro) {
  override def result: RdfModelDocument = RdfModelDocument(document)
}
