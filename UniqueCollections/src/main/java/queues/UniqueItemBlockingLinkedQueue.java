package queues;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class UniqueItemBlockingLinkedQueue<E> implements BlockingQueue<E> {
	
	private BlockingQueue<E> internalQueue;
	private Set<Integer> queueHash = new HashSet<>();

	public UniqueItemBlockingLinkedQueue() {
		internalQueue  = new LinkedBlockingQueue<>();
	}

	public UniqueItemBlockingLinkedQueue(int initCapacity) {
		internalQueue  = new LinkedBlockingQueue<>(initCapacity);
	}
	
	public UniqueItemBlockingLinkedQueue(Collection<? extends E> collection){
		internalQueue = new LinkedBlockingQueue<>(collection);
		internalQueue.stream().forEach(e->queueHash.add(e.hashCode()));
	}
	
	
	@Override
	public E remove() {
		E item = internalQueue.remove();
		queueHash.remove(item.hashCode());
		return item;
	}

	@Override
	public E poll() {
		E item = internalQueue.poll();
		if(item != null) {
			queueHash.remove(item.hashCode());
		}
		return item;
	}

	@Override
	public E element() {
		return internalQueue.element();
	}

	@Override
	public E peek() {
		return internalQueue.peek();
	}

	@Override
	public int size() {
		return internalQueue.size();
	}

	@Override
	public boolean isEmpty() {
		return internalQueue.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return new UniqueItemBlackingLinkedQueueIterator();
	}

	@Override
	public Object[] toArray() {
		return internalQueue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return internalQueue.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return internalQueue.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		synchronized (internalQueue) {
			if(internalQueue.addAll(c)) {
				c.stream().forEach(e->queueHash.add(e.hashCode()));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		synchronized (internalQueue) {
			if(internalQueue.removeAll(c)) {
				c.stream().forEach(e->queueHash.remove(e.hashCode()));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if(internalQueue.retainAll(c)) {
			queueHash = new HashSet<>();
			internalQueue.stream().forEach(e->queueHash.add(e.hashCode()));
			return true;
		}
		return false;
	}

	@Override
	public void clear() {
		internalQueue.clear();
		queueHash.clear();		
	}

	@Override
	public boolean add(E e) {
		if(!queueHash.contains(e.hashCode())) {
			synchronized (internalQueue) {
				if(!queueHash.contains(e.hashCode())) {
					if(internalQueue.add(e)) {
						queueHash.add(e.hashCode());
					}else return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean offer(E e) {
		if(internalQueue.offer(e)) {
			queueHash.add(e.hashCode());
			return true;
		}
		return false;
	}

	@Override
	public void put(E e) throws InterruptedException {
		internalQueue.put(e);
		queueHash.add(e.hashCode());
	}

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		if(internalQueue.offer(e, timeout, unit)) {
			queueHash.add(e.hashCode());
			return true;
		}
		return false;
	}

	@Override
	public E take() throws InterruptedException {
		E item = internalQueue.take();
		queueHash.remove(item.hashCode());
		return item;
	}

	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		E item = internalQueue.poll(timeout, unit);
		queueHash.remove(item.hashCode());
		return item;
	}

	@Override
	public int remainingCapacity() {
		return internalQueue.remainingCapacity();
	}

	@Override
	public boolean remove(Object o) {
		if(internalQueue.remove(o)) {
			queueHash.remove(o.hashCode());
			return true;
		}
		return false;
	}

	@Override
	public boolean contains(Object o) {
		return internalQueue.contains(o);
	}

	@Override
	public int drainTo(Collection<? super E> c) {
		RuntimeException error = null;
		int i=0;
		
		try {
			i = internalQueue.drainTo(c);
		}catch(RuntimeException ex) {
			error = ex;
		}
		//In order to maintain integrity, we only remove items there were actually transfered
		//we then re-throw any exceptions to maintain consistent behavior
		c.parallelStream().forEach(e -> queueHash.remove(e.hashCode())); 
		if(error != null) {
			throw error;
		}
		return i;
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		RuntimeException error = null;
		int i=0;
		
		try {
			i = internalQueue.drainTo(c, maxElements);
		}catch(RuntimeException ex) {
			error = ex;
		}
		//In order to maintain integrity, we only remove items there were actually transfered
		//we then re-throw any exceptions to maintain consistent behavior
		c.parallelStream().forEach(e -> queueHash.remove(e.hashCode())); 
		if(error != null) {
			throw error;
		}
		return i;
	}
	
	private class UniqueItemBlackingLinkedQueueIterator implements Iterator<E>{

		private Iterator<E> internalIterator = internalQueue.iterator();
		private Integer currentHash;
		
        @Override
        public boolean hasNext() {
            return internalIterator.hasNext();
        }
 
        @Override
        public E next() {
        	E item = internalIterator.next();
        	currentHash = item.hashCode();
        	return item;
        }
        
        @Override
        public void remove() {
        	internalIterator.remove();
        	queueHash.remove(currentHash);
        	currentHash=null;
        }
        
		
	}

}
