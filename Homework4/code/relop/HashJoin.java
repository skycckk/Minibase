package relop;

/**
 * <p>CS186 - Spring 2007 - Homework 4: Join Operators</p>
 * <p>Implement the hash-based join algorithm described in section 14.4.3 of the
 * textbook (3rd edition; see pages 463 to 464). Use {@link index.HashIndex} 
 * to partition the tuples into buckets during the partitoning phase, 
 * {@link relop.IndexScan} to scan through the tuples resinding in the partitions
 * generated during the first phase, and {@link relop.HashTableDup} to store a 
 * partition in memory during the matching phase of the algorithm. <b>Do not</b>
 * concern yourselves about any memory requirements and handling partition
 * overflows. Assume that every partiton that gets created in the partitioning
 * phase will fit in memory during the second phase.</p>
 * @version 1.0
 */
public class HashJoin extends Iterator
{
	/**
	 * Constructs a hash join, given the left and right iterators and which
	 * columns to match (relative to their individual schemas). Unlike 
	 * {@link relop.SimpleJoin}, <code>HashJoin</code> implements an equijoin
	 * between two attributes, one from <code>left</code>'s relation and the other
	 * from <code>right</code>'s relation.
	 * @param left The {@link relop.Iterator} object corresponding to the left 
	 * input of the join.
	 * @param right The {@link relop.Iterator} object corresponding to the right 
	 * input of the join.
	 * @param lcol The column number of the relation corresponding to the
	 * <code>left</code> input of the operator, on which the join is going to be 
	 * performed.
	 * @param lcol The column number of the relation corresponding to the
	 * <code>right</code> input of the operator, on which the join is going to be 
	 * performed.
	 */
	public HashJoin(Iterator left, Iterator right, Integer lcol, Integer rcol)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Gives a one-line explaination of the iterator, repeats the call on any
	 * child iterators, and increases the indent depth along the way.
	 * @param depth The indentation depth of the output.
	 */
	public void explain(int depth)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Restarts the iterator, i.e. as if it were just constructed.
	 */
	public void restart()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Checks if the iterator is open.
	 * @return <code>true</code> if the iterator is open; <code>false</code> otherwise.
	 */
	public boolean isOpen()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Closes the iterator, releasing any resources (i.e. temporary fires).
	 */
	public void close()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Checks if there are more tuples available. It is a good practice to 
	 * "precompute" the next available tuple in this function and return it
	 * by a call of {@link #getNext()}.
	 * @return <code>true</code> if there are more tuples, <code>false<code> otherwise.
	 */
	public boolean hasNext()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Gets the next tuple in the iteration.
	 * @return The next available {@link relop.Tuple} object of the relation.
	 * @throws IllegalStateException if no more tuples
	 */
	public Tuple getNext()
	{
		throw new UnsupportedOperationException("Not implemented");
	}
}
