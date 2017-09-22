/*  File BufMgr,java */

package bufmgr;


import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import global.AbstractBufMgr;
import global.AbstractBufMgrFrameDesc;
import global.PageId;
import global.SystemDefs;
import diskmgr.Page;

import exceptions.BufMgrException;
import exceptions.BufferPoolExceededException;
import exceptions.DiskMgrException;
import exceptions.FileIOException;
import exceptions.HashEntryNotFoundException;
import exceptions.HashOperationException;
import exceptions.InvalidBufferException;
import exceptions.InvalidFrameNumberException;
import exceptions.InvalidPageNumberException;
import exceptions.InvalidReplacerException;
import exceptions.InvalidRunSizeException;
import exceptions.OutOfSpaceException;
import exceptions.PageNotFoundException;
import exceptions.PageNotReadException;
import exceptions.PagePinnedException;
import exceptions.PageUnpinnedException;
import exceptions.ReplacerException;

// *****************************************************

/**
 * This is a dummy buffer manager class. You will need to replace it
 * with a buffer manager that reads from and writes to disk
 *
 * algorithm to replace the page.
 */
public class BufMgr extends AbstractBufMgr
{
	// Replacement policies to be implemented
	public static final String Clock = "Clock";
	public static final String LRU = "LRU";
	public static final String MRU = "MRU";
	
	// Total number of buffer frames in the buffer pool. */
	private int numBuffers;

	// This buffer manager keeps all pages in memory! 
	private Hashtable pageIdToPageData = new Hashtable();

	// An array of Descriptors one per frame. 
	private BufMgrFrameDesc[] frameTable = new BufMgrFrameDesc[NUMBUF];
	
	// A map keeps the relation b/w page id and frame desc
	private Map<PageId, BufMgrFrameDesc> pageIdToFrameDesc = new HashMap<>();


	/**
	 * Create a buffer manager object.
	 * 
	 * @param numbufs
	 *            number of buffers in the buffer pool.
	 * @param replacerArg
	 *            name of the buffer replacement policy (e.g. BufMgr.Clock).
	 * @throws InvalidReplacerException 
	 */
	public BufMgr(int numbufs, String replacerArg) throws InvalidReplacerException
	{
		numBuffers = numbufs;
		frameTable = new BufMgrFrameDesc[numBuffers];
		setReplacer(replacerArg);
	}

	/**
     * Default Constructor
	 * Create a buffer manager object.
	 * @throws InvalidReplacerException 
	 */
	public BufMgr() throws InvalidReplacerException
	{
		System.out.println("constructor for buffer manager. init replacer");
		numBuffers = 1;
		frameTable = new BufMgrFrameDesc[numBuffers];
		replacer = new Clock(this);
	}

	/**
	 * Check if this page is in buffer pool, otherwise find a frame for this
	 * page, read in and pin it. Also write out the old page if it's dirty
	 * before reading. If emptyPage==TRUE, then actually no read is done to bring
	 * the page in.
	 * 
	 * @param pin_pgid
	 *            page number in the minibase.
	 * @param page
	 *            the pointer poit to the page.
	 * @param emptyPage
	 *            true (empty page); false (non-empty page)
	 * 
	 * @exception ReplacerException
	 *                if there is a replacer error.
	 * @exception HashOperationException
	 *                if there is a hashtable error.
	 * @exception PageUnpinnedException
	 *                if there is a page that is already unpinned.
	 * @exception InvalidFrameNumberException
	 *                if there is an invalid frame number .
	 * @exception PageNotReadException
	 *                if a page cannot be read.
	 * @exception BufferPoolExceededException
	 *                if the buffer pool is full.
	 * @exception PagePinnedException
	 *                if a page is left pinned .
	 * @exception BufMgrException
	 *                other error occured in bufmgr layer
	 * @exception IOException
	 *                if there is other kinds of I/O error.
	 */

	public void pinPage(PageId pin_pgid, Page page, boolean emptyPage)
			throws ReplacerException, HashOperationException,
			PageUnpinnedException, InvalidFrameNumberException,
			PageNotReadException, BufferPoolExceededException,
			PagePinnedException, BufMgrException, IOException
	{
		// This buffer manager just keeps allocating new pages and puts them
		// the hash table. It regards each page it is passed as a parameter
		// (<code>page</code> variable) as empty, and thus doesn't take into
		// account any data stored in it.
		//
		// Extend this method to operate as it is supposed to (see the javadoc
		// description above).
		
		// if frame is already in the buffer pool: just read it to the page
		if (pageIdToPageData.get(pin_pgid) != null) {
			// read it from pool
			frameTable[pin_pgid.getPid()].pin();
			replacer.pin(pageIdToFrameDesc.get(pin_pgid).getFrameNo());
			page.setpage((byte[])(pageIdToPageData.get(pin_pgid)));
		} else {
			// Find a victim page to replace it with the current one.
			int frameNo = replacer.pick_victim(); 
			if (frameNo < 0) {
				page = null;
				throw new ReplacerException(null, "BUFMGR: REPLACER_ERROR.");
			}
			// Find a victim page to replace it with the current one.
			BufMgrFrameDesc victimFrame = frameTable[frameNo];
			if (victimFrame != null && victimFrame.isDirty()) {
				// flush to disk
				try {
					flushPage(victimFrame.getPageNo());
				} catch (PageNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (victimFrame != null) {
				pageIdToPageData.remove(victimFrame.getPageNo());
				pageIdToFrameDesc.remove(victimFrame.getPageNo());
			}
			
			frameTable[frameNo] = new BufMgrFrameDesc(pin_pgid, frameNo);
			BufMgrFrameDesc newFrame = frameTable[frameNo];
			newFrame.pin();
			replacer.pin(newFrame.getFrameNo());
			page.setpage((byte[])(pageIdToPageData.get(pin_pgid)));
			
			// The following code excerpt reads the contents of the page with id pin_pgid
			// into the object page. Use it to read the contents of a dirty page to be
			// written back to disk.
			try {
				SystemDefs.JavabaseDB.read_page(pin_pgid, page);
			} catch (Exception e) {
				throw new PageNotReadException(e,"BUFMGR: DB_READ_PAGE_ERROR");
			} 
			pageIdToPageData.put(new PageId(pin_pgid.getPid()), page.getpage());
			pageIdToFrameDesc.put(new PageId(pin_pgid.getPid()), newFrame);
		}
		
		// Hint: Notice that this naive Buffer Manager allocates a page, but does not
		// associate it with a page frame descriptor (an entry of the frameTable
		// object). Your Buffer Manager shouldn't be that naive ;) . Have in mind that
		// the hashtable is simply the "storage" area of your pages, while the frameTable
		// contains the frame descriptors, each associated with one loaded page, which
		// stores that page's metadata (pin count, status, etc.)
	}

	/**
	 * To unpin a page specified by a pageId. If pincount>0, decrement it and if
	 * it becomes zero, put it in a group of replacement candidates. if
	 * pincount=0 before this call, return error.
	 * 
	 * @param globalPageId_in_a_DB
	 *            page number in the minibase.
	 * @param dirty
	 *            the dirty bit of the frame
	 * 
	 * @exception ReplacerException
	 *                if there is a replacer error.
	 * @exception PageUnpinnedException
	 *                if there is a page that is already unpinned.
	 * @exception InvalidFrameNumberException
	 *                if there is an invalid frame number .
	 * @exception HashEntryNotFoundException
	 *                if there is no entry of page in the hash table.
	 */
	public void unpinPage(PageId PageId_in_a_DB, boolean dirty)
			throws ReplacerException, PageUnpinnedException,
			HashEntryNotFoundException, InvalidFrameNumberException
	{
		BufMgrFrameDesc frame = pageIdToFrameDesc.get(PageId_in_a_DB);
		if (frame == null) throw new PageUnpinnedException(null, "ERROR: NULL frame");
		if (frame.getPinCount() > 0) {
			frame.unpin();
			if (frame.getPinCount() == 0) replacer.unpin(frame.getFrameNo());
		} else {
			throw new PageUnpinnedException(null, "ERROR: pinCount is zero at unpinPage");
		}
		frame.setDirty(dirty);
	}

	/**
	 * Call DB object to allocate a run of new pages and find a frame in the
	 * buffer pool for the first page and pin it. If buffer is full, ask DB to
	 * deallocate all these pages and return error (null if error).
	 * 
	 * @param firstpage
	 *            the address of the first page.
	 * @param howmany
	 *            total number of allocated new pages.
	 * @return the first page id of the new pages.
	 * 
	 * @exception BufferPoolExceededException
	 *                if the buffer pool is full.
	 * @exception HashOperationException
	 *                if there is a hashtable error.
	 * @exception ReplacerException
	 *                if there is a replacer error.
	 * @exception HashEntryNotFoundException
	 *                if there is no entry of page in the hash table.
	 * @exception InvalidFrameNumberException
	 *                if there is an invalid frame number.
	 * @exception PageUnpinnedException
	 *                if there is a page that is already unpinned.
	 * @exception PagePinnedException
	 *                if a page is left pinned.
	 * @exception PageNotReadException
	 *                if a page cannot be read.
	 * @exception IOException
	 *                if there is other kinds of I/O error.
	 * @exception BufMgrException
	 *                other error occured in bufmgr layer
	 * @exception DiskMgrException
	 *                other error occured in diskmgr layer
	 */
	public PageId newPage(Page firstpage, int howmany)
			throws BufferPoolExceededException, HashOperationException,
			ReplacerException, HashEntryNotFoundException,
			InvalidFrameNumberException, PagePinnedException,
			PageUnpinnedException, PageNotReadException, BufMgrException,
			DiskMgrException, IOException
	{
		PageId newPid = new PageId();
		try {
			SystemDefs.JavabaseDB.allocate_page(newPid, howmany); // this will get the page id to the start
		} catch (OutOfSpaceException | InvalidRunSizeException | InvalidPageNumberException | FileIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			pinPage(newPid, firstpage, true);
		} catch (Exception e) {
			try {
				SystemDefs.JavabaseDB.deallocate_page(newPid, howmany);
			} catch (InvalidRunSizeException | InvalidPageNumberException | FileIOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
		
		return newPid;
	}

	/**
	 * User should call this method if s/he needs to delete a page. this routine
	 * will call DB to deallocate the page.
	 * 
	 * @param globalPageId
	 *            the page number in the data base.
	 * @exception InvalidBufferException
	 *                if buffer pool corrupted.
	 * @exception ReplacerException
	 *                if there is a replacer error.
	 * @exception HashOperationException
	 *                if there is a hash table error.
	 * @exception InvalidFrameNumberException
	 *                if there is an invalid frame number.
	 * @exception PageNotReadException
	 *                if a page cannot be read.
	 * @exception BufferPoolExceededException
	 *                if the buffer pool is already full.
	 * @exception PagePinnedException
	 *                if a page is left pinned.
	 * @exception PageUnpinnedException
	 *                if there is a page that is already unpinned.
	 * @exception HashEntryNotFoundException
	 *                if there is no entry of page in the hash table.
	 * @exception IOException
	 *                if there is other kinds of I/O error.
	 * @exception BufMgrException
	 *                other error occured in bufmgr layer
	 * @exception DiskMgrException
	 *                other error occured in diskmgr layer
	 */
	public void freePage(PageId globalPageId) throws InvalidBufferException,
			ReplacerException, HashOperationException,
			InvalidFrameNumberException, PageNotReadException,
			BufferPoolExceededException, PagePinnedException,
			PageUnpinnedException, HashEntryNotFoundException, BufMgrException,
			DiskMgrException, IOException
	{
		BufMgrFrameDesc frame = pageIdToFrameDesc.get(globalPageId);
		if (frame == null) throw new PageUnpinnedException(null, "ERROR: NULL frame");
		if (frame.getPinCount() == 1) {
			frame.unpin();
			replacer.unpin(frame.getFrameNo());
		} else if (frame.getPinCount() != 0) {
			throw new PagePinnedException(null, "ERROR: pin count > 1 when free");
		}
		
		// it's ok to free
		frameTable[frame.getFrameNo()] = null;
		pageIdToPageData.remove(globalPageId);
		pageIdToFrameDesc.remove(globalPageId);
		replacer.free(frame.getFrameNo());
		try {
			SystemDefs.JavabaseDB.deallocate_page(globalPageId);
		} catch (InvalidRunSizeException | InvalidPageNumberException | FileIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Added to flush a particular page of the buffer pool to disk
	 * 
	 * @param pageid
	 *            the page number in the database.
	 * 
	 * @exception HashOperationException
	 *                if there is a hashtable error.
	 * @exception PageUnpinnedException
	 *                if there is a page that is already unpinned.
	 * @exception PagePinnedException
	 *                if a page is left pinned.
	 * @exception PageNotFoundException
	 *                if a page is not found.
	 * @exception BufMgrException
	 *                other error occured in bufmgr layer
	 * @exception IOException
	 *                if there is other kinds of I/O error.
	 */
	public void flushPage(PageId pageid) throws HashOperationException,
			PageUnpinnedException, PagePinnedException, PageNotFoundException,
			BufMgrException, IOException
	{
		BufMgrFrameDesc frame = pageIdToFrameDesc.get(pageid);
		if (frame == null) throw new PageUnpinnedException(null, "ERROR: NULL frame");
		if (frame.getPinCount() > 0) throw new PageUnpinnedException(null, "ERROR: still pinned");
		if (frame.isDirty()) {
			Page aPage = new Page((byte[])this.pageIdToPageData.get(pageid));
			try {
				SystemDefs.JavabaseDB.write_page(pageid, aPage);
			} catch (InvalidPageNumberException | FileIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			frame.setDirty(false);
		}
	}

	/**
	 * Flushes all pages of the buffer pool to disk
	 * 
	 * @exception HashOperationException
	 *                if there is a hashtable error.
	 * @exception PageUnpinnedException
	 *                if there is a page that is already unpinned.
	 * @exception PagePinnedException
	 *                if a page is left pinned.
	 * @ exception PageNotFoundException
	 *                if a page is not found.
	 * @exception BufMgrException
	 *                other error occured in bufmgr layer
	 * @exception IOException
	 *                if there is other kinds of I/O error.
	 */
	public void flushAllPages() throws HashOperationException,
			PageUnpinnedException, PagePinnedException, PageNotFoundException,
			BufMgrException, IOException
	{
		System.out.println("flush to disk?");
		Iterator i = this.pageIdToPageData.keySet().iterator();

		while(i.hasNext()) {
			PageId id = (PageId)i.next();
			this.flushPage(id);
		}

	}

	/**
	 * Gets the total number of buffers.
	 * 
	 * @return total number of buffer frames.
	 */
	public int getNumBuffers()
	{
		return numBuffers;
	}

	/**
	 * Gets the total number of unpinned buffer frames.
	 * 
	 * @return total number of unpinned buffer frames.
	 */
	public int getNumUnpinnedBuffers()
	{
		int count = 0;
		for (BufMgrFrameDesc frame : frameTable) {
			if (frame != null && frame.getPinCount() == 0) count++;
		}
		return count;
	}

	/** A few routines currently need direct access to the FrameTable. */
	public AbstractBufMgrFrameDesc[] getFrameTable()
	{
		return this.frameTable;
	}

}

