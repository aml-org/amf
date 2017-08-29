# AMF

### Requirements
* Scala 2.12.2
* sbt 0.13.15

## sbt useful commands

### Test
* Tests on jvm and js
```sh
sbt test
```

### Generate coverage reports
```sh
sbt coverage test coverageReport
```

## Using AMF

To use AMF you should first generate or get the right distribution for your project and import them as dependencies.

### Getting artifacts for JS

Before you get started, you'll want to register with our private npm repository so you can download @mulesoft modules.

```
npm login --registry=https://npm.mulesoft.com --scope=@mulesoft
```

When prompted, enter your github username and password. You may then clone and install the project's dependencies.

If you have 2-factor authentication enabled, you'll get a 401 after attempting to login. Temporarily disable 2FA, login with your github credentials, then re-enable 2FA.

Once you are logged in on the scope @mulesoft just execute the command

```bash
npm install --save @mulesoft/amf-jenkins@latest
```

Import it using
```javascript
import amf from '@mulesoft/amf-js'
```

and *amf* will be an object containing all the exported classes, for example:
```javascript
const client = new amf.JsClient()
```

### Getting artifacts for JVM

Add the mulesoft ci-snapshots repository and its credentials to the repositories, for example in gradle:

```groovy
maven {
        url 'https://repository-master.mulesoft.org/nexus/content/repositories/ci-snapshots'
        credentials {
            username = "username"
            password = "password"
        }
    }
```

And then add the dependency:

```groovy
dependencies {
    compile 'org.mulesoft:amf_2.12:0.0.1-SNAPSHOT'
}
```

## Use as artifacts

Use *amf* importing the artifacts generated from cloning the project and running *sbt generate*.

### Generate artifacts directly from cloned repository
```sh
sbt generate
```
This will generate two *JS artifacts*:
- **Client**: JS file in amf-js/target/artifact/amf-browser.js that can be imported from a script tag to be used in client side.
- **Server**: JS file in amf-js/target/artifact/amf-module.js that has commonJS modules, and can be installed as a node module.

And two *JVM artifacts*:
- **Jar**: jar file in amf-jvm/target/artifact/amf.jar with the amf library.
- **Javadoc jar**: jar file in amf-jvm/target/artifact/amf-javadoc.jar with the docs of the project in Scaladoc format.

### Using AMF in a node.js project

```bash
npm install --save amf-project-location/amf-js/
```

If you are using *Node.js* (server side) just import it using
```javascript
import amf from '@mulesoft/amf-js'
```

and *amf* will be an object containing all the exported classes, for example:
```javascript
const client = new amf.JsClient()
```

### Using AMF from client browser

Just import the generated JS file in a script tag
```html
<script src="amf-browser.js"></script>
```

and use the exported classes as if they were global ones, for example:
```javascript
const client = new JsClient()
```

# Examples

Inside the *usage* folder we have an example for each of the three usages and a *converter* project to give the library some UI.

### Browser-client

It shows a static *html* file that imports the *amf-browser.js* artifact as a script and uses its globally exported classes.

##### Usage

Just open the *html* file in any browser and play with what's inside the script tag!

### JS Client (node.js)

This example has a *node.js* file **index.js** where amf library will be imported and can be used as a node module.

##### Usage

1. Run *cd usage/jsClient* while standing on the root of the project
2. Run *npm install*
3. Make sure that artifacts for amf have been generated (Run *sbt generate*)
4. Run *npm install ../../amf-js/* (This will pick up configurations from *package.json* to install the *amf-module.js* file as a node module in the jsClient project)
5. Run *node index.js*
6. Open *localhost:3000* in the browser

Modify *index.js* to change the use of the library server side.

### JVM Client

This is a simple example that uses the **JVM jar artifact** in a gradle projects' main file.

##### Usage

1. Make sure that artifacts for amf have been generated (Run *sbt generate*), as the project will pick up the jar from the generated location
2. Play with the library in java files! (There's a main class in src/main/java/ with some examples)

### Converter

This is a node project that demonstrates how amf parses and generates an OAS/RAML document.

##### Usage

1. Make sure that artifacts for amf have been generated (Run *sbt generate*)

2. Inside the *site* directory (*cd usage/site*):
    - Create directory *"build"*
    - Run *npm install*
    - Run *npm start*
    - Open *localhost:3000* in the browser

**Note**: if *npm start* fails, check if you have a *"public/build"* in the *site* directory.