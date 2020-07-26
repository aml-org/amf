const path = require('path');
const ProgressPlugin = require('webpack').ProgressPlugin;

module.exports = {
  entry: [ path.resolve(__dirname, 'amf.js') ],
  target: 'web',
  output: {
    filename: 'amf-browser.js',
    path: __dirname,
    libraryTarget: 'window',
    library: 'amf'
  },
  devtool: 'source-map',
  node: {
    fs: "empty"
  }
};
