package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.Operation

class CleanRepeatedOperationIds()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T): T =
    try {
      val operations           = getOperationsFromModel(model)
      val repeatedOperationIds = operationsWithRepeatedIds(operations)
      repeatedOperationIds.foreach(addDistinctOperationIds)
      model
    } catch {
      case _: Throwable => model
    }

  private def addDistinctOperationIds(operations: Seq[Operation]): Unit =
    operations.zipWithIndex.foreach {
      case (operation, index) => operation.withName(operation.name + s"_${index}")
    }

  private def operationsWithRepeatedIds(operations: Seq[Operation]): Seq[Seq[Operation]] =
    operations
      .filter(x => x.name.option().isDefined)
      .groupBy(_.name.value())
      .filter(isRepeated)
      .values
      .toSeq

  private def isRepeated(tuple: (String, Seq[Operation])): Boolean = tuple._2.size > 1

  private def getOperationsFromModel[T <: BaseUnit](model: T): Seq[Operation] =
    model
      .iterator()
      .collect {
        case operation: Operation => operation
      }
      .toSeq
}
