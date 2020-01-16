package amf.plugins.document.webapi.validation.json;

import org.json.JSONException;
import org.json.JSONTokener;

/**
 * Custom JSON Array which overrides org.json implementation to add a validation for trailing commas
 */
public class JSONArray extends org.json.JSONArray {

    public JSONArray(JSONTokener x) throws JSONException {
        super();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }

        char nextChar = x.nextClean();
        if (nextChar == 0) {
            // array is unclosed. No ']' found, instead EOF
            throw x.syntaxError("Expected a ',' or ']'");
        }
        if (nextChar != ']') {
            x.back();
            for (;;) {
                if (x.nextClean() == ',') {
                    x.back();
                    this.put(JSONObject.NULL);
                } else {
                    x.back();
                    this.put(x.nextValue());
                }
                switch (x.nextClean()) {
                    case 0:
                        // array is unclosed. No ']' found, instead EOF
                        throw x.syntaxError("Expected a ',' or ']'");
                    case ',':
                        nextChar = x.nextClean();
                        if (nextChar == 0) {
                            // array is unclosed. No ']' found, instead EOF
                            throw x.syntaxError("Expected a ',' or ']'");
                        }
                        if (nextChar == ']') {
                            // This is the only modification made over org.json.JSONArray implementation
                            throw x.syntaxError("Invalid trailing comma");
                        }
                        x.back();
                        break;
                    case ']':
                        return;
                    default:
                        throw x.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }
}
