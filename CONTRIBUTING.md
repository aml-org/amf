# How to contribute to AMF

👍🎉 First off, thanks for taking the time to contribute! 🎉👍

The following is a set of guidelines for contributing to AMF.

## Did you find a bug?
- Ensure the bug was not already reported by searching on GitHub under Issues.
- If you're unable to find an open issue addressing the problem, open a new one. If possible, use the relevant bug report templates to create the issue.
If not, be sure to include a title and clear description, as much relevant information as possible, 
and a code sample or an executable test case demonstrating the expected 
behavior that is not occurring.

## Contributing Changes
- Read and sign the [Contributors License Agreement](#contributors-license-agreement)
- Open a new GitHub pull request with the change.
- Ensure the PR description clearly describes the problem and solution. Include the relevant issue number if applicable.
- Before submitting, please read the [Code contributions](#code-contributions) section to know more about the technical contribution requirements.

## Code Contributions

### Development Requirements
* Scala 2.12.11
* sbt 1.3.9
* Node

### Version control branching
- Always branch from `master` branch to ensure you are updated with the latest release.
- Don’t submit unrelated changes in the same branch/pull request.
- If you need to update your branch because of changes in `master` you should always **rebase**, not **merge**.
- You should always be up-to-date with the latest changes in `master`.

### Generating artifacts (from cloned repository)

```sh
sbt package
```
This will generate *jvm* JARs in each of the module's targets.

```sh
sbt buildJS
```
This will generate a *js* artifact in ./amf-client/js/amf.js

### Code formatting

We use [Scalafmt](https://scalameta.org/scalafmt/) to format our code! Please format your code before opening a Pull Request.

### Tests aren’t optional
Any bugfix that doesn’t include a test proving the existence of the bug being fixed, may be suspect. 
Same for new features that can’t prove they actually work.
 
Writing tests before the implementation is strongly encouraged.

To run tests:
```sh
$ sbt test
$ sbt clientJVM/testOnly // to run tests only for the JVM platform
$ sbt clientJS/testOnly // to run tests only for the JS platform
```

#### Travis CI

A Travis CI script is available for you to run Travis. To be able to do this you must have a Travis account and 
setup Travis for your fork in your account.

#### Test coverage

Contributions must comply with a minimum of 80% coverage rate.

To run a coverage report of the whole project:
```sh
sbt coverage test coverageReport
```

### Contributors License Agreement

In order to accept your pull request, we need you to sign [Salesforce's CLA](https://cla.salesforce.com/sign-cla). You only need to do this once to work on any of Salesforce's open source projects.
