/**
 * 
 */
package queues;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Boaz
 *
 */
@SuppressWarnings("rawtypes")
public class HistoryQueue<E> implements Queue<E>{

	
	private Queue<E> internalQueue = new LinkedList<>();
	private LinkedHashMap<Integer, Object> queueHash;
    // Dummy value to associate with an Object in the backing Map (Same implementation as Java HashSet)
    private static final Object PRESENT = new Object();
	private int historySizeLimit=Integer.MAX_VALUE;
	/**
	 * 
	 */
	public HistoryQueue() {
		queueHash = new LinkedHashMap<>();
	}
	
	public HistoryQueue(Collection<? extends E> collection, int historySize){
		queueHash = new LinkedHashMap<>();
		collection.stream().forEach(P -> add(P));
	}
	
	/**
	 * @param historySize The amount of elements to keep track of in the history. 
	 * This does not limit the size of the queue itself.
	 */
	public HistoryQueue(int historySize) {
		this();
		this.historySizeLimit=historySize;
	}

	@Override
	public int size() {
		return internalQueue.size();
	}

	@Override
	public boolean isEmpty() {
		return internalQueue.isEmpty();
	}

	/* 
	 * Checks if the current queue contains the object. 
	 * Does not check if the history contains the object 
	 */
	@Override
	public boolean contains(Object o) {
		return internalQueue.contains(o);
	}
	
	/**
	 * @param Object o
	 * @return true if the queue history contains this object. Does not check 
	 * if the object is currently in the queue
	 */
	public boolean contained(Object o) {
		return queueHash.containsKey(o.hashCode());
	}

	@Override
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		return internalQueue.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray(Object[] a) {
		return internalQueue.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return internalQueue.remove(o);
	}

	/* 
	 * Checks if the current queue contains all the objects in the collection. 
	 * Does not check if the history contains any of the objects 
	 */

	@Override
	public boolean containsAll(Collection c) {
		return internalQueue.containsAll(c);
	}

	/* 
	 * Checks if the history contains all the objects in the collection. 
	 * Does not check if the current queue contains any of the objects 
	 */
	public boolean containedAll(Collection<E> c) {
		return queueHash.keySet().containsAll(c);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(Collection c) {
		c.stream().forEach(obj -> add(obj));
		return false;
	}

	@Override
	public boolean removeAll(Collection c) {
		return internalQueue.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
		return internalQueue.retainAll(c);
	}

	@Override
	public void clear() {
		internalQueue.clear();		
	}

	
	/**Clear the queue of all elements
	 * 
	 * @param clearHistory if true, clear history as well
	 */
	public void clear(boolean clearHistory) {
		internalQueue.clear();
		if(clearHistory) {
			queueHash.clear();
		}
	}
	
	/**Clears the history of the queue. Current queue remains intact
	 * 
	 */
	public void clearHistory() {
		queueHash.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(Object o) {
		E e = (E) o; //While we're suppressing the warning. We want this cast to fail early if it does
		
		if(queueHash.containsKey(e.hashCode())) {
			return false;
		}
		
		if((queueHash.size()+1) > historySizeLimit) {
			try {
				trimHistory();
			}catch(Exception ex) {
				return false;
			}
		}
		
		queueHash.put(e.hashCode(), PRESENT);
		return internalQueue.add(e);
	}

	@Override
	public boolean offer(Object e) {
		return add(e);
	}

	private void trimHistory() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public E remove() {
		return internalQueue.remove();
	}

	@Override
	public E poll() {
		return internalQueue.poll();
	}

	@Override
	public E element() {
		return internalQueue.element();
	}

	@Override
	public E peek() {
		return internalQueue.peek();
	}

}
