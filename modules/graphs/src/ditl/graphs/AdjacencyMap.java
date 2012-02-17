package ditl.graphs;

import java.util.*;

public abstract class AdjacencyMap<C extends Couple,T> implements Map<C,T> {
	
	Map<Integer,Map<Integer,T>> map = new HashMap<Integer,Map<Integer,T>>();
	int size = 0;
	
	public Map<Integer,T> getStartsWith(Integer i){
		Map<Integer,T> from_map = map.get(i);
		if ( from_map == null )
			return Collections.emptyMap();
		return from_map;
	}
	
	public Set<Integer> vertices(){
		Set<Integer> vertices = new HashSet<Integer>();
		vertices.addAll(map.keySet());
		for ( Map<Integer,T> from_maps : map.values() )
			vertices.addAll(from_maps.keySet());
		return Collections.unmodifiableSet(vertices);
	}
	
	@Override
	public T get(Object key){
		Couple c = (Couple)key;
		Map<Integer,T> from_map = map.get(c.id1());
		if ( from_map != null )
			return from_map.get(c.id2());
		return null;
	}
	
	@Override
	public T put(C c, T obj){
		if ( obj == null ) throw new NullPointerException();
		T prev = put(c.id1(), c.id2(), obj);
		if ( prev == null )
			size++;
		return prev;
	}
	
	protected T put(Integer id1, Integer id2, T obj){
		T prev = null;
		Map<Integer,T> from_map = map.get(id1);
		if ( from_map != null ){
			prev = from_map.put(id2, obj);
		} else {
			from_map = new HashMap<Integer,T>();
			from_map.put(id2, obj);
			map.put(id1, from_map);
		}
		return prev;
	}
	
	
	@Override
	public void putAll(Map<? extends C, ? extends T> m) {
		for ( Map.Entry<? extends C, ? extends T> e : m.entrySet() )
			put(e.getKey(), e.getValue());
	}
	
	@Override
	public T remove(Object key){
		Couple c = (Couple)key;
		T obj = remove(c.id1(), c.id2());
		size--;
		return obj;
	}
	
	protected T remove(Integer id1, Integer id2){
		Map<Integer,T> from_map = map.get(id1);
		T obj = from_map.remove(id2);
		if ( from_map.isEmpty() )
			map.remove(id1);
		return obj;
	}
	
	abstract C newCouple(Integer id1, Integer id2);
	
	Iterator<T> valuesIterator() {
		return new Iterator<T>(){
			Deque<Integer> froms = new LinkedList<Integer>(map.keySet());
			Iterator<T> vi = null;
			
			@Override
			public boolean hasNext() {
				if ( vi == null || ! vi.hasNext() ){
					return ! froms.isEmpty();
				}
				return true;
			}

			@Override
			public T next() {
				if ( vi == null || ! vi.hasNext() ){
					vi = map.get(froms.pop()).values().iterator();
				}
				return vi.next();
			}

			@Override
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}
	
	Iterator<C> keyIterator(){
		return new Iterator<C>(){
			Deque<Integer> froms = new LinkedList<Integer>(map.keySet());
			Iterator<Integer> vi = null;
			Integer id1 = null;
			Set<Integer> cur_set = null;
			
			@Override
			public boolean hasNext() {
				if ( vi == null || ! vi.hasNext() ){
					return ! froms.isEmpty();
				}
				return true;
			}

			@Override
			public C next() {
				if ( vi == null || ! vi.hasNext() ){
					id1 = froms.pop();
					cur_set = map.get(id1).keySet();
					vi = cur_set.iterator();
				}
				Integer id2 = vi.next();
				return newCouple(id1,id2);
			}

			@Override
			public void remove() {
				vi.remove();
				if ( cur_set.isEmpty() )
					map.remove(id1);
				size--;
			}
		};
	}
	
	Iterator<Map.Entry<C,T>> entryIterator(){
		return new Iterator<Map.Entry<C,T>>(){
			Deque<Integer> froms = new LinkedList<Integer>(map.keySet());
			Iterator<Map.Entry<Integer,T>> vi = null;
			Integer id1 = null;
			Map<Integer,T> cur_map = null;
			
			@Override
			public boolean hasNext() {
				if ( vi == null || ! vi.hasNext() ){
					return ! froms.isEmpty();
				}
				return true;
			}

			@Override
			public Map.Entry<C, T> next() {
				if ( vi == null || ! vi.hasNext() ){
					id1 = froms.pop();
					cur_map = map.get(id1);
					vi = cur_map.entrySet().iterator();
				}
				Map.Entry<Integer, T> e = vi.next();
				Integer id2 = e.getKey();
				C c = newCouple(id1,id2);
				return new AbstractMap.SimpleImmutableEntry<C,T>(c,e.getValue());
			}

			@Override
			public void remove() {
				vi.remove();
				if ( cur_map.isEmpty() )
					map.remove(id1);
				size--;
			}
		};
	}


	@Override
	public void clear() {
		for ( Map<Integer,T> from_maps : map.values() )
			from_maps.clear();
		map.clear();
		size = 0;
	}

	@Override
	public boolean containsKey(Object key) {
		Couple c = (Couple)key;
		Map<Integer,T> from_map = map.get(c.id1());
		if ( from_map != null){
			return from_map.containsKey(c.id2());
		}
		return false;
	}

	@Override
	public boolean containsValue(Object o) {
		Iterator<T> i = valuesIterator();
		while ( i.hasNext() ){
			T v  = i.next();
			if ( o==null? v==null : o.equals(v) )
				return true;
		}
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<C, T>> entrySet() {
		return new EntriesView();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<C> keySet() {
		return new KeyView();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Collection<T> values() {
		return new ValuesView();
	}
	
	private class ValuesView implements Collection<T> {

		@Override
		public boolean add(T e) { throw new UnsupportedOperationException(); }

		@Override
		public boolean addAll(Collection<? extends T> c) { throw new UnsupportedOperationException(); }

		@Override
		public void clear() { throw new UnsupportedOperationException(); }

		@Override
		public boolean contains(Object o) {
			return containsValue(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for ( Object o : c )
				if ( ! containsValue(o) )
					return false;
			return true;
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public Iterator<T> iterator() {
			return valuesIterator();
		}

		@Override
		public boolean remove(Object o) { throw new UnsupportedOperationException(); }

		@Override
		public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }

		@Override
		public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }

		@Override
		public int size() { return map.size(); }

		@Override
		public Object[] toArray() {
			Object[] array = new Object[size];
			int j = 0;
			Iterator<T> i = valuesIterator();
			while ( i .hasNext() ){
				array[j] = i.next();
				j++;
			}
			return array;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <E> E[] toArray(E[] a) {
			return (E[]) toArray();
		}
	}
	
	
	private class KeyView implements Set<C> {

		@Override
		public boolean add(C c) { throw new UnsupportedOperationException();}

		@Override
		public boolean addAll(Collection<? extends C> couples) { throw new UnsupportedOperationException(); }

		@Override
		public void clear() {
			AdjacencyMap.this.clear();
		}

		@Override
		public boolean contains(Object obj) {
			return map.containsKey(obj);
		}

		@Override
		public boolean containsAll(Collection<?> objects) {
			for ( Object o : objects )
				if ( ! map.containsKey(o) )
					return false;
			return true;
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public Iterator<C> iterator() {
			return keyIterator();
		}

		@Override
		public boolean remove(Object o) {
			return (AdjacencyMap.this.remove(o) != null);
		}

		@Override
		public boolean removeAll(Collection<?> objects) {
			boolean changed = false;
			for ( Object o : objects )
				changed |= remove(o);
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> objects) {
			boolean changed = false;
			Iterator<C> i = keyIterator();
			while ( i.hasNext() ){
				C c = i.next();
				if ( ! objects.contains(c) ){
					i.remove();
					changed = true;
				}
			}
			return changed;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public Object[] toArray() {
			Object[] array = new Object[size];
			Iterator<C> i = keyIterator();
			int j=0;
			while ( i.hasNext() ){
				array[j] = i.next();
				j++;
			}
			return array;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <E> E[] toArray(E[] arg0) {
			return (E[]) toArray();
		}		
	}
	
	
	private class EntriesView implements Set<Map.Entry<C,T>> {

		@Override
		public boolean add(java.util.Map.Entry<C, T> e) { throw new UnsupportedOperationException(); }
		
		@Override
		public boolean addAll(Collection<? extends java.util.Map.Entry<C, T>> c) { throw new UnsupportedOperationException(); }

		@Override
		public void clear() {
			AdjacencyMap.this.clear();
		}

		@Override
		public boolean contains(Object o) {
			@SuppressWarnings("unchecked")
			Map.Entry<Couple, T> e = (Map.Entry<Couple,T>)o;
			return map.containsKey(e.getKey());
		}

		@Override
		public boolean containsAll(Collection<?> objects) {
			for ( Object o : objects )
				if ( ! contains(o) )
					return false;
			return true;
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public Iterator<java.util.Map.Entry<C, T>> iterator() {
			return entryIterator();
		}

		@Override
		public boolean remove(Object o) {
			@SuppressWarnings("unchecked")
			Map.Entry<C, T> e = (Map.Entry<C, T>)o;
			return (AdjacencyMap.this.remove(e.getKey())!=null);
		}

		@Override
		public boolean removeAll(Collection<?> objects) {
			boolean changed = false;
			for ( Object o : objects )
				changed |= remove(o);
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> objects) {
			boolean changed = false;
			Iterator<C> i = keyIterator();
			while ( i.hasNext() ){
				C c = i.next();
				for ( Object o : objects ){
					if ( o != null ){
						@SuppressWarnings("unchecked")
						Map.Entry<C, T> e = (Map.Entry<C, T>)o;
						if ( e.getKey().equals(c) ){
							i.remove();
							changed = true;
						}
					}
				}
			}
			return changed;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public Object[] toArray() {
			Object[] array = new Object[size];
			Iterator<Map.Entry<C, T>> i = entryIterator();
			int j = 0;
			while ( i.hasNext() ){
				array[j] = i.next();
				j++;
			}
			return array;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <E> E[] toArray(E[] a) {
			return (E[]) toArray();
		}
		
	}


	public final static class Edges<T> extends AdjacencyMap<Edge,T> {
		@Override
		Edge newCouple(Integer id1, Integer id2) {
			return new Edge(id1,id2);
		}
	}


	public final static class Links<T> extends AdjacencyMap<Link,T> {
		@Override
		Link newCouple(Integer id1, Integer id2) {
			return new Link(id1,id2);
		}
		@Override
		protected T put(Integer id1, Integer id2, T obj){
			T prev = super.put(id1, id2, obj);
			super.put(id2, id1, obj);
			return prev;
		}
		@Override
		protected T remove(Integer id1, Integer id2){
			T obj = super.remove(id1,id2);
			super.remove(id2,id1);
			return obj;
		}
		@Override
		public Set<Integer> vertices(){
			return Collections.unmodifiableSet(map.keySet());
		}
		
	}
}
