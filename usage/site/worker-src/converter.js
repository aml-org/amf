const amf = require('../../../amf-client/js/target/artifact/amf-client-module.js')

amf.AMF.init()

const stringify = (data) => {
  if (!data) return ''
  if (typeof data === 'string') return data
  const result = JSON.stringify(data, null, 2)
  return result === '{}' ? '' : result
}

const resolve = (error, result) => {
  self.postMessage({
    result: stringify(result),
    error: stringify(error),
    message: error ? error.message : ''
  })
}

function getParser(from) {
  let parser
  if (from === 'raml') {
    parser = amf.AMF.raml10Parser()
  } else if (from === 'oas') {
    parser = amf.AMF.oas20Parser()
  } else {
    parser = amf.AMF.amfGraphParser()
  }
  return parser
}

function getGenerator(to) {
  let generator
  if (to === 'raml') {
    generator = amf.AMF.raml10Generator()
  } else if (to === 'oas') {
    generator = amf.AMF.oas20Generator()
  } else {
    generator = amf.AMF.amfGraphGenerator()
  }
  return generator
}

self.addEventListener('message', (e) => {
  console.dir('amf: ' + amf)
  console.log('e: ' + e)
  const message = e.data
  console.log('message: ' + message)
  console.log('rawdata: ' + message.rawData)
  const from = message.fromLanguage.className
  const to = message.toLanguage.className
  const parser = getParser(from)

  const generator = getGenerator(to)

  parser.parseString(message.rawData, {
    success: function (doc) {
      console.log('result: ' + doc)
      generator.generateString(doc, {
        success: function (doc) {
          console.log('result: ' + doc)
          resolve(null, doc)
        },
        error: function (exception) {
          resolve(exception, exception.toString())
        }
      })
    },
    error: function (exception) {
      resolve(exception, exception.toString())
    }

  })
}, false)
