package amf.domain
import amf.builder.OperationBuilder
import amf.metadata.domain.OperationModel.{Request => OperationRequest, _}

/**
  * Operation internal model.
  */
case class Operation(fields: Fields, annotations: List[Annotation]) extends DomainElement {

  override type T = Operation

  val method: String              = fields(Method)
  val name: String                = fields(Name)
  val description: String         = fields(Description)
  val deprecated: Boolean         = fields(Deprecated)
  val summary: String             = fields(Summary)
  val documentation: CreativeWork = fields(Documentation)
  val schemes: Seq[String]        = fields(Schemes)
  val request: Request            = fields(OperationRequest)
  val responses: Seq[Response]    = fields(Responses)

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
        schemes == that.schemes &&
        request == that.request &&
        responses == that.responses
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(method, name, description, deprecated, summary, documentation, schemes, request, responses)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString =
    s"Operation($method, $name, $description, $deprecated, $summary, $documentation, $schemes, $request, $responses)"

  override def toBuilder: OperationBuilder = OperationBuilder(fields, annotations)
}
