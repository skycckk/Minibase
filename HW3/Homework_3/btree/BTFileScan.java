/*
 * @(#) BTIndexPage.java   98/05/14
 * Copyright (c) 1998 UW.  All Rights Reserved.
 *         Author: Xiaohu Li (xioahu@cs.wisc.edu)
 *
 */
package btree;

import exceptions.ScanDeleteException;
import exceptions.ScanIteratorException;
import global.GlobalConst;
import global.PageId;
import global.RID;
import global.Minibase;

import index.IndexFileScan;
import index.Key;
import index.KeyEntry;

import java.io.IOException;

import btree.page.BTLeafPage;

/**
 * BTFileScan implements a search/iterate interface to B+ tree index files
 * (class BTreeFile). It derives from abstract base class IndexFileScan.
 */
public class BTFileScan extends IndexFileScan implements GlobalConst
{

	int maxKeysize;

	/**
	 * Iterate once (during a scan).
	 * 
	 * @return null if done; otherwise next KeyDataEntry
	 * @exception ScanIteratorException
	 *                iterator error
	 */
	public KeyEntry get_next() throws ScanIteratorException
	{
		return(null);
	}

	/**
	 * Delete currently-being-scanned(i.e., just scanned) data entry.
	 * 
	 * @exception ScanDeleteException
	 *                delete error when scan
	 */
	public void delete_current() throws ScanDeleteException
	{
	}

	/**
	 * max size of the key
	 * 
	 * @return the maxumum size of the key in BTFile
	 */
	public int keysize()
	{
		return maxKeysize;
	}

	/**
	 * destructor. unpin some pages if they are not unpinned already. and do
	 * some clearing work.
	 * 
	 * @exception IOException
	 *                error from the lower layer
	 * @exception exceptions.InvalidFrameNumberException
	 *                error from the lower layer
	 * @exception exceptions.ReplacerException
	 *                error from the lower layer
	 * @exception exceptions.PageUnpinnedException
	 *                error from the lower layer
	 * @exception exceptions.HashEntryNotFoundException
	 *                error from the lower layer
	 */
	public void destroyBTreeFileScan() throws IOException,
			exceptions.InvalidFrameNumberException, exceptions.ReplacerException,
			exceptions.PageUnpinnedException, exceptions.HashEntryNotFoundException
	{
	}

}
