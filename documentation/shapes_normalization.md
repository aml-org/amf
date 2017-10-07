# Shape Normalization Algorithms

In this section we introduce two homomorphic transformations for RAML types that can be used to obtain a canonical representation of the type.


## Expanded Form

A RAML type expressed in expanded form has the following properties:

- All type expressions have been expanded
- All type names have been replaced by their expanded forms
- All nested types in the RAML type have an explicit type property that can be a built-in type name string or the declaration of a expanded RAML type (single inheritance) or array of RAML types (multiple inheritance)
- All facets with default values like `required` are made explicit
- All nullable property values have been replaced by unions

The algorithm takes as input a RAML type form and a map of type bindings, a dictionary mapping type names (strings) into type expressions.
The output of the algorithm is the expanded form for the RAML type.

Before describing the algorithm, we need to describe the data model for a RAML type form.

A RAML type form is defined using the following algebraic data types:

``` clojure
(constructor Record [a1:String  f1:RAMLForm, ..., an:String fn:RAMLForm])
(constructor Seq [a1:RAMLForm, ... an:RAMLForm])
(constructor Scalar String | Integer | Boolean | $recur | ...)
(constructor RAMLForm Scalar | Record | Seq)

```

Since we are going to expand all type expressions, we need to provide a form representation for RAML union type, not currently defined in the spec:

``` clojure
(constructor Union [a1:RAMLForm, an:RAMLForm]  (Record "type" "union", "of" (Seq a1 ... an)))
```

RAML types can be recursive, at the same time they are anonymous, no identifier for a type exist. To address both problems, we introduce another type form to designate a fixpoint recursion:

``` clojure
(constructor Fixpoint RAMLForm)
```

Where the recursion point is marked by the scalar type ```$recur```. Also note that there are not nesting restrictions on fixpoints.


It is trivial to create a mapping for a concrete syntax (JSON, XML, RAML) into these data types.

For example, provided the following definition of RAML types

``` raml
#%RAML 1.0 Library

types:
  Song:
    properties:
      title: string
      length: number
  Album:
    properties:
      title: string
      songs: Song[]
```

The output of expanding the `Album` type is the following

``` clojure
{"type" "object"
 "properties" {"title" {"type" "string"
                        "required" true}
               "songs" {"type" "array"
                        "items" {"type" "object"
                                 "properties" {"title"  {"type" "string" "required" true}
                                               "length" {"type" "number" "required" true}}
                                "additional-properties" true
                                "required" true}
               "required" true}}
 "additional-properties" true
 "required" true}
```

A recursive ```List``` type like:

``` raml
types:
  List:
    cell: Cell
  Cell:
    properties:
      car: any
      cdr: List | nil
```

Will be expanded in the following form:

``` clojure
{:type :fixpoint,
 :value {:type "object",
         :properties {"cell" {:properties {"car" {:type "any", :required true},
                                           "cdr" {:anyOf [{:type :$recur, :required true}
                                                          {:type "nil", :required true}],
                                                  :type "union",
                                                  :required true}},
                              :additionalProperties true,
                              :type "object",
                              :required true}},
         :additionalProperties true,
         :required true}}
```

The pseudo-code for the transformation is the following:


The input for the algorithm is:
  - `form` The form being expanded
  - `bindings` A `Record` from `String` into `RAMLForm` holding a mapping from user defined RAML type names to RAML type forms.
  - `top-level-type` a `String` with the default RAML type whose base type is not explicit and cannot be inferred, it can be `any` or `string` depending if the the type comes from the `body` of RAML service definition or any other node.

*Algorithm*

1. if `form` is a `String`
   1. if `form` is a RAML built-in data type, we return `(Record "type" form)`
   2. if `form` is a Type Expression, we return the output of calling the algorithm recursively with the parsed type expression and the provided `bindings`
   3. if `form` is a key in `bindings`:
      1. If the type hasn't been traversed yet, we return the output of invoking the algorithm recursively with the value for `form` found in `bindings` and the `bindings` mapping and we add the type to the current traverse path
      2. If the type has been traversed:
         1. We mark the value for the current form as a fixpoint recursion: `$recur`
         2. We find the container form matching the recursion type and we wrap it into a `(fixpoint RAMLForm)` form.
   4. else we return an error
2. if `form` is a `Record`
   1. we initialize a variable `type`
      1. if `type` has a defined value in `form` we initialize `type` with that value
      2. if `form` has a `properties` key defined, we initialize `type` with the value `object`
      3. if `form` has a `items` key defined, we initialize `type` with the value `object`
      4. otherwise we initialise `type` with the value passed in `top-level-type`
   2. if `type` is a `String` with  value `array`
      1. we initialize the value `expanded-items` with the result of invoking the algorithm on the value in `form` for the key `items`
      2. we replace the value of the key `items` in `form` by `expanded-items`
   3. if `type` is a `String` with value `object`
      1. we iterate over the `Record` associated to the key `properties` in `form`
         1. for each pair `(Seq property-name property-value)` in the record associated to the key `properties` in `form`
            1. we initialize the variable `expanded-property-value` with the value of  invoking the algorithm with input arguments `property-value` and the provided `bindings`
         2. if the string `property-name` finishes with a `?` string
            1. we remove the key `property-name` from the `properties` record
            2. we replace the `?` character in `property-name` by the empty string and
            3. we re-assigned the value of `expanded-property-value` by the union `(Union expanded-property-value (Record "type" "null"))`
         3. if the output `Record` assigned to `expanded-property-value` does not have a `required` key, we assign a value `true` to the key
         4. we re-assigned the key `property-name` in the `properties` `Record` to the computed value for `expanded-property-value`
      2. if the `Record` in `form` does not have a defined value for the key `additional-properties`, we assign a value `true` to the key `additional-properties`
   4. if `type` is a `String` with value `union`
      1. we iterate through all the values stored in the key `of` in `form` of type `Seq[RAMLForm]`
         1. we initialize the variable `i` with the position of the value we are iterating and `elem` with the current value
         2. we initialize the variable `expanded-elem` with the output of invoking the algorithm with input arguments `elem` and the provided map of bindings
         3. we replace the value at position `i` in the sequence `of` by the the computed `expanded-elem`
   5. if `type` is a `Record`
      1. we return the output of invoking the algorithm on the value of `type` with the current value for `bindings`
   6. if `type` is a `Seq[RAMLForm]`
      1. we iterate through all the values in `type`
         1. for initialise the variable `i` with the position of the value we are iterating and `elem` with the current value
         2. we initialise the variable `expanded-type` with the output of invoking the algorithm on `elem` with `bindings`
         3. we replace the element `i` in `type` with the computed `expanded-type`
   7. we return the new value for `form`

## Canonical Form

A RAML type expressed in canonical form has all the properties of RAML type in expanded form plus the following:

- Unions can only appear at the top level of the type form
- All inheritance relationships have been resolved
- All `type` properties have a `String` value
- All the constraints defined for the type are valid

The canonical form can be used to represent a RAML type in a unique way. It can also be used to perform validation since inheritance and union types have been resolved.

In order to work with inheritance over RAML types we will consider standard set inclusion semantics, where saying that type `A` is a sub-type of `B` means that all instances of `A` are included in the domain of the type `B`.

The algorithm we show in the following section computes the canonical form for a RAML type.
It takes as input a RAML type in expanded form and produces the canonical form as output or throws an error if an inconsistency structurally or in a constraint is found

For example, provided the following (not expanded) RAML type:

``` raml
properties:
  a: string
  b: number | string
```

The computed canonical form of the type is:

``` clojure
{"type" "union",
 "required" true,
 "of" [{"type" "object"
        "properties" {"a" {"type" "string", "required" true},
                      "b" {"type" "number", "required" true}},
        "additional-properties" true,
        "required" true}
       {"type" "object",
        "properties" {"a" {"type" "string", "required" true},
                      "b" {"type" "string", "required" true}},
        "additional-properties" true,
        "required" true}]}
```

The input of the algorithm is:

- `expanded-form` the form being processed of type `Record[String][RAMLForm]`

*Algorithm*

1. we initialize the variable type with the value of the property `type` of `expanded-form`
2. if `type` is in the set `any boolean datetime datetime-only number integer string null file xml json"`
   1. we return the output of applying the `consistency-check` to the `form`
3. if `type` is the string `array`
   1. we initialize the variable `items` with the output of applying the algorithm to the value of the key `items` of the input `form` of type `Record[String]RAMLForm]`
   2. we initialize the variable `items-type` with the value of the `type` property of the `items` variable
   3. if `items-type` has a value `array`
      1. we replace the property `items` in `form` with the value of `items` variable
      2. we return the output of applying the `consistency-check` algorithm to the new value of `form`
   4. if `items-type` has a value `union`
      1. for each value `elem` in position `i` of the property `of` in `items-type`
         1. we initialize the variable `union-array` cloning the value of `form`
         2. we replace the property `items` of the cloned value in `union-array` with `elem`
         3. we replace the element `i` in the property `of` in `items-type` with the modified value in `union-array`
         4. we return the output of applying the `consistency-check` algorithm to `items-type`
4. if `type` is the string `object`
   1. we initialize the variable properties with the value of the `properties` key in `form`
   2. we initialize the variable `accum` with the cloned value of `form`
   3. we reset the key `properties` in `accum` to an empty record
   4. for each pair `property-name` and `property-value` in the variable `properties`
      1. we initialize the variable `tmp` with the output of invoking the algorithm over the value in `property-value`
      2. if the property `type` of `tmp` has the value `object`
         1. we add the pair `property-name` `tmp` to the `properties` keys in each record in `accum`
      3. if the property `type` of `tmp` has the value `union`
         1. we initialize the variable `new-accum` to the empty sequence
         2. for each value `elem-of` in the property `of` of `tmp`
            1. for each value `elem` in `accum`
               1. we clone `elem`
               2. we add the pair `property-name` `elem-of` to the key `properties` of the cloned`elem`
               3. we add the cloned  `elem` to the sequence `new-new-accum`
         3. we replace `accum` with `new-accum`
      4. if `accum` contains a single element
         1. we return  the output of applying the `consistency-check` algorithm to the only element in `accum`
      5. if `accum` contains more than one element
         1. we replace the `type` of `form` with `union`
         2. we remove the key `properties`
         3. we add the key `of` with the value of `accum`
         4. we return  the output of applying the `consistency-check` algorithm to the modified value of `form`
5. if `type` is a `Record[String][RAMLForm]`
   1. we initialize the variable `super-type-name` to the first value of type string in the chain of nested records for the value `type` starting with the one assigned to `type` in `form`
      1. if `super-type-name` has a value `array` we transform `form` adding the property `items` pointing a record `(Record "type" "any")`
      2. if `super-type-name` has a value `object` we transform `form` adding the property `properties` with the empty record `(Record)`
      3. if `super-type-name` has a value `union` we transform `form` adding the property `of` with the empty sequence `(Seq)`
      4. we initialize the variable `canonical-super-type` to the output of applying the algorithm to the value for the property  `type` in `form`
      5. we set the `type` property of `form` to `super-type-name`
   2. we initialize the variable `tmp` with the output of invoking the algorithm `min-type` to the inputs `canonical-super-type` and `form`
   3. we return the output of applying the `consistency-check` algorithm to the modified value of `tmp`
6. if `type` is `Seq[RAMLForm]`
   1. we initialize the variable `super-type-name` to the first value of type string in the chain of nested records for the value `type` starting with the one assigned to `type` in `form`
      1. if `super-type-name` has a value `array` we transform `form` adding the property `items` pointing a record `(Record "type" "any")`
      2. if `super-type-name` has a value `object` we transform `form` adding the property `properties` with the empty record `(Record)`
      3. if `super-type-name` has a value `union` we transform `form` adding the property `of` with the empty sequence `(Seq)`
      4. we initialize the variable `super-types` to the value for the property  `type` in `form`
      5. we set the `type` property of `form` to `super-type-name`
   2. for each value `elem` in `super-types`
      1. we initialize the variable `tmp` with the output of computing the result of invoking the algorithm `min-type` to `elem` and `form`
      2. we re-assign `form` to the value computed in `tmp`
   3. we return the output of applying the `consistency-check` algorithm to the modified value of `tmp`

In the previous algorithm we have used two auxiliary algorithms `min-type` and `consistency-check`.

`min-type` computes a canonical RAML type that will compute the biggest intersection between the sets defined `super` and `sub`. If such an RAML type is empty, an error will be thrown.

The input of the algorithm is:

- `super` the RAML canonical super-type
- `sub` the RAML canonical sub-type

*Algorithm `min-type`*

1. we initialize the variables `super-type` and `sub-type` with the values of the properties `type` of `super` and `sub` respectively.
2. if `super-type` and `sub-type` have the same value and the value is in the set `any boolean datetime datetime-only number integer string null file xml json"`
   1. we initialize the variable `computed` to the record with property `type` having the common `super-type` and `sub-type` value
   2. for each restriction in `super` and `sub` we compute the narrower restriction and we assign it in `computed`
   3. for each restriction only in `super` or `sub` we assign it directly to `computed`
   3. we return the output of computing the algorithm `consistency-check` on `computed`
3. if only one of `super-type` or `sub-type` has a value of `any`
   1. for each restriction in the `any` type and in the other type, we compute the narrower restriction and we re-assign it to the other type
   2. for each restriction only in `any` we assign it directly to the other type
   3. we return the output of computing the algorithm `consistency-check` on the other type
4. if `super-type` is `number` and the `sub-type` is `integer`
   1. for each restriction in the `number` type and in the `integer` type, we compute the narrower restriction and we re-assign it to the `integer` type
   2. for each restriction only in `number` we assign it directly to the `integer` type
   3. we return the output of computing the algorithm `consistency-check` on the `integer` type
5. if `super-type` is `array` and `sub-type` is `array`
   1. we initialize the variable `min-items` with the output of applying this algorithm to the values for the key `items` in `super` and `sub`
   2. we re-assign the value of the property `items` in `sub` with the value of `min-items`
   3. for each restriction in `super` and `sub` we compute the narrower restriction and we assign it in `sub`
   4. for each restriction only in `super` we assign it directly to `sub`
   5. we return the output of computing the algorithm `consistency-check` on `sub`
5. if `super-type` is `object` and `sub-type` is `object`
   1. for initialize the variable `common-props` to the empty record
   2. for each key in the `properties` value `sub` that is also present in the `properties` value of `super`
      1. we initialize the variable `tmp` with the output of applying the algorithm to the value for the common property in `super` and in `sub`
     2. we assign the computed value using the name of the common property as the key in the `common-props` record
   3. for each pair `property-name` `property-value` only in either `super` or `sub` we add it to the record `common-props`
   4. for each restriction in `super` and `sub` we compute the narrower restriction and we assign it in `sub`
   5. for each restriction only in `super` we assign it directly to `sub`
   6. we assign the value of the key `properties` in `sub` to be `common-props`
   7. we return the output of computing the algorithm `consistency-check` on `sub`
6. if `super-type` is `union` and `sub-type` is `union`
   1. we initialize the variable `accum` to the empty sequence
   2. for each value `elem-super` in the property `of` of `super`
      1. for each value `elem-sub` in the property `of` of `sub`
         1. we add to `accum` the output of applying this algorithm to `elem-super` and `elem-sub`
   3. for each restriction in `super` and `sub` we compute the narrower restriction and we assign it in `sub`
   4. for each restriction only in `super` we assign it directly to `sub`
   5. we assign the value of the key `of` in `sub` to be `accum`
   6. we return the output of computing the algorithm `consistency-check` on `sub`
6. if `super-type` is `union` and `sub-type` is any other type
   2. for each value `i` `elem-super` in the property `of` of `super`
      1. we replace `i` in `of` with the output of applying this algorithm to `elem-super` and `sub`
   3. for each restriction in `super` and `sub` we compute the narrower restriction and we assign it in `super`
   4. for each restriction only in `sub` we assign it directly to `super`
   6. we return the output of computing the algorithm `consistency-check` on `super`

In the previous algorithm we need to define how the narrower version of a constraint is computed.
The following table provides the details:

| property | valid | narrower |
|----------|-------|----------|
| minProperties | (<= super sub) | (max super sub) |
| maxProperties |(>= super sub) | (min super sub) |
| minLength | (<= super sub) | (max super sub) |
| maxLength | (>= super sub) | (min super sub) |
| minimum | (<= super sub) | (max super sub) |
| maximum | (>= super sub) | (min super sub) |
| minItems | (<= super sub) | (max super sub) |
| maxItems | (>= super sub) | (min super sub) |
| format | (or (nil? super) (= super sub)) | (or super sub) |
| pattern | (or (nil? super) (= super sub)) | (or super sub) |
| discriminator | (or (nil? super) (= super sub)) | (or super sub) |
| discriminatorValue | (or (nil? super) (= super sub)) | (or super sub) |
| enumValues | (subset? sub super) | (intersection super sub) |
| uniqueItems | (or (false? super) (= super sub)) | (and super sub) |
| required | (or (false? super) (= super sub)) | (or super sub) |
| additionalProperties | (or (false? super) (= super sub)) | (and super sub) |

If the valid condition in the previous table is not met, the `min-type` algorithm will fail throwing an error.


The other algorithm is `consistency-check`. It just iterates through all the possible restriction constraints defined in the RAML specification and checks that the constraints hold for the provided type using custom logic. The check functions are:

| check name | restrictions | check |
|------------|------------|-------|
| num-properites| `minProperties` and `maxProperties` | `minProperties` <= `maxProperties` |
| length| `minLength` and `maxLength` | `minLength` <= `maxLength` |
| size| `minimum` and `maximum` | `minimum` <= `maximum` |
| num-items | `minItems` and `maxItems` | `minItems` <= `maxItems` |

If any of the restrictions involved in the check are not defined, it automatically succeeds.
In order to support additional restrictions will require to plug in the algorithm additional check functions or extend the structural type system here outlined to provide a generic way of expressing properties and there automatic check.
