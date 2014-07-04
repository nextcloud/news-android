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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities for generating and parsing URL parameter strings
 *
 * @author aclarke
 *
 */
public class URLParams
{
    /**
     * Parse the query string, placing the results in <code>paramsToFill</code>,
     * assuming UTF-8 encoding.
     *
     * @param queryString
     *          The query string (without leading "?").
     * @param paramsToFill
     *          A list to add entries to; the list is not emptied first by this
     *          method. Entries are added in the order they are found.
     * @see URLParams#parseQueryString(String, List, String)
     */
    public static void parseQueryString(final String queryString,
                                        final List<KeyValuePair> paramsToFill)
    {
        try
        {
            parseQueryString(queryString, paramsToFill, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("What?  UTF-8 isn't a valid encoding", e);
        }
    }

    /**
     * Parse the query string, and return as a new list, assuming UTF-8 encoding.
     *
     * @param queryString
     *          The query string (without leading "?").
     * @return A new list with all entries included, in the order they were found.
     * @see URLParams#parseQueryString(String, List, String)
     */
    public static List<KeyValuePair> parseQueryString(final String queryString)
    {
        final List<KeyValuePair> retval = new ArrayList<KeyValuePair>();
        parseQueryString(queryString, retval);
        return retval;
    }

    /**
     * Parse the query string, and return as a new list, assuming UTF-8 encoding.
     *
     * @param queryString
     *          The query string (without leading "?").
     * @param encoding
     *          An encoding (e.g. "UTF-8") to decode parameters as.
     * @return A new list with all entries included, in the order they were found.
     * @throws UnsupportedEncodingException
     *           If the <code>encode</code> specified is not supported.
     * @see URLParams#parseQueryString(String, List, String)
     */
    public static List<KeyValuePair> parseQueryString(final String queryString,
                                                      final String encoding) throws UnsupportedEncodingException
    {
        final List<KeyValuePair> retval = new ArrayList<KeyValuePair>();
        parseQueryString(queryString, retval, encoding);
        return retval;
    }

    /**
     * Parse the query string, placing the results in <code>paramsToFill</code>.
     *
     * @param queryString
     *          The query string (without leading "?").
     * @param parametersToFill
     *          A list to add entries to; the list is not emptied first by this
     *          method. Entries are added in the order they are found.
     * @param encoding
     *          An encoding (e.g. "UTF-8") to decode parameters as.
     * @throws UnsupportedEncodingException
     *           If the <code>encode</code> specified is not supported.
     * @see URLParams#parseQueryString(String, List, String)
     */
    public static void parseQueryString(final String queryString,
                                        final List<KeyValuePair> parametersToFill, final String encoding)
            throws UnsupportedEncodingException
    {
        final String params[] = queryString.split("&");
        for (String param : params)
        {
            final String[] tuple = param.split("=");
            final KeyValuePair retval = new KeyValuePair(URLDecoder.decode(
                    tuple[0], encoding), tuple.length > 1 ? URLDecoder.decode(tuple[1],
                    encoding) : null);
            parametersToFill.add(retval);
        }
    }

    /**
     * Generates a query string suitable for appending to a URI that encodes the
     * given parameters as UTF-8 encoding strings.
     *
     * @param params
     *          A list of name value pairs to encode.
     * @return A string suitable for appending to URIs.
     */
    public static String generateQueryString(
            final List<? extends IKeyValuePair> params)
    {
        try
        {
            return generateQueryString(params, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("What?  UTF-8 isn't a valid encoding", e);
        }
    }

    /**
     * Generates a query string suitable for appending to a URI that encodes the
     * given parameters as specified by the encoding parameter.
     *
     * @param params
     *          A list of name value pairs to encode.
     * @param encoding
     *          The text encoding to use
     * @return A string suitable for appending to URIs.
     * @throws UnsupportedEncodingException
     *           If the encoding is not supported.
     */
    public static String generateQueryString(
            final List<? extends IKeyValuePair> params, final String encoding)
            throws UnsupportedEncodingException
    {
        if (params == null)
            throw new IllegalArgumentException("need params");
        if (encoding == null)
            throw new UnsupportedEncodingException();

        final StringBuilder builder = new StringBuilder();
        final Iterator<? extends IKeyValuePair> iterator = params.iterator();
        boolean hasNext = iterator.hasNext();
        while (hasNext)
        {
            IKeyValuePair pair = iterator.next();
            if (pair != null)
            {
                final String key = pair.getKey();
                final String value = pair.getValue();
                builder.append(URLEncoder.encode(key, encoding));
                if (value != null)
                {
                    builder.append("=");
                    builder.append(URLEncoder.encode(value, encoding));
                }
            }
            if (iterator.hasNext())
            {
                if (pair != null)
                    builder.append("&");
                hasNext = true;
            }
            else
            {
                hasNext = false;
            }
        }
        return builder.toString();
    }
}

