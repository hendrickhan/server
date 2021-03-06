package org.nasdanika.core.convert.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nasdanika.core.Context;
import org.nasdanika.core.Converter;

public class Array {
	
	public static class ToJSON implements Converter<Object, JSONArray, Context> {

		@Override
		public JSONArray convert(
				Object source, 
				Class<JSONArray> target,
				Context context) throws Exception {
			if (source.getClass().isArray()) {
				JSONArray ret = new JSONArray();
				Object[] sa = (Object[]) source;
				for (Object sae: sa) {
					ret.put(context.convert(sae, JSONObject.class));
				}
				return ret;
			}
			
			return null;
		}

		@Override
		public void close() throws Exception {
			// NOP
		}
		
	}
	
	public static class FromJSON implements Converter<JSONArray, Object, Context> {

		@Override
		public Object convert(
				JSONArray source, 
				Class<Object> target,
				Context context) throws Exception {
			Object[] ret = new Object[source.length()];
			for (int i=0; i<ret.length; ++i) {
				ret[i] = context.convert(source.get(i), Object.class);
			}
			return ret;
		}

		@Override
		public void close() throws Exception {
			// NOP
		}
		
	}

}
