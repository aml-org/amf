package amf.plugins.document.webapi.validation

import amf.core.vocabulary.Namespace

trait DataNodeTypeHierarchy {

  def getTimeHierarchy: Set[String] = Set(
    (Namespace.Xsd + "string").iri().trim,
    (Namespace.Xsd + "time").iri().trim
  )

  def getDateOnlyHierarchy: Set[String] = Set(
    (Namespace.Xsd + "string").iri().trim,
    (Namespace.Xsd + "date").iri().trim
  )

  def getDateTimeOnlyHierarchy: Set[String] = Set(
    (Namespace.Xsd + "string").iri().trim,
    (Namespace.Shapes + "dateTimeOnly").iri().trim
  )

  def getDateTimeHierarchy: Set[String] = Set(
    (Namespace.Xsd + "string").iri().trim,
    (Namespace.Xsd + "dateTime").iri().trim
  )

  def getBooleanHierarchy: Set[String] = Set(
    (Namespace.Xsd + "boolean").iri().trim
  )

  def getIntegerHierarchy: Set[String] = Set(
    (Namespace.Xsd + "integer").iri().trim
  )

  def getNumberHierarchy: Set[String] =
    Set(
      (Namespace.Xsd + "double").iri().trim,
      (Namespace.Xsd + "float").iri().trim,
      (Namespace.Xsd + "long").iri().trim
    ) ++ getIntegerHierarchy

  def getDoubleHierarchy: Set[String] = getNumberHierarchy

  def getFloatHierarchy: Set[String] = getNumberHierarchy

  def getStringHierarchy: Set[String]
}

object DataNodeTypeHierarchyLogicalConstraint extends DataNodeTypeHierarchy {

  override def getStringHierarchy: Set[String] =
    Set(
      (Namespace.Xsd + "string").iri().trim
    ) ++ getTimeHierarchy ++ getDateOnlyHierarchy ++ getDateTimeOnlyHierarchy ++ getDateTimeHierarchy

}

object DataNodeTypeHierarchyStandard extends DataNodeTypeHierarchy {

  override def getStringHierarchy: Set[String] =
    Set(
      (Namespace.Xsd + "string").iri().trim
    ) ++ getTimeHierarchy ++ getDateOnlyHierarchy ++ getDateTimeOnlyHierarchy ++ getDateTimeHierarchy ++
      getBooleanHierarchy ++ getNumberHierarchy

}
