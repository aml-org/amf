package amf.parser

import amf.lexer.Token

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * AST emitter
  */
case class ASTEmitter[T <: Token, N <: ASTNode[T]](factory: ASTFactory[T, N]) {

  private val stack: mutable.ArrayStack[NodeBuilder] = mutable.ArrayStack()

  /** Build and return root node. */
  def root(root: T)(parse: () => Unit): N = {
    beginNode()
    parse()
    createNode(root)
  }

  /** Starts the building of a new tree node. Every call to addChild will place a new child under the current sub-tree. */
  def beginNode(): this.type = {
    stack.push(NodeBuilder(Range.NONE))
    this
  }

  /**
    * Instruct the emitter to end the building of the current sub-tree. The provided token will be
    * used as the root of the constructed sub-tree
    */
  def endNode(token: T): this.type = {
    addChild(createNode(token))
  }

  protected def createNode(token: T): N = {
    val n = stack.pop
    factory.createNode(token, n.range, n.nodes)
  }

  def value(t: T, content: String): this.type = addChild(factory.createNode(t, content, Range.NONE))

  /** Adds given input token to the sub-tree under construction. */
  def addChild(n: N): this.type = {
    stack.head + n
    this
  }

  private case class NodeBuilder(range: Range, nodes: ListBuffer[N] = ListBuffer()) {
    def +(n: N): Unit = nodes += n
  }
}
