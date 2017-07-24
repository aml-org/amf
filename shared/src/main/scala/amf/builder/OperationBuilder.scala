package amf.builder

import amf.domain.{CreativeWork, Fields, Operation}
import amf.metadata.domain.OperationModel._

/**
  * Operation builder.
  */
class OperationBuilder extends Builder {

  override type T = Operation

  def withMethod(method: String): this.type = set(Method, method)

  def withName(name: String): this.type = set(Name, name)

  def withDescription(description: String): this.type = set(Description, description)

  def isDeprecated(deprecated: Boolean): this.type = set(Deprecated, deprecated)

  def withSummary(summary: String): this.type = set(Summary, summary)

  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)

  def withSchemes(schemes: Seq[String]): this.type = set(Schemes, schemes)

  override def build: Operation = Operation(fields)
}

object OperationBuilder {
  def apply(): OperationBuilder = new OperationBuilder()

  def apply(fields: Fields): OperationBuilder = apply().copy(fields)
}
