package amf.plugins.document.apicontract.validation.json;

import org.json.JSONException;
import org.json.JSONTokener;

/**
  * Custom JSON Object which overrides org.json implementation to add a validation for trailing commas
  * and stringToValue static method implementation.
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

    /**
     * This redefines JSONObject.stringToValue method so that we can fail when an unquoted value is processed (as all the other json parsers).
     * Method is called from JSONTokenerHack.
     */
    public static Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        }

        // check JSON key words true/false/null
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return org.json.JSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it. If a number cannot be
         * produced, then the value will just be a string.
         */

        char initial = string.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            try {
                return stringToNumber(string);
            } catch (Exception ignore) {
                throw new InvalidJSONValueException("Unquoted string value");
            }
        }
        else {
            throw new InvalidJSONValueException("Unquoted string value");
        }
    }

}


