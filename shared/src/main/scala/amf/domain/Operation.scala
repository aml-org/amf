package amf.domain
import amf.builder.{Builder, OperationBuilder}
import amf.metadata.domain.OperationModel._

/**
  * Operation internal model.
  */
case class Operation(override val fields: Fields) extends FieldHolder(fields) with DomainElement {
  override type This = Operation

  val method: String              = fields get Method
  val name: String                = fields get Name
  val description: String         = fields get Description
  val deprecated: Boolean         = fields get Deprecated
  val summary: String             = fields get Summary
  val documentation: CreativeWork = fields get Documentation
  val schemes: CreativeWork       = fields get Schemes

  def canEqual(other: Any): Boolean = other.isInstanceOf[Operation]

  override def equals(other: Any): Boolean = other match {
    case that: Operation =>
      (that canEqual this) &&
        method == that.method &&
        name == that.name &&
        description == that.description &&
        deprecated == that.deprecated &&
        summary == that.summary &&
        documentation == that.documentation &&
        schemes == that.schemes
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(method, name, description, deprecated, summary, documentation, schemes)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Operation($method, $name, $description, $deprecated, $summary, $documentation, $schemes)"

  override def toBuilder: Builder[Operation] = OperationBuilder(fields)
}
