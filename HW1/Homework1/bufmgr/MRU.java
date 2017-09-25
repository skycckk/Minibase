package bufmgr;

import global.GlobalConst;
import global.AbstractBufMgr;
import global.AbstractBufMgrFrameDesc;
import global.AbstractBufMgrReplacer;

import java.util.ArrayList;

import exceptions.BufferPoolExceededException;
import exceptions.InvalidFrameNumberException;
import exceptions.PagePinnedException;
import exceptions.PageUnpinnedException;


/**
 * This class should implement a Clock replacement strategy.
 */
public class MRU extends BufMgrReplacer
{
	int totalFrames = 0;
	// victimList indicates as a stack for frame eviction and it only contains Referenced page
	// emptyList is a list of frame No. indicating which frame is not used
	ArrayList<Integer> victimList = new ArrayList<>(); 
	ArrayList<Integer> emptyList = new ArrayList<>(); 
	public MRU() 
	{
		initialize();
	};
	
	public MRU(AbstractBufMgr b) 
	{
		setBufferManager((BufMgr)b);
		initialize();
	};
	
	private void initialize() {
		totalFrames = mgr.getNumBuffers();
		for (int i = 0; i < totalFrames; i++) emptyList.add(i);
	}
	
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
		if (frameNo < 0 || frameNo > totalFrames) throw new InvalidFrameNumberException(null, "ERROR: invalid frame no.");
		
		if (state_bit[frameNo] == Referenced) {
			// remove the current frameNo and later unpin will add it back to the head
			// if there is no match, no need to remove
			for (int i = 0; i < victimList.size(); i++ ) {
				if (victimList.get(i) == frameNo) victimList.remove(i);
			}
		}
		
		state_bit[frameNo] = Pinned;
	}

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
		if (frameNo < 0 || frameNo > totalFrames) return false;
		if (state_bit[frameNo] == Pinned) {
			// unpin must be happened after pinned
			victimList.add(0, frameNo);
			state_bit[frameNo] = Referenced;
		}
		return true; 
	}

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
		if (frameNo < 0 || frameNo > totalFrames) throw new PagePinnedException(null, "ERROR: invalid frame no.");
		
		if (state_bit[frameNo] == Referenced) {
			for (int i = 0; i < victimList.size(); i++) {
				if (victimList.get(i) == frameNo) victimList.remove(i);
			}
		}
		emptyList.add(0, frameNo);
	};

	/** Must pin the returned frame. */
	public int pick_victim() throws BufferPoolExceededException,
			PagePinnedException
	{
		int victimNo = -1;
		if (emptyList.size() > 0) { victimNo = emptyList.get(0); emptyList.remove(0); } 
		else if (victimList.size() > 0) { victimNo = victimList.get(0); victimList.remove(0); }
		else throw new BufferPoolExceededException(null, "ERROR: not enough unpinned buffers. Waiting for unpin");
		return victimNo;
	}

	/** Retruns the name of the replacer algorithm. */
	public String name()
	{ return "MRU"; };

	/**
	 * Counts the unpinned frames (free frames) in the buffer pool.
	 * 
	 * @returns the total number of unpinned frames in the buffer pool.
	 */
	public int getNumUnpinnedBuffers()
	{
		return emptyList.size() + victimList.size();
	}
}
