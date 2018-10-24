package amf.core.model.domain

import amf.core.metamodel.domain.LinkableElementModel
import amf.core.model.{BoolField, StrField}
import amf.core.parser.{Annotations, DeclarationPromise, Fields, ParserContext}
import org.yaml.model.YPart
import amf.core.utils._

trait Linkable extends AmfObject { this: DomainElement with Linkable =>

  def linkTarget: Option[DomainElement]    = Option(fields(LinkableElementModel.Target))
  var linkAnnotations: Option[Annotations] = None
  def supportsRecursion: BoolField         = fields.field(LinkableElementModel.SupportsRecursion)
  def effectiveLinkTarget: DomainElement = {
    linkTarget
      .map {
        case linkable: Linkable if linkTarget.isDefined => linkable.effectiveLinkTarget
        case other                                      => other
      }
      .getOrElse(this)
  }

  def isLink: Boolean     = linkTarget.isDefined
  def linkLabel: StrField = fields.field(LinkableElementModel.Label)

  def linkCopy(): Linkable

  def withLinkTarget(target: DomainElement): this.type = {
    fields.setWithoutId(LinkableElementModel.Target, target)
    set(LinkableElementModel.TargetId, target.id)
  }
  def withLinkLabel(label: String): this.type              = set(LinkableElementModel.Label, label)
  def withSupportsRecursion(recursive: Boolean): this.type = set(LinkableElementModel.SupportsRecursion, recursive)

  def link[T](label: String, annotations: Annotations = Annotations()): T = {
    val copied = linkCopy()
    copied
      .withId(linkCounter.genId(copied.id + "/linked"))
      .withLinkTarget(this)
      .withLinkLabel(label)
      .add(annotations)
      .asInstanceOf[T]
  }

  /**
    * This can be overriden by subclasses to customise how the links to unresolved classes are generated.
    * By default it just generates a link.
    */
  def resolveUnreferencedLink[T](label: String,
                                 annotations: Annotations = Annotations(),
                                 unresolved: T,
                                 supportsRecursion: Boolean): T = {
    if (unresolved.asInstanceOf[Linkable].shouldLink) {

      val linked: T = link(label, annotations)
      if (supportsRecursion && linked.isInstanceOf[Linkable])
        linked.asInstanceOf[Linkable].withSupportsRecursion(supportsRecursion)
      linked
    } else
      this.asInstanceOf[T]
  }

  protected val shouldLink: Boolean = true

  def afterResolve(fatherSyntaxKey: Option[String], resolvedId: String): Unit = Unit

  // Unresolved references to things that can be linked
  // TODO: another trait?
  var isUnresolved: Boolean         = false
  var unresolvedSeverity: String    = "error"
  var refName                       = ""
  var refAst: Option[YPart]         = None
  var refCtx: Option[ParserContext] = None

  def unresolved(refName: String, refAst: YPart, unresolvedSeverity: String = "error")(implicit ctx: ParserContext) = {
    isUnresolved = true
    this.refName = refName
    this.refAst = Some(refAst)
    refCtx = Some(ctx)
    this
  }

  def toFutureRef(resolve: Linkable => Unit) = {
    refCtx match {
      case Some(ctx) =>
        ctx.futureDeclarations.futureRef(
          id,
          refName,
          DeclarationPromise(
            resolve,
            () =>
              if (unresolvedSeverity == "warning") {
                ctx.warning(id,
                            s"Unresolved reference '$refName' from root context ${ctx.rootContextDocument}",
                            refAst.get)
              } else {
                ctx.violation(id,
                              s"Unresolved reference '$refName' from root context ${ctx.rootContextDocument}",
                              refAst.get)
            }
          )
        )
      case none => throw new Exception("Cannot create unresolved reference with missing parsing context")
    }
  }

  private val linkCounter = new IdCounter()

  /** generates a new instance of the domain element only clonning his own fields map, and not clonning all the tree (not recursive)*/
  /** Do not generates a new link.*/
  def copyElement(): Linkable with DomainElement = classConstructor(fields.copy(), annotations.copy())

  /** apply method for create a new instance with fields and annotations. Aux method for copy*/
  protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement
}
