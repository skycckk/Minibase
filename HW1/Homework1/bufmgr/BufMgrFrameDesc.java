package bufmgr;

import global.AbstractBufMgrFrameDesc;
import global.GlobalConst;
import global.PageId;

public class BufMgrFrameDesc extends global.AbstractBufMgrFrameDesc implements GlobalConst
{
	private int pinCount = 0;
	private boolean dirty = false;
	private PageId pid = null;
	
	public BufMgrFrameDesc(PageId pid) {
		this.pid = pid;
	}
	/**
	 * Returns the pin count of a certain frame page.
	 * 
	 * @return the pin count number.
	 */
	public int getPinCount()
	{ return pinCount; };

	/**
	 * Increments the pin count of a certain frame page when the page is pinned.
	 * 
	 * @return the incremented pin count.
	 */
	public int pin()
	{ return ++pinCount; };

	/**
	 * Decrements the pin count of a frame when the page is unpinned. If the pin
	 * count is equal to or less than zero, the pin count will be zero.
	 * 
	 * @return the decremented pin count.
	 */
	public int unpin()
	{ return Math.max(--pinCount, 0); };

	/**
	 * 
	 */
	public PageId getPageNo()
	{ return pid; };

	/**
	 * the dirty bit, 1 (TRUE) stands for this frame is altered, 0 (FALSE) for
	 * clean frames.
	 */
	public boolean isDirty()
	{ return dirty; };
}
