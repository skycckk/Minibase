package relop;

/**
 * <p>CS186 - Spring 2007 - Homework 4: Join Operators</p>
 * <p>Implement the simplest of all join algorithms: nested loops (see textbook, 3rd edition,
 * section 14.4.1, page 454).</p>
 * @version 1.0
 */
public class SimpleJoin extends Iterator
{
	/**
	 * Constructs a join, given the left and right iterators and join predicates
	 * (relative to the combined schema).
	 * @param left The {@link relop.Iterator} object corresponding to the left 
	 * input of the join.
	 * @param right The {@link relop.Iterator} object corresponding to the right 
	 * input of the join.
	 * @param preds An array of {@link relop.Predicate} objects, corresponding to
	 * the predicates on which the join is performed. If for example you are joining
	 * relations R and S under the condition (R.attr1 > S.attr2 && R.attr3 = S.attr4),
	 * you have to pass as an argument an array of two {@link relop.Predicate} objects,
	 * instantiated according to the above conditions. 
	 */
	public SimpleJoin(Iterator left, Iterator right, Predicate[] preds)
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
	public Tuple getNext() throws IllegalStateException
	{
		throw new UnsupportedOperationException("Not implemented");
	}
}
