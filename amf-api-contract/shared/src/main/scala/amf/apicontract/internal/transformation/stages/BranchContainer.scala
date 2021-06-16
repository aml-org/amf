package amf.apicontract.internal.transformation.stages

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class BranchContainer(branches: Seq[Branch]) {
  def flatten(): Seq[Branch] = {
    val visited: mutable.Set[Key]      = mutable.Set()
    val queue: ListBuffer[Seq[Branch]] = ListBuffer()
    queue += branches
    visited ++= branches.map(_.key)
    collect(visited, queue, branches)
    queue.foldLeft(Seq[Branch]())(_ ++ _)
  }

  private def collect(visited: mutable.Set[Key], queue: ListBuffer[Seq[Branch]], previous: Seq[Branch]): Unit = {
    val branches: ListBuffer[Branch] = ListBuffer()
    previous.foreach(prev => {
      prev.children.filter(c => visited.add(c.key)).foreach(branches += _)
    })
    queue += branches
    if (branches.nonEmpty) collect(visited, queue, branches)
  }
}

object BranchContainer {
  def merge(left: Seq[Branch], right: Seq[Branch]): Seq[Branch] = left ++ right.filterNot(left.contains(_))
}
