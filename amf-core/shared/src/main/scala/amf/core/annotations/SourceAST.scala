package amf.core.annotations

import amf.core.model.domain.Annotation
import org.yaml.model.{YNode, YPart}

case class SourceAST(ast: YPart) extends Annotation

case class SourceNode(node: YNode) extends Annotation
