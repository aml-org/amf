package amf.core.validation

object ValidationDialectText {

  def text: String =
    """
      |#%RAML 1.0 Dialect
      |
      |dialect: Validation Profile
      |version: 1.0
      |usage: Dialect to describe validations over RAML documents
      |external:
      |  schema-org: "http://schema.org/"
      |  shacl: "http://www.w3.org/ns/shacl#"
      |  validation: "http://raml.org/vocabularies/amf-validation#"
      |nodeMappings:
      |  functionConstraintNode:
      |    classTerm: shacl.JSConstraint
      |    mapping:
      |      message:
      |        propertyTerm: shacl.message
      |        range: string
      |      code:
      |        propertyTerm: validation.jsCode
      |        range: string
      |        #pattern: "^function\(.+\)\s*\{.+\}$"
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
      |        range: number
      |      minCount:
      |        propertyTerm: shacl.minCount
      |        range: number
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
      |      propertyConstraints:
      |        mandatory: true
      |        propertyTerm: shacl.property
      |        asMap: true
      |        hash: validation.ramlPropertyId
      |        range: propertyConstraintNode
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
      |      propertyConstraints:
      |        propertyTerm: shacl.property
      |        asMap: true
      |        hash: validation.ramlPropertyId
      |        range: propertyConstraintNode
      |      targetQuery:
      |        mandatory: true
      |        propertyTerm: validation.targetQuery
      |        range: string
      |
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
      |        asMap: true
      |        hash: validation.ramlPrefixName
      |        hashValue: validation.ramlPrefixUri
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
      |        asMap: true
      |        hash: schema-org.name
      |        range: [ shapeValidationNode, queryValidationNode,functionValidationNode]
      |raml:
      |  fragments:
      |    encodes:
      |      ShapeValidation: queryValidationNode
      |      FunctionValidation: functionValidationNode
      |
      |  module:
      |    declares:
      |      shapes: queryValidationNode
      |      functions: functionValidationNode
      |
      |  document:
      |    encodes: profileNode
    """.stripMargin
}
