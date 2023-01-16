package amf.shapes.internal.domain.resolution.shape_normalization
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.validation.CoreValidations.RecursiveShapeSpecification
import amf.shapes.client.scala.model.domain.UnionShape
import scala.collection.mutable

class RecursionAnalyzer(errorHandler: AMFErrorHandler) {

  val visitedIds          = mutable.ArrayBuffer[String]()
  val allowedRecursionIds = mutable.ArrayBuffer[String]()

  // These are exclusive used to handle recursions in unions
  val unionsBeingAnalyzed       = mutable.ArrayBuffer[String]()
  val traversedUnionMembers     = mutable.ArrayBuffer[String]()
  var pendingAnalysisRecursions = mutable.ArrayBuffer[UnionRecursionRecord]()

  def executeWhileDetectingRecursion[T](shape: Shape, f: Shape => T): T = {
    registerVisit(shape)
    val result = f(shape)
    unregisterLastVisit()
    result
  }

  def executeAllowingRecursionIn[T](shape: Shape, f: Shape => T, condition: Boolean = true): T = {
    if (condition) allow(shape)
    val result = f(shape)
    if (condition) disallowLast()
    result
  }

  def executeAndAnalyzeRecursionInUnion[T](union: UnionShape, f: UnionShape => T): T = {
    unionsBeingAnalyzed.append(union.id)
    allow(union)
    val result = f(union)
    analyzePossibleRecursions(union)
    disallowLast()
    unionsBeingAnalyzed.remove(unionsBeingAnalyzed.size - 1)
    result
  }

  def isInvalidRecursion(shape: Shape): Boolean = recursionCycle(shape).intersect(allowedRecursionIds).isEmpty

  def recursionDetected(shape: Shape): Boolean = hasBeenVisited(shape) && !isAProperty(shape)

  private def recursionCycle(shape: Shape) = visitedIds.slice(visitedIds.indexOf(shape.id), visitedIds.size)

  private def hasBeenVisited(lastVersion: Shape) = visitedIds.contains(lastVersion.id)

  private def isAProperty(lastVersion: Shape) = lastVersion.isInstanceOf[PropertyShape]

  def analyzePossibleRecursions(union: UnionShape): Unit = {
    val numberOfConfirmedRecursiveMembers = countConfirmedRecursiveMembers(union)
    val numberOfPossibleRecursiveMembers  = countPossibleRecursiveMembers(union)
    val numberOfMembers                   = union.anyOf.size
    val allMembersAreRecursive            = numberOfConfirmedRecursiveMembers == numberOfMembers
    val existsNotRecursiveMember          = numberOfPossibleRecursiveMembers < numberOfMembers

    if (existsNotRecursiveMember) {
      discardPossibleRecursions()
    } else {
      if (allMembersAreRecursive) reportRecursionsRelatedTo(union) else updateInfo(union)
    }
  }

  private def countPossibleRecursiveMembers(union: UnionShape) = {
    pendingAnalysisRecursions
      .filter(rec => onlyDependsOnCurrentUnion(rec, union) || mayBeDeclaredValidInCurrentUnion(rec, union))
      .map(_.relativeToUnionMember)
      .toSet
      .size
  }

  private def countConfirmedRecursiveMembers(union: UnionShape) = {
    pendingAnalysisRecursions
      .filter(rec => onlyDependsOnCurrentUnion(rec, union))
      .map(_.relativeToUnionMember)
      .toSet
      .size
  }

  private def reportRecursionsRelatedTo(union: UnionShape): Unit = {
    // These recursion cannot be avoided so are invalid and should de reported
    pendingAnalysisRecursions
      .filter(_.relativeToUnion == union.id)
      .foreach(recursion => reportInvalidRecursion(recursion.generator))

    // Remove reported recursions
    pendingAnalysisRecursions = pendingAnalysisRecursions.filter(recursion => recursion.relativeToUnion != union.id)
  }

  def detectRecursionsFromUnions(shape: Shape): Unit = {
    val cycle = recursionCycle(shape)

    // The idea is to find the union that was traversed first of all involved in the recursion
    val lastExit = unionsBeingAnalyzed.find(w => cycle.contains(w))

    lastExit.foreach(exit => {
      val indexOfGenerator     = unionsBeingAnalyzed.lastIndexWhere(w => cycle.contains(w))
      val relatedToUnion       = unionsBeingAnalyzed(indexOfGenerator)
      val relatedToUnionMember = traversedUnionMembers.last
      pendingAnalysisRecursions.append(UnionRecursionRecord(exit, relatedToUnion, relatedToUnionMember, shape))
    })
  }

  private def updateInfo(union: UnionShape): Unit = {
    // This recursions are not going to be reported now.
    // It's necessary to update member to check in father if an exit exists.
    pendingAnalysisRecursions.foreach(recursion =>
      if (mayBeDeclaredValidInCurrentUnion(recursion, union)) {
        recursion.relativeToUnion = unionsBeingAnalyzed(unionsBeingAnalyzed.size - 2)
        recursion.relativeToUnionMember = traversedUnionMembers(traversedUnionMembers.size - 1)
      }
    )
  }

  private def discardPossibleRecursions(): Unit = {
    // There is an exit (a member of this union with no recursion)
    // All recursions found are now valid
    pendingAnalysisRecursions.clear()
  }

  private def mayBeDeclaredValidInCurrentUnion(recursion: UnionRecursionRecord, current: UnionShape) =
    recursion.relativeToUnion == current.id
  private def onlyDependsOnCurrentUnion(recursion: UnionRecursionRecord, current: UnionShape) =
    recursion.lastExit == current.id

  // Should we provide info about the cycle?
  private def reportInvalidRecursion(lastVersion: Shape): Unit = {
    errorHandler.violation(
      RecursiveShapeSpecification,
      lastVersion.id,
      None,
      "Error recursive shape",
      lastVersion.position(),
      lastVersion.location()
    )
  }

  private def disallowLast()                    = allowedRecursionIds.remove(allowedRecursionIds.size - 1)
  private def allow(shape: Shape): Unit         = allowedRecursionIds.append(shape.id)
  private def registerVisit(shape: Shape): Unit = visitedIds.append(shape.id)
  private def unregisterLastVisit(): Unit       = visitedIds.remove(visitedIds.size - 1)
}
