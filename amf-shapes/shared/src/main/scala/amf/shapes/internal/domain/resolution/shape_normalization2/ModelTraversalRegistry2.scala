package amf.shapes.internal.domain.resolution.shape_normalization2

import scala.collection.mutable

class ModelTraversalRegistry2() {

  // All IDs visited in the traversal
  private[amf] var visitedIds: mutable.Set[String] = mutable.Set()

  // IDs visited in the current path
  private[amf] var currentPath: Set[String] = Set.empty

  def +(id: String): this.type = {
    visitedIds += id
    currentPath += id
    this
  }

  def isInCurrentPath(id: String): Boolean = currentPath.contains(id)

  def wasVisited(id: String): Boolean = visitedIds.contains(id)

  // Runs a function and restores the currentPath to its original state after the run
  def runNested[T](fnc: this.type => T): T = {
    val previousPath = currentPath
    val element      = fnc(this)
    currentPath = previousPath
    element
  }

}
