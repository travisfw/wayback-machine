package org.archive.wayback.util.partition;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A class which holds elements of some type for a particular Date range.
 * 
 * This class also has two additional application-usable fields:

 *   containsClosest: boolean - tracks whether this Partition holds the 
 *   	"closest" element of interest to an application
 *   
 *   total: int - independent counter for total internal application-level
 *   	elements, useful when nesting partitions, to track the sum-of-totals
 *		of interior partitions
 * 
 * @author brad
 *
 * @param <T> Generic type which this partition holds.
 */
public class Partition<T> {

	private Date start = null;
	private Date end = null;
	private List<T> list = null;
	private boolean containsClosest = false;
	private int total = 0;

	/**
	 * Create a Partition for holding elements between the two argument Date 
	 * objects.
	 * @param start Date representing the start of elements held in this 
	 * Partition, inclusive.  
	 * @param end Date representing the end of elements held in this Partition,
	 * exclusive.
	 */
	public Partition(Date start, Date end) {
		this.start = start;
		this.end = new Date(end.getTime()-1);
		list = new ArrayList<T>();
		total = 0;
	}

	/**
	 * Checks if a date is within this partition
	 * @param d Date to check
	 * @return boolean true if d is >= start, and < end
	 */
	public boolean containsDate(Date d) {
		return (start.compareTo(d) <= 0) &&
				(end.compareTo(d) > 0); 
	}

	/**
	 * @return the start Date for this Partition.
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * @return the end Date for this Partition.
	 */
	public Date getEnd() {
		return end;
	}

	/**
	 * @return number of elements held in this Partition.
	 */
	public int count() {
		return list.size();
	}

	/**
	 * @param o element to be added to this partition.
	 */
	public void add(T o) {
		list.add(o);
	}

	/**
	 * @return an Iterator of elements held in this Partition.
	 */
	public Iterator<T> iterator() {
		return list.iterator();
	}

	/**
	 * @return a List of the elements held in this Partition.
	 */
	public List<T> list() {
		return list;
	}

	/**
	 * @return the containsClosest
	 */
	public boolean isContainsClosest() {
		return containsClosest;
	}

	/**
	 * @param containsClosest the containsClosest to set
	 */
	public void setContainsClosest(boolean containsClosest) {
		this.containsClosest = containsClosest;
	}

	/**
	 * Add and int to the Total count for this partition.
	 * @param numberToAdd number to add
	 */
	public void addTotal(int numberToAdd) {
		total += numberToAdd;
	}
	/**
	 * @return the Total count for this partition.
	 */
	public int getTotal() {
		return total;
	}
}
