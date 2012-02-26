/*
 * Agora Exchange for Online Managed Services
 *
 * Copyright (C) 2012 Sakari A. Maaranen
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.agora_exchange.conversion;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.reflect.TypeToken;

/**
 * A static wrapper for Gson. Also allows switching to an alternative JSON
 * implementation, if we ever want to move away from Gson.
 *
 * @author Sakari A. Maaranen
 */
public abstract class JSON {

    private static final com.google.gson.Gson gson = new com.google.gson.Gson();

    private static final Type TYPE_HASHMAP = new TypeToken<HashMap<String, String>>() {
    }.getType();

    private static final Type TYPE_HASHSET = new TypeToken<HashSet<String>>() {
    }.getType();

    public static final HashMap<String, String> toHashMap(String data) {
        return gson.fromJson(data, TYPE_HASHMAP);
    }

    public static final HashSet<String> toHashSet(String data) {
        return gson.fromJson(data, TYPE_HASHSET);
    }

    public static final <T> T toInstance(String data, Class<T> clazz) {
        return gson.fromJson(data, clazz);
    }

    public static final String toJSON(Object data) {
        return gson.toJson(data);
    }
}
