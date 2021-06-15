package amf.plugins.domain.shapes.models

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.SchemaDependenciesModel
import amf.plugins.domain.shapes.metamodel.SchemaDependenciesModel.SchemaTarget
import org.yaml.model.YMapEntry

/**
  * Schema Dependency
  */
case class SchemaDependencies(fields: Fields, annotations: Annotations) extends Dependencies {

  def schemaTarget: Shape                       = fields.field(SchemaTarget)
  def withSchemaTarget(shape: Shape): this.type = set(SchemaTarget, shape)

  override def meta: SchemaDependenciesModel.type = SchemaDependenciesModel

  /** Call after object has been adopted by specified parent. */
  override def adopted(parent: String, cycle: Seq[String]): SchemaDependencies.this.type = {
    simpleAdoption(parent)
    Option(schemaTarget).foreach(_.adopted(id, cycle :+ id))
    this
  }

  override def componentId: String = {
    val propertySourceName = propertySource.option().map(x => x).getOrElse("unknown").split("/").last
    s"/dependencySchema/$propertySourceName"
  }
}

object SchemaDependencies {
  def apply(): SchemaDependencies = apply(Annotations())

  def apply(ast: YMapEntry): SchemaDependencies = apply(Annotations(ast))

  def apply(annotations: Annotations): SchemaDependencies = SchemaDependencies(Fields(), annotations)
}
