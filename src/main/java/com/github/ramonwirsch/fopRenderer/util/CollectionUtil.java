package com.github.ramonwirsch.fopRenderer.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ramon on 03.08.2016.
 */
public class CollectionUtil {
	public static <T> Map<String, T> immutableMap(T... keyVals) {
		Map<String, T> map = new HashMap<>();
		String key = null;
		for (T keyVal : keyVals) {
			if (key == null) {
				key = (String) keyVal;
			} else {
				map.put(key, keyVal);
				key = null;
			}
		}

		if (key != null) {
			throw new IllegalStateException("Uneven number of args. Use key, value, key, value ...");
		}

		return Collections.unmodifiableMap(map);
	}
}
