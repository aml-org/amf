package amf.shape

import amf.domain.CreativeWork
import amf.metadata.shape.ShapeModel._
import amf.model.AmfObject

/**
  * Shape
  */
abstract class Shape extends AmfObject {

  def name: String                = fields(Name)
  def displayName: String         = fields(DisplayName)
  def description: String         = fields(Description)
  def default: String             = fields(Default)
  def values: Seq[String]         = fields(Values)
  def documentation: CreativeWork = fields(Documentation)

  def withName(name: String): this.type                         = set(Name, name)
  def withDisplayName(name: String): this.type                  = set(DisplayName, name)
  def withDescription(description: String): this.type           = set(Description, description)
  def withDefault(default: String): this.type                   = set(Default, default)
  def withValues(values: Seq[String]): this.type                = set(Values, values)
  def withDocumnetation(documentation: CreativeWork): this.type = set(Documentation, documentation)
}
