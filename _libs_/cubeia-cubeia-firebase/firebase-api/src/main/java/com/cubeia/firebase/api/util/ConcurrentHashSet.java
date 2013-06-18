/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.api.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple Set implementation backed by a {@link java.util.concurrent.ConcurrentHashMap} to deal with the fact that the
 * JDK does not have a proper concurrent Set implementation that uses efficient lock striping.
 * <p/>
 * Note that values are stored as keys in the underlying Map, with a static dummy object as value.
 *
 */
public class ConcurrentHashSet<E> extends AbstractSet<E>
{
   protected ConcurrentHashMap<E, Object> map;
   private static final Object DUMMY = new Object();

   public ConcurrentHashSet()
   {
      map = new ConcurrentHashMap<E, Object>();
   }

   public int size()
   {
      return map.size();
   }

   public boolean isEmpty()
   {
      return map.isEmpty();
   }

   public boolean contains(Object o)
   {
      return map.containsKey(o);
   }

   public Iterator<E> iterator()
   {
      return map.keySet().iterator();
   }
   public Object[] toArray()
   {
      return map.keySet().toArray();
   }

   public <T> T[] toArray(T[] a)
   {
      return map.keySet().toArray(a);
   }

   public boolean add(E o)
   {
      Object v = map.put(o, DUMMY);
      return v == null;
   }

   public boolean remove(Object o)
   {
      Object v = map.remove(o);
      return v != null;
   }

   public boolean containsAll(Collection<?> c)
   {
      return map.keySet().containsAll(c);
   }

   public boolean addAll(Collection<? extends E> c)
   {
      throw new UnsupportedOperationException("Not supported in this implementation since additional locking is required and cannot directly be delegated to multiple calls to ConcurrentHashMap");
   }

   public boolean retainAll(Collection<?> c)
   {
      throw new UnsupportedOperationException("Not supported in this implementation since additional locking is required and cannot directly be delegated to multiple calls to ConcurrentHashMap");
   }

   public boolean removeAll(Collection<?> c)
   {
      throw new UnsupportedOperationException("Not supported in this implementation since additional locking is required and cannot directly be delegated to multiple calls to ConcurrentHashMap");
   }

   public void clear()
   {
      map.clear();
   }
}
