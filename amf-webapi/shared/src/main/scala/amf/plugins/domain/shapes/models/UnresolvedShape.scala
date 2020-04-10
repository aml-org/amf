package amf.plugins.domain.shapes.models

import amf.core.metamodel.Field
import amf.core.metamodel.domain.ModelDoc
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.{Annotations, Fields, UnresolvedReference}
import amf.plugins.document.webapi.parser.spec.common.ShapeExtensionParser
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import org.yaml.model.{YNode, YPart}

import scala.collection.mutable

/**
  * Unresolved shape: intended to be resolved after parsing (exception is thrown if shape is not resolved).
  */
case class UnresolvedShape(override val fields: Fields,
                           override val annotations: Annotations,
                           override val reference: String,
                           fatherExtensionParser: Option[Option[String] => ShapeExtensionParser] = None,
                           updateFatherLink: Option[String => Unit] = None,
                           override val shouldLink: Boolean = true)
    extends AnyShape(fields, annotations)
    with UnresolvedReference {

  override def linkCopy(): AnyShape = this

  /*
  override def withId(newId: String): this.type = {
    if (id == null) super.withId(newId)
    this
  }
   */
  override def link[T](label: String, annotations: Annotations): T = this.asInstanceOf[T]

  /** Resolve [[UnresolvedShape]] as link to specified target. */
  def resolve(target: Shape): Shape = target.link(reference, annotations).asInstanceOf[Shape].withName(name.value())

  override val meta: AnyShapeModel = new AnyShapeModel {
    override def fields: List[Field] = AnyShapeModel.fields
    override val doc: ModelDoc       = AnyShapeModel.doc

    override def modelInstance: UnresolvedShape = UnresolvedShape(Fields(), Annotations(), reference = reference)
  }

  override def ramlSyntaxKey: String = "unresolvedShape"

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/unresolved"

  override def afterResolve(fatherSyntaxKey: Option[String], resolvedKey: String): Unit = {
    fatherExtensionParser.foreach { parser =>
      parser(fatherSyntaxKey).parse()
    }
    updateFatherLink.foreach(f => f(resolvedKey))
  }

  // if is unresolved the effective target its himselft, because any real type has been found.
  override def effectiveLinkTarget(links: Seq[String] = Seq()): UnresolvedShape = this

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    (fields: Fields, annotations: Annotations) =>
      new UnresolvedShape(fields, annotations, reference, fatherExtensionParser)
}

object UnresolvedShape {
  def apply(reference: String): UnresolvedShape = apply(reference, Annotations(), None)

  def apply(reference: String,
            ast: YPart,
            extensionParser: Option[Option[String] => ShapeExtensionParser]): UnresolvedShape =
    apply(reference, Annotations(ast), extensionParser)

  def apply(reference: String, ast: YPart): UnresolvedShape = apply(reference, Annotations(ast), None)

  def apply(reference: String, ast: Option[YPart]): UnresolvedShape =
    apply(reference, Annotations(ast.getOrElse(YNode.Null)), None)

  def apply(reference: String,
            annotations: Annotations,
            extensionParser: Option[Option[String] => ShapeExtensionParser]): UnresolvedShape =
    UnresolvedShape(Fields(), annotations, reference, extensionParser)

}
