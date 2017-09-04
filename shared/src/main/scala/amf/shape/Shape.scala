package amf.shape

import amf.domain.{CreativeWork, Fields}
import amf.metadata.shape.ShapeModel._
import amf.model.AmfObject

/**
  * Shape
  */
abstract class Shape extends AmfObject {

  def name: String                = fields(Name)
  def description: String         = fields(Description)
  def default: String             = fields(Default)
  def in: Seq[String]             = fields(In)
  def documentation: CreativeWork = fields(Documentation)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
}
