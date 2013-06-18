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
package com.cubeia.firebase.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A Round Robin iterator implementation.
 * 
 * the method hasNext will *always return true if we haev more then one element
 * since we will loop the elements.
 * 
 * 
 * 
 * Created on 2006-sep-07
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public class RoundRobinList<E> implements Iterator<E>, Collection<E>{

    private List<E> elements = Collections.synchronizedList(new ArrayList<E>());
    
    int index = 0;
    
    public boolean hasNext() {
        return elements.size() > 0;
    }

    public void checkIndex() {
        if (index == elements.size()) {
            index = 0;
        }
    }
    
    public E next() {
        E element = null;
        synchronized (elements) {
            element = elements.get(index);
            index++;
            checkIndex();
        }
        
        
        return element;
    }

    public void remove() {
        synchronized (elements) {
            elements.remove(index);
            checkIndex();
        }
        
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return (elements.size() == 0);
    }

    public boolean contains(Object o) {
        return elements.contains(o);
    }

    public Iterator<E> iterator() {
        return this;
    }

    public Object[] toArray() {
        return elements.toArray();
    }

    @SuppressWarnings("unchecked")
	public Object[] toArray(Object[] a) {
    	return elements.toArray(a);
    }

    public boolean add(E o) {
        return elements.add(o);
    }

    public boolean remove(Object o) {
        return elements.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return elements.containsAll(c);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean addAll(Collection c) {
        return elements.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return elements.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return elements.retainAll(c);
    }

    public void clear() {
        elements.clear();
    }

    
}

