package amf.grpc.internal.spec.emitter.context

import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}

class GrpcEmitterContext(document: BaseUnit) {
  def topLevelMessages: Seq[NodeShape] = messages.filter { s =>
    val declarations = s.name.value().split("\\.").filter(w => w != "" && !w.matches("[a-z].+"))
    declarations.length == 1
  }

  def extensions: Seq[CustomDomainProperty] = document match {
    case dec: DeclaresModel => dec.declares.collect { case cdp: CustomDomainProperty => cdp }
    case _                  => Nil
  }

  def topLevelEnums: Seq[ScalarShape] = enums.filter { s =>
    val declarations = s.name.value().split("\\.").filter(w => w != "" && !w.matches("[a-z].+"))
    declarations.length == 1
  }

  def nestedMessages(shape: NodeShape): Seq[NodeShape] = {
    val currentPath  = shape.name.value()
    val currentLevel = currentPath.split("\\.").length
    messages.filter { s =>
      val level = s.name.value().split("\\.").length
      s.name.value().startsWith(currentPath) && (level == currentLevel + 1)
    }
  }

  def nestedEnums(shape: NodeShape): Seq[ScalarShape] = {
    val currentPath  = shape.name.value()
    val currentLevel = currentPath.split("\\.").length
    enums.filter { s =>
      val level = s.name.value().split("\\.").length
      s.name.value().startsWith(currentPath) && (level == currentLevel + 1)
    }
  }

  def messages: Seq[NodeShape] = {
    document match {
      case dec: DeclaresModel => dec.declares.collect { case n: NodeShape => n }
      case _                  => Nil
    }
  }

  def enums: Seq[ScalarShape] = {
    document match {
      case dec: DeclaresModel => dec.declares.collect { case s: ScalarShape if s.values.nonEmpty => s }
      case _                  => Nil
    }
  }

}
