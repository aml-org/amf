# Getting Started

Before you get started, you'll want to register with our private npm repository so you can download @mulesoft modules.

```
npm login --registry=https://npm.mulesoft.com --scope=@mulesoft
```

When prompted, enter your github username and password. You may then clone and install the project's dependencies.

If you have 2-factor authentication enabled, you'll get a 401 after attempting to login. Temporarily disable 2FA, login with your github credentials, then re-enable 2FA.

Once you are logged in on the scope @mulesoft just execute the command

```bash
npm install --save @mulesoft/dw-parser-js@develop
```

# Setting up

## Node.js

If you are using Node.js (server side) just import it using

```javascript
import { DataWeaveApi } from '@mulesoft/dw-parser-js'
```

## Browser

### Oldie but goldie standard and beautiful way

Just add the `<script>`.

1. Install the NPM module normally.
2. Copy the `node_modules/@mulesoft/dw-parser-js/index.js` file to your `public` folder.

   ```
   cp node_modules/@mulesoft/dw-parser-js/index.js public/dw-parser.js
   ```
3. Include the file ***BEFORE*** your webpack bundle on your HTML file

   ```html
   <script src="dw-parser.js"></script>
   ```
4. It's working. It exposes everything as global variables. Beautiful.


### Webpack

As this package is auto-generated using scala-js. **It's HUGE** and we couldn't made it work as a bundled package YET.

By the moment we have the following workaround:

1. Install the NPM module normally.
2. You copy the `node_modules/@mulesoft/dw-parser-js/index.js` file to your `public` (or whatever) folder.

   ```
   cp node_modules/@mulesoft/dw-parser-js/index.js public/dw-parser.js
   ```
3. Include the file ***BEFORE*** your webpack bundle on your HTML file

   ```html
   <script src="dw-parser.js"></script>
   <script src="webpack.bundle.js"></script>
   ```
4. In order to tell webpack that `@mulesoft/dw-parser-js` must be ignored, you need to add this line to your config file in the `external` section:

   ```javascript
   var webpackConfig = {
     // ...
     externals: {
        '@mulesoft/dw-parser-js': 'DataWeaveApi'
     }
     // ...
   }
   ```