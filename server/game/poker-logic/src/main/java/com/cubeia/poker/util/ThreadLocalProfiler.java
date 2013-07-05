/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.poker.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class ThreadLocalProfiler {

    private static ThreadLocal<Map<String, Long>> calls = new ThreadLocal<Map<String, Long>>();

    public static void start() {
        calls.set(new LinkedHashMap<String, Long>());
        add("start");
    }

    public static void stop() {
        add("stop");
    }

    public static void clear() {
        calls.remove();
    }

    public static Map<String, Long> get() {
        return calls.get();
    }

    public static void add(String method) {
        if (calls.get() != null) {
            calls.get().put(method, System.currentTimeMillis());
        }
    }

    public static String getCallStackAsString() {
        Map<String, Long> map = calls.get();
        if (map == null) {
            return "No calls recorded, thread local is null";
        }

        Long start = map.get("start");

        String result = "Profiled Calls:\n";
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            Long time = entry.getValue();
            Long timeSinceStart = time - start;
            result += "\t" + entry.getKey() + "\t" + timeSinceStart + "\n";
        }

        return result;
    }

}
