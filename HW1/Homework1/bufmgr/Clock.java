package bufmgr;

import exceptions.BufferPoolExceededException;
import exceptions.InvalidFrameNumberException;
import exceptions.PagePinnedException;
import exceptions.PageUnpinnedException;


/**
 * This class should implement a Clock replacement strategy.
 */
public class Clock extends BufMgrReplacer
{
	private int poolSize;
	private int clockHand;

	public Clock()
	{
		System.out.println("default ctor");
		
	};
	
	public Clock(BufMgr b)
	{
		System.out.println("clock ctor");
		System.out.println(b);
		setBufferManager(b);

		poolSize = mgr.getNumBuffers();
		clockHand = 0;
	};

	/**
	 * Pins a candidate page in the buffer pool.
	 * 
	 * @param frameNo
	 *            frame number of the page.
	 * @throws InvalidFrameNumberException
	 *             if the frame number is less than zero or bigger than number
	 *             of buffers.
	 * @return true if successful.
	 */
	public void pin(int frameNo) throws InvalidFrameNumberException
	{
		System.out.println("clock pin called");
		state_bit[frameNo] = Pinned;
	};

	/**
	 * Unpins a page in the buffer pool.
	 * 
	 * @param frameNo
	 *            frame number of the page.
	 * @throws InvalidFrameNumberException
	 *             if the frame number is less than zero or bigger than number
	 *             of buffers.
	 * @throws PageUnpinnedException
	 *             if the page is originally unpinned.
	 * @return true if successful.
	 */
	public boolean unpin(int frameNo) throws InvalidFrameNumberException,
			PageUnpinnedException
	{
		if (frameNo < 0 || frameNo > poolSize)
			throw new InvalidFrameNumberException(null, "ERROR: invalid frame no.");

		state_bit[frameNo] = Referenced;
		return true;
	};

	/**
	 * Frees and unpins a page in the buffer pool.
	 * 
	 * @param frameNo
	 *            frame number of the page.
	 * @throws PagePinnedException
	 *             if the page is pinned.
	 */
	public void free(int frameNo) throws PagePinnedException
	{
		state_bit[frameNo] = Available;
	};

	/** Must pin the returned frame. */
	public int pick_victim() throws BufferPoolExceededException, PagePinnedException
	{
		System.out.println("Clock : Picking victim");


		while ( checkClock() == -1 )
		{
			System.out.println("checking clock");
		}

		return clockHand;
	} ;

	private	int checkClock()
	{

		// if frame buffer is available to replace, return it.
		if (state_bit[clockHand] == Available)
		{
			return clockHand;
		}

		// if reference bit is set, clear the bit, and advance the clock hand.
		if (state_bit[clockHand] == Referenced)
		{
			state_bit[clockHand] = Available;
			clockHand = (clockHand + 1) % poolSize;
			return -1;
		}
		else // pinned. advanced clock hand
		{
			clockHand = (clockHand + 1) % poolSize;
			return -1;
		}

	};


	/** Retruns the name of the replacer algorithm. */
	public String name()
	{ return "Clock"; };

	/**
	 * Counts the unpinned frames (free frames) in the buffer pool.
	 * 
	 * @returns the total number of unpinned frames in the buffer pool.
	 */
	public int getNumUnpinnedBuffers()
	{
		System.out.println(mgr);
		return mgr.getNumUnpinnedBuffers();
	}


}
