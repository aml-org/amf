const amf = require('../../../amf-js/target/scala-2.12/amf-opt.js')

const stringify = (data) => {
	if (!data) return '';
	if (typeof data === 'string') return data;
	const result = JSON.stringify(data, null, 2);
	return result === '{}' ? '' : result;
};

const resolve = (error, result) => {
	self.postMessage({
		result: stringify(result),
		error: stringify(error),
		message: error ? error.message : ''
	});
};

self.addEventListener('message', (e) => {
	const message = e.data;

	const client = new amf.JsClient();
	client.convert(message.rawData,message.fromLanguage.className,message.toLanguage.className,
		{
			success: function(doc){
				resolve(null,doc);
			},
			error: function(exception){
				resolve(exception,exception.toString());
			}

		}
	);
}, false);
