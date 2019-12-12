package amf.plugins.document.webapi.validation.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONString;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
  * Custom JSON Object which overrides org.json implementation to add a validation for trailing commas
  */
public class JSONObject extends org.json.JSONObject {

    public JSONObject(JSONTokener x) throws JSONException {
        super();
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            c = x.nextClean();
            switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                default:
                    x.back();
                    key = x.nextValue().toString();
            }

            // The key is followed by ':'.

            c = x.nextClean();
            if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }

            // Use syntaxError(..) to include error location

            if (key != null) {
                // Check if key exists
                if (this.opt(key) != null) {
                    // key already exists
                    throw x.syntaxError("Duplicate key \"" + key + "\"");
                }
                // Only add value if non-null
                Object value = x.nextValue();
                if (value!=null) {
                    this.put(key, value);
                }
            }

            // Pairs are separated by ','.

            switch (x.nextClean()) {
                case ';':
                case ',':
                    if (x.nextClean() == '}') {
                        // This is the only modification made over org.json.JSONObject implementation
                        throw x.syntaxError("Invalid trailing comma");
                    }
                    x.back();
                    break;
                case '}':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

}


