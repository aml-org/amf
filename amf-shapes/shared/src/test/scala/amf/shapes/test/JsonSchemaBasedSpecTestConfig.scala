package amf.shapes.test

import amf.core.internal.remote.Spec
import amf.shapes.client.scala.JsonSchemaBasedSpecConfiguration
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchemaLoader

case class CycleInstance(source: String, golden: String)
case class InvalidInstance(path: String, expectedErrors: Option[Int], expectedMessages: Seq[String] = Nil)

case class JsonSchemaBasedSpecTestConfig(
    specName: String,
    basePath: String,
    configuration: JsonSchemaBasedSpecConfiguration,
    schemaLoader: JsonSchemaBasedSpecSchemaLoader,
    spec: Spec,
    validInstances: Seq[String],
    invalidInstances: Seq[InvalidInstance],
    cycleInstances: Seq[CycleInstance],
    schemaShapeCheck: amf.core.client.scala.model.domain.Shape => Boolean = _.isInstanceOf[amf.shapes.client.scala.model.domain.NodeShape]
)
