package amf.shape

import amf.domain.{CreativeWork, DomainElement}
import amf.metadata.shape.ShapeModel._

/**
  * Shape.
  */
abstract class Shape extends DomainElement {

  def name: String                    = fields(Name)
  def displayName: String             = fields(DisplayName)
  def description: String             = fields(Description)
  def default: String                 = fields(Default)
  def values: Seq[String]             = fields(Values)
  def documentation: CreativeWork     = fields(Documentation)
  def xmlSerialization: XMLSerializer = fields(XMLSerialization)

  def withName(name: String): this.type                                = set(Name, name)
  def withDisplayName(name: String): this.type                         = set(DisplayName, name)
  def withDescription(description: String): this.type                  = set(Description, description)
  def withDefault(default: String): this.type                          = set(Default, default)
  def withValues(values: Seq[String]): this.type                       = set(Values, values)
  def withDocumentation(documentation: CreativeWork): this.type        = set(Documentation, documentation)
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = set(XMLSerialization, xmlSerialization)
}
