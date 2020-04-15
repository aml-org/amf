package amf.plugins.features.validation.model

object ValidationDialectText {

  def text: String =
    """
      |#%Dialect 1.0
      |
      |dialect: Validation Profile
      |version: 1.0
      |usage: Dialect to describe validations over RAML documents
      |external:
      |  schema-org: "http://schema.org/"
      |  shacl: "http://www.w3.org/ns/shacl#"
      |  validation: "http://a.ml/vocabularies/amf-validation#"
      |nodeMappings:
      |  functionConstraintNode:
      |    classTerm: shacl.JSConstraint
      |    mapping:
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      code:
      |        range: string
      |        propertyTerm: validation.jsCode
      |        #pattern: '^function\(.+\)\s*\{.+\}$'
      |      libraries:
      |        propertyTerm: shacl.jsLibrary
      |        range: string
      |        allowMultiple: true
      |      functionName:
      |        propertyTerm: shacl.jsFunctionName
      |        range: string
      |  propertyConstraintNode:
      |    classTerm: shacl.PropertyShape
      |    mapping:
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      name:
      |        propertyTerm: validation.ramlPropertyId
      |        mandatory: true
      |        range: string
      |      pattern:
      |        propertyTerm: shacl.pattern
      |        range: string
      |      maxCount:
      |        propertyTerm: shacl.maxCount
      |        range: integer
      |      minCount:
      |        propertyTerm: shacl.minCount
      |        range: integer
      |      minExclusive:
      |        propertyTerm: shacl.minExclusive
      |        range: number
      |      maxExclusive:
      |        propertyTerm: shacl.maxExclusive
      |        range: number
      |      minInclusive:
      |        propertyTerm: shacl.minInclusive
      |        range: number
      |      maxInclusive:
      |        propertyTerm: shacl.maxInclusive
      |        range: number
      |      datatype:
      |         propertyTerm: shacl.datatype
      |         range: string
      |      in:
      |        propertyTerm: shacl.in
      |        allowMultiple: true
      |        range: any
      |      nested:
      |        propertyTerm: shacl.node
      |        range: [ shapeValidationNode, queryValidationNode, functionValidationNode, xoneShapeValidationNode, orShapeValidationNode, notShapeValidationNode, andShapeValidationNode]
      |      atLeast:
      |        propertyTerm: shacl.atLeastNode
      |        range: qualifiedShapeValidationNode
      |      atMost:
      |        propertyTerm: shacl.atMostNode
      |        range: qualifiedShapeValidationNode
      |      equalsToProperty:
      |        propertyTerm: shacl.equals
      |        range: string
      |      disjointWithProperty:
      |        propertyTerm: shacl.disjoint
      |        range: string
      |      lessThanProperty:
      |        propertyTerm: shacl.lessThan
      |        range: string
      |      lessThanOrEqualsToProperty:
      |        propertyTerm: shacl.lessThanOrEquals
      |        range: string
      |  functionValidationNode:
      |    classTerm: validation.FunctionValidation
      |    mapping:
      |      name:
      |        propertyTerm: schema-org.name
      |        range: string
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      targetClass:
      |        propertyTerm: validation.ramlClassId
      |        range: string
      |        allowMultiple: true
      |      functionConstraint:
      |        mandatory: true
      |        propertyTerm: shacl.js
      |        range: functionConstraintNode
      |  qualifiedShapeValidationNode:
      |    classTerm: validation.QualifiedShapevalidationNode
      |    mapping:
      |      count:
      |        propertyTerm: shacl.count
      |        range: integer
      |        mandatory: true
      |      validation:
      |        propertyTerm: shacl.valueShape
      |        range: [ shapeValidationNode, queryValidationNode, functionValidationNode, xoneShapeValidationNode, orShapeValidationNode, notShapeValidationNode, andShapeValidationNode]
      |        mandatory: true
      |  shapeValidationNode:
      |    classTerm: validation.ShapeValidation
      |    mapping:
      |      name:
      |        propertyTerm: schema-org.name
      |        range: string
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      targetClass:
      |        propertyTerm: validation.ramlClassId
      |        range: string
      |        allowMultiple: true
      |      classConstraints:
      |        propertyTerm: shacl.class
      |        range: string
      |        allowMultiple: true
      |      propertyConstraints:
      |        mandatory: true
      |        propertyTerm: shacl.property
      |        mapKey: name
      |        range: propertyConstraintNode
      |  andShapeValidationNode:
      |    classTerm: validation.AndShapeValidation
      |    mapping:
      |      name:
      |        propertyTerm: schema-org.name
      |        range: string
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      targetClass:
      |        propertyTerm: validation.ramlClassId
      |        range: string
      |        allowMultiple: true
      |      and:
      |        propertyTerm: shacl.and
      |        range: [ shapeValidationNode, queryValidationNode, functionValidationNode, xoneShapeValidationNode, orShapeValidationNode, notShapeValidationNode, andShapeValidationNode]
      |        allowMultiple: true
      |  notShapeValidationNode:
      |    classTerm: validation.NotShapeValidation
      |    mapping:
      |      name:
      |        propertyTerm: schema-org.name
      |        range: string
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      targetClass:
      |        propertyTerm: validation.ramlClassId
      |        range: string
      |        allowMultiple: true
      |      not:
      |        propertyTerm: shacl.not
      |        range: [ shapeValidationNode, queryValidationNode, functionValidationNode, xoneShapeValidationNode, orShapeValidationNode, notShapeValidationNode, andShapeValidationNode]
      |  orShapeValidationNode:
      |    classTerm: validation.OrShapeValidation
      |    mapping:
      |      name:
      |        propertyTerm: schema-org.name
      |        range: string
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      targetClass:
      |        propertyTerm: validation.ramlClassId
      |        range: string
      |        allowMultiple: true
      |      or:
      |        propertyTerm: shacl.or
      |        range: [ shapeValidationNode, queryValidationNode, functionValidationNode, xoneShapeValidationNode, orShapeValidationNode, notShapeValidationNode, andShapeValidationNode]
      |        allowMultiple: true
      |  xoneShapeValidationNode:
      |    classTerm: validation.XoneShapeValidation
      |    mapping:
      |      name:
      |        propertyTerm: schema-org.name
      |        range: string
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      targetClass:
      |        propertyTerm: validation.ramlClassId
      |        range: string
      |        allowMultiple: true
      |      xone:
      |        propertyTerm: shacl.xone
      |        range: [ shapeValidationNode, queryValidationNode, functionValidationNode, xoneShapeValidationNode, orShapeValidationNode, notShapeValidationNode, andShapeValidationNode]
      |        allowMultiple: true
      |  queryValidationNode:
      |    classTerm: validation.QueryValidation
      |    mapping:
      |      name:
      |        propertyTerm: schema-org.name
      |        range: string
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      targetClass:
      |        propertyTerm: validation.ramlClassId
      |        range: string
      |        allowMultiple: true
      |      query:
      |        mandatory: true
      |        propertyTerm: validation.targetQuery
      |        range: string
      |  ramlPrefixNode:
      |    classTerm: validation.RamlPrefix
      |    mapping:
      |      prefix:
      |        propertyTerm: validation.ramlPrefixName
      |        range: string
      |      uri:
      |        propertyTerm: validation.ramlPrefixUri
      |        range: string
      |
      |  profileNode:
      |    classTerm: validation.Profile
      |    mapping:
      |      prefixes:
      |        propertyTerm: validation.ramlPrefixes
      |        mapKey: prefix
      |        mapValue: uri
      |        range: ramlPrefixNode
      |      profile:
      |        propertyTerm: schema-org.name
      |        mandatory: true
      |        range: string
      |      description:
      |        propertyTerm: schema-org.description
      |        range: string
      |      extends:
      |        propertyTerm: validation.extendsProfile
      |        range: string
      |      violation:
      |        propertyTerm: validation.setSeverityViolation
      |        range: string
      |        allowMultiple: true
      |      info:
      |        propertyTerm: validation.setSeverityInfo
      |        range: string
      |        allowMultiple: true
      |      warning:
      |        propertyTerm: validation.setSeverityWarning
      |        range: string
      |        allowMultiple: true
      |      disabled:
      |        propertyTerm: validation.disableValidation
      |        range: string
      |        allowMultiple: true
      |      validations:
      |        propertyTerm: validation.validations
      |        mapKey: name
      |        range: [ shapeValidationNode, queryValidationNode, functionValidationNode, xoneShapeValidationNode, orShapeValidationNode, notShapeValidationNode, andShapeValidationNode]

      |  validationShapesUnion:
      |    union:
      |      - shapeValidationNode
      |      - queryValidationNode
      |      - xoneShapeValidationNode
      |      - orShapeValidationNode
      |      - notShapeValidationNode
      |      - andShapeValidationNode
      |
      |documents:
      |  fragments:
      |    encodes:
      |      ShapeValidation: queryValidationNode
      |      FunctionValidation: functionValidationNode
      |
      |  library:
      |    declares:
      |      validations: validationShapesUnion
      |      functions: functionValidationNode
      |
      |  root:
      |    encodes: profileNode
    """.stripMargin
}
