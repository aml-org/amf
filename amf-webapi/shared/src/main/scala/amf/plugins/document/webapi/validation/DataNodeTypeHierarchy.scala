package amf.plugins.document.webapi.validation

import amf.core.model.DataType

trait DataNodeTypeHierarchy {

  def getTimeHierarchy: Set[String] = Set(
    DataType.String.trim,
    DataType.Time.trim
  )

  def getDateOnlyHierarchy: Set[String] = Set(
    DataType.String.trim,
    DataType.Date.trim
  )

  def getDateTimeOnlyHierarchy: Set[String] = Set(
    DataType.String.trim,
    DataType.DateTimeOnly.trim
  )

  def getDateTimeHierarchy: Set[String] = Set(
    DataType.String.trim,
    DataType.DateTime.trim
  )

  def getBooleanHierarchy: Set[String] = Set(
    DataType.Boolean.trim
  )

  def getIntegerHierarchy: Set[String] = Set(
    DataType.Integer.trim
  )

  def getNumberHierarchy: Set[String] =
    Set(
      DataType.Double.trim,
      DataType.Float.trim,
      DataType.Long.trim
    ) ++ getIntegerHierarchy

  def getDoubleHierarchy: Set[String] = getNumberHierarchy

  def getFloatHierarchy: Set[String] = getNumberHierarchy

  def getStringHierarchy: Set[String]
}

object DataNodeTypeHierarchyLogicalConstraint extends DataNodeTypeHierarchy {

  override def getStringHierarchy: Set[String] =
    Set(
      DataType.String.trim
    ) ++ getTimeHierarchy ++ getDateOnlyHierarchy ++ getDateTimeOnlyHierarchy ++ getDateTimeHierarchy

}

object DataNodeTypeHierarchyStandard extends DataNodeTypeHierarchy {

  override def getStringHierarchy: Set[String] =
    Set(
      DataType.String.trim
    ) ++ getTimeHierarchy ++ getDateOnlyHierarchy ++ getDateTimeOnlyHierarchy ++ getDateTimeHierarchy ++
      getBooleanHierarchy ++ getNumberHierarchy

}
