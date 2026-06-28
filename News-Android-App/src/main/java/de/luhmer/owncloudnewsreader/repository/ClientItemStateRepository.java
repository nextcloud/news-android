/*
 * Android ownCloud News
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.luhmer.owncloudnewsreader.repository;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import de.luhmer.owncloudnewsreader.model.ClientItemState;

/**
 * App-scoped in-memory store for {@link ClientItemState}, keyed by RSS item id.
 * <p>
 * Provided as a Dagger singleton so the state survives both ViewPager fragment recycling and
 * leaving/re-entering {@link de.luhmer.owncloudnewsreader.NewsDetailActivity} within a session.
 * The state is intentionally <b>not</b> persisted yet; this class is the seam where a persistent
 * backend (e.g. a greenDAO / Room table) could be added later without touching its callers.
 */
public class ClientItemStateRepository {

    private final Map<Long, ClientItemState> states = new HashMap<>();

    public ClientItemStateRepository() {
        // Created by Dagger (see ApiModule#provideClientItemStateRepository)
    }

    /**
     * Returns the {@link ClientItemState} for the given item, creating an empty one on first access.
     * Never returns null so callers can read/write state without null checks.
     */
    @NonNull
    public synchronized ClientItemState get(long itemId) {
        ClientItemState state = states.get(itemId);
        if (state == null) {
            state = new ClientItemState();
            states.put(itemId, state);
        }
        return state;
    }

    /** Drops all cached state (e.g. could be called after a sync removes items). */
    public synchronized void clear() {
        states.clear();
    }
}
