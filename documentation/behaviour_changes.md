# AMF Behaviur Changes with Java Parser

* When specifying an string example with a number value, AMF validation will not conform, and it will give you the following message:
  Scalar at / must have data type http://www.w3.org/2001/XMLSchema#string
  
  Example:
  
  ```yaml
  #%RAML 0.8
  From:
   description: |
     The phone number or client identifier to use as the caller id. If
     using a phone number, it must be a Twilio number or a Verified
     outgoing caller id for your account.
   type: string
   required: true
   pattern: (\+1|1)?([2-9]\d\d[2-9]\d{6}) # E.164 standard
   example: +14158675309
  
  ```
  
* Java parser allows set example to body and not to the shape in the body. 
  Actually, java parser handle all media type map as a type ( storing the media type in the shape name).
  For 0.8 this is valid, but in 1.0 not. We only parse the example in 0.8 an store inside the shape. 
  
  Example:
  
  ```yaml
  
  #%RAML 1.0
  title: Test API
  version: 1.0
  
  /subscription:
    post:
      body:
        application/json:
          schema: !include schema.json
          example: !include example.json
  
  ```