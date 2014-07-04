package de.luhmer.owncloudnewsreader.collections;

/*
 * Copyright (c) 2008, 2009 by Xuggle Incorporated.  All rights reserved.
 *
 * This file is part of Xuggler.
 *
 * You can redistribute Xuggler and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Xuggler is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Some useful utilities for mapping lists to maps and back.
 * @author aclarke
 *
 */
public class MapUtils
{
    /**
     * How {@link MapUtils#listToMap(List, Map, ListToMapMode)} treats
     * duplicate values in the input list.
     * @author aclarke
     *
     */
    public enum ListToMapMode
    {
        /**
         * Last item in list always wins.
         */
        LAST_WINS,
        /**
         * First item in list always wins.
         */
        FIRST_WINS,
    }

    /**
     * Converts a list of key-value pairs into a {@link Map}, with either
     * the first duplicate key always being inserted, or the last duplicate key
     * always being inserted.
     *
     * @param list
     *          The list of name value pairs to convert.
     * @param mapToFill
     *          The map to fill. This method will empty the list first.
     * @param mode
     *          How duplicate values in <code>list</code> should be treated.
     */
    public static void listToMap(final List<? extends IKeyValuePair> list,
                                 final Map<String, String> mapToFill, final ListToMapMode mode)
    {
        if (list == null)
            throw new IllegalArgumentException();
        if (mapToFill == null)
            throw new IllegalArgumentException();
        mapToFill.clear();
        for (IKeyValuePair pair : list)
        {
            if (pair != null)
            {
                if (mode == ListToMapMode.FIRST_WINS)
                {
                    if (mapToFill.containsKey(pair.getKey()))
                        continue;
                }
                mapToFill.put(pair.getKey(), pair.getValue());
            }
        }
    }

    /**
     * Converts a list of key-value pairs into a {@link Map}, with either
     * the first duplicate key always being inserted, or the last duplicate key
     * always being inserted.
     *
     * @param list
     *          The list of name value pairs to convert.
     * @param mode
     *          How duplicate values in <code>list</code> should be treated.
     * @return a new {@link Map} of key-value pairs.
     */
    public static Map<String, String> listToMap(
            final List<? extends IKeyValuePair> list, final ListToMapMode mode)
    {
        final Map<String, String> retval = new HashMap<String, String>();
        listToMap(list, retval, mode);
        return retval;
    }

    /**
     * Converts a map into a {@link List} of {@link IKeyValuePair} objects.
     * @param map Map to convert.
     * @param listToFill List to fill.  The list has {@link List#clear()} called
     *   before any items are added.
     */
    public static void mapToList(final Map<String, String> map, final List<IKeyValuePair> listToFill)
    {
        if (map == null || listToFill == null)
            throw new IllegalArgumentException();
        final Set<Entry<String, String>> entries = map.entrySet();
        for(Entry<String, String> entry : entries)
        {
            final String name = entry.getKey();
            final String value = entry.getValue();
            if (name == null)
                continue;
            IKeyValuePair pair = new KeyValuePair(name, value);
            listToFill.add(pair);
        }
    }
    /**
     * Converts a map into a {@link List} of {@link IKeyValuePair} objects, and
     * returns the new list.
     * @param map Map to convert.
     * @return A new {@link List} containing all key-value pairs in <code>map</code>
     *   as {@link IKeyValuePair} objects.
     */
    public static List<IKeyValuePair> mapToList(final Map<String, String> map)
    {
        final List<IKeyValuePair> retval = new ArrayList<IKeyValuePair>();
        mapToList(map, retval);
        return retval;
    }


}


