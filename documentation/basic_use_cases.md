# Basic use cases - parsing & validating an API

In order to validate an API, you need to parse it with one of the parsers provided by AMF:

- raml10Parser
- raml08Parser

This will give you a model (BaseUnit) need it for validation.

AMF has the following models for RAML:
 - Document
 	- Overlay
 	- Extension
 - Fragment
 	- NamedExample
 	- DataType
 	- AnnotationTypeDeclaration
 	- ResourceTypeFragment
 	- SecuritySchemeFragment
 	- PayloadFragment
 	- TraitFragment
 	- DocumentationItem
 	- ExternalFragment (*)

(*) If the parsed file doesn't match any of the previous model an ExternalFragment will be returned.

### Validation snippet:
```scala
amf.client.AMF.init().get() // required initialization
val parser = amf.client.AMF.raml10Parser()
// parse
val baseUnit = parser.parseFileAsync("file://" + file.getPath).get()
// validate
val validations = amf.client.AMF.validate(baseUnit, amf.ProfileNames.RAML, amf.ProfileNames.AMFStyle).get()
```

With validations provided by AMF.validate you can see if the API is valid:
- "validations" will be a ValidationReport that contains:
    - conforms: Boolean - true if the API is valid (i.e. results is empty, or only has validaitons with level "warning")
    - results: ValidationResult[] - Seq with all the ValidationReport that the API doesnt conform.
     	- message: String
    	- level: String
    	- validationid: String
    	- position: Range


## Use cases
### Valid API:

```yaml
#%RAML 1.0
title: DefectsProcessAPI
version: 1.0

/defects/{defectID}/1.0:
  patch:
    body:
      application/json:
        type: object
        properties:
          Task:
            type: object

        example:
          Task:
            TaskNumber: 21328190
```

AMF will return a Document as the BaseUnit as this is a recognized API, validations will conform true and its results will be empty since there are no messages to process.

 - Model:
    - Document
 - ValidationReport: 
	- conforms: true
	- results: Empty seq


### API with errors

```yaml
#%RAML 1.0
title: DefectsProcessAPI
version: 1.0

/defects/{defectID}/1.0:
  patch:
    body:
      application/json:
        type: object
        properties:
          Task:
            type: string

        example:
          TaskNumber: 21
```

AMF will return a Document as the BaseUnit as this is a recognized API. 
Validations will not conform and its results will contain all the errors/warnings processed by AMF.

 - Model:
    - Document
 - ValidationReport: 
	- conforms: false
	- results: ValidationResult[]
		- message: ```Data at //Task must have min. cardinality 1```
		- level: Violation
		- validationid: ```file://file.raml#/web-api/end-points/%2Fdefects%2F%7BdefectID%7D%2F1.0/patch/request/application%2Fjson/schema_validation_Task_validation_minCount/prop```
		- position: Range [(15,0)-(15,24)]


### Any file, not an API

Using an xml for example:

```xml
<widget>
    <debug>on</debug>
</widget>
```

AMF will return an ExternalFragment as the BaseUnit as this is not a recognized API.

 - Model:
 	- ExternalFragment
 - ValidationReport: 
	- conforms: true
	- results: Empty seq
