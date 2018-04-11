package amf.plugins.domain.shapes.models

import amf.core.metamodel.Obj
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Fields}
import amf.core.services.PayloadValidator
import amf.core.validation.{AMFValidationReport, SeverityLevels}
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaSerializer
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.metamodel.AnyShapeModel._
import org.yaml.model.YPart

import scala.concurrent.Future

class AnyShape(val fields: Fields, val annotations: Annotations)
    extends Shape
    with ShapeHelpers
    with JsonSchemaSerializer {

  def documentation: CreativeWork     = fields.field(Documentation)
  def xmlSerialization: XMLSerializer = fields.field(XMLSerialization)
  def examples: Seq[Example]          = fields.field(Examples)

  def withDocumentation(documentation: CreativeWork): this.type        = set(Documentation, documentation)
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = set(XMLSerialization, xmlSerialization)
  def withExamples(examples: Seq[Example]): this.type                  = setArray(Examples, examples)

  def withExample(name: Option[String]): Example = {
    val example = Example()
    name.foreach { example.withName }
    add(Examples, example)
    example
  }

  override def linkCopy(): AnyShape = AnyShape().withId(id)

  override def meta: Obj = AnyShapeModel

  def toJsonSchema: String = toJsonSchema(this)

  def copyAnyShape(fields: Fields = fields, annotations: Annotations = annotations): AnyShape =
    AnyShape(fields, annotations).withId(id)

  def validate(payload: String): Future[AMFValidationReport] =
    PayloadValidator.validate(this, payload, SeverityLevels.VIOLATION)

  def validate(fragment: PayloadFragment): Future[AMFValidationReport] =
    PayloadValidator.validate(this, fragment, SeverityLevels.VIOLATION)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/any/" + name.option().getOrElse("default-any")
}

object AnyShape {
  def apply(): AnyShape = apply(Annotations())

  def apply(ast: YPart): AnyShape = apply(Annotations(ast))

  def apply(annotations: Annotations): AnyShape = AnyShape(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): AnyShape = new AnyShape(fields, annotations)
}
