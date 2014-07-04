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


import java.util.Map.Entry;


/**

 * An implementation of {@link IKeyValuePair} with immutable keys

 * but resettable values.

 *

 * @author aclarke

 *

 */

public class KeyValuePair implements IKeyValuePair, Entry<String, String>

{

    private final String mKey;

    private String mValue;



    /**

     * Creates a new key-value pair.

     *

     * @param key

     *          The key; may not be null.

     * @param value

     *          The value; may be null.

     * @throws IllegalArgumentException

     *           if <code>name==null</code>.

     */

    public KeyValuePair(final String key, final String value)

    {

        if (key == null)

            throw new IllegalArgumentException("must have non null name");

        mKey = key;

        mValue = value;

    }



    /**

     * {@inheritDoc}

     */

    public String getKey()

    {

        return mKey;

    }



    /**

     * {@inheritDoc}

     */

    public String getValue()

    {

        return mValue;

    }



    /**

     * {@inheritDoc}

     */

    public String setValue(String value)

    {

        final String retval = mValue;

        mValue = value;

        return retval;

    }

}

