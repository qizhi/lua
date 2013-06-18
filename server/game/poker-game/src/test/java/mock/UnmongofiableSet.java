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

package mock;

import com.cubeia.firebase.api.util.UnmodifiableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Testable implementation of a Firebase unmodifiable set.
 *
 * @author w
 */
public final class UnmongofiableSet<T> implements UnmodifiableSet<T> {
    private final Set<T> set;

    public UnmongofiableSet(Collection<T> collection) {
        this.set = new HashSet<T>(collection);
    }

    public UnmongofiableSet() {
        this.set = new HashSet<T>();
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public boolean contains(T object) {
        return set.contains(object);
    }
}