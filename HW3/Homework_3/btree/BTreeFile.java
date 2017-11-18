/*
 * @(#) bt.java   98/03/24
 * Copyright (c) 1998 UW.  All Rights Reserved.
 *         Author: Xiaohu Li (xioahu@cs.wisc.edu).
 *
 */

package btree;

import diskmgr.Page;
import exceptions.AddFileEntryException;
import exceptions.BufMgrException;
import exceptions.BufferPoolExceededException;
import exceptions.ConstructPageException;
import exceptions.ConvertException;
import exceptions.DeleteFashionException;
import exceptions.DeleteFileEntryException;
import exceptions.DeleteRecException;
import exceptions.DiskMgrException;
import exceptions.DuplicateEntryException;
import exceptions.FileEntryNotFoundException;
import exceptions.FileIOException;
import exceptions.FileNameTooLongException;
import exceptions.FreePageException;
import exceptions.GetFileEntryException;
import exceptions.HashEntryNotFoundException;
import exceptions.HashOperationException;
import exceptions.IndexFullDeleteException;
import exceptions.IndexInsertRecException;
import exceptions.IndexSearchException;
import exceptions.InsertException;
import exceptions.InsertRecException;
import exceptions.InvalidBufferException;
import exceptions.InvalidFrameNumberException;
import exceptions.InvalidPageNumberException;
import exceptions.InvalidRunSizeException;
import exceptions.IteratorException;
import exceptions.KeyNotMatchException;
import exceptions.KeyTooLongException;
import exceptions.LeafDeleteException;
import exceptions.LeafInsertRecException;
import exceptions.LeafRedistributeException;
import exceptions.NodeNotMatchException;
import exceptions.OutOfSpaceException;
import exceptions.PageNotReadException;
import exceptions.PagePinnedException;
import exceptions.PageUnpinnedException;
import exceptions.PinPageException;
import exceptions.RecordNotFoundException;
import exceptions.RedistributeException;
import exceptions.ReplacerException;
import exceptions.UnpinPageException;
import global.AttrType;
import global.GlobalConst;
import global.Minibase;
import global.PageId;
import global.RID;
import heap.HFPage;
import index.IndexFile;
import index.Key;
import index.KeyEntry;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import btree.page.BTIndexPage;
import btree.page.BTLeafPage;
import btree.page.BTSortedPage;
import btree.page.BTHeaderPage;
 
/**
 * btfile.java This is the main definition of class BTreeFile, which derives
 * from abstract base class IndexFile. It provides an insert/delete interface.
 */
public class BTreeFile extends IndexFile implements GlobalConst
{
	public static final int NAIVE_DELETE = 0;

	public static final int FULL_DELETE = 1;

	private final static int MAGIC0 = 1989;

	private BTHeaderPage header = null;
	private String db_filename;

	/**
	 * Access method to data member.
	 * 
	 * @return Return a BTreeHeaderPage object that is the header page of this
	 *         btree file.
	 */
	public BTHeaderPage getHeaderPage()
	{
		return header;
	}


	/**
	 * BTreeFile class an index file with given filename should already exist;
	 * this opens it.
	 * 
	 * @param filename
	 *            the B+ tree file name. Input parameter.
	 * @exception GetFileEntryException
	 *                can not ger the file from DB
	 * @exception PinPageException
	 *                failed when pin a page
	 * @exception ConstructPageException
	 *                BT page constructor failed
	 * @throws IOException 
	 * @throws DiskMgrException 
	 * @throws InvalidPageNumberException 
	 * @throws FileIOException 
	 */
	public BTreeFile(String filename) throws GetFileEntryException,
			PinPageException, ConstructPageException, FileIOException, InvalidPageNumberException, DiskMgrException, IOException
	{
		PageId page = Minibase.JavabaseDB.get_file_entry(filename);
		if (page == null) {
			header = new BTHeaderPage();
		} else {
			header = new BTHeaderPage(page);
		}
		db_filename = new String(filename);
	}

	/**
	 * if index file exists, open it; else create it.
	 * 
	 * @param filename
	 *            file name. Input parameter.
	 * @param keytype
	 *            the type of key. Input parameter.
	 * @param keysize
	 *            the maximum size of a key. Input parameter.
	 * @param delete_fashion
	 *            full delete or naive delete. Input parameter. It is either
	 *            DeleteFashion.NAIVE_DELETE or DeleteFashion.FULL_DELETE.
	 * @exception GetFileEntryException
	 *                can not get file
	 * @exception ConstructPageException
	 *                page constructor failed
	 * @exception IOException
	 *                error from lower layer
	 * @exception AddFileEntryException
	 *                can not add file into DB
	 * @throws DiskMgrException 
	 * @throws InvalidPageNumberException 
	 * @throws FileIOException 
	 * @throws PinPageException 
	 * @throws OutOfSpaceException 
	 * @throws DuplicateEntryException 
	 * @throws InvalidRunSizeException 
	 * @throws FileNameTooLongException 
	 */
	public BTreeFile(String filename, int keytype, int keysize,
			int delete_fashion) throws GetFileEntryException,
			ConstructPageException, IOException, AddFileEntryException, FileIOException, InvalidPageNumberException, DiskMgrException, PinPageException, FileNameTooLongException, InvalidRunSizeException, DuplicateEntryException, OutOfSpaceException
	{
		this(filename);
		
		if (header != null) {
			header.set_keyType((short) keytype);
			header.set_maxKeySize(keysize);
			header.set_deleteFashion(delete_fashion);
			header.set_magic0(MAGIC0);
			header.set_rootId(new PageId(INVALID_PAGE));
			Minibase.JavabaseDB.add_file_entry(filename, header.getPageId());
		}
	}

	/**
	 * Close the B+ tree file. Unpin header page.
	 * 
	 * @exception PageUnpinnedException
	 *                error from the lower layer
	 * @exception InvalidFrameNumberException
	 *                error from the lower layer
	 * @exception HashEntryNotFoundException
	 *                error from the lower layer
	 * @exception ReplacerException
	 *                error from the lower layer
	 * @throws IOException 
	 */
	public void close() throws PageUnpinnedException,
			InvalidFrameNumberException, HashEntryNotFoundException,
			ReplacerException, IOException
	{
		if (header != null) {
			Minibase.JavabaseBM.unpinPage(header.getPageId(), false/* not dirty */);
			header = null;
		}
	}

	/**
	 * Destroy entire B+ tree file.
	 * 
	 * @exception IOException
	 *                error from the lower layer
	 * @exception IteratorException
	 *                iterator error
	 * @exception UnpinPageException
	 *                error when unpin a page
	 * @exception FreePageException
	 *                error when free a page
	 * @exception DeleteFileEntryException
	 *                failed when delete a file from DM
	 * @exception ConstructPageException
	 *                error in BT page constructor
	 * @exception PinPageException
	 *                failed when pin a page
	 * @throws InvalidFrameNumberException 
	 * @throws HashEntryNotFoundException 
	 * @throws PageUnpinnedException 
	 * @throws ReplacerException 
	 * @throws DiskMgrException 
	 * @throws BufMgrException 
	 * @throws PagePinnedException 
	 * @throws BufferPoolExceededException 
	 * @throws PageNotReadException 
	 * @throws HashOperationException 
	 * @throws InvalidBufferException 
	 * @throws FileIOException 
	 * @throws InvalidPageNumberException 
	 * @throws InvalidRunSizeException 
	 * @throws FileEntryNotFoundException 
	 */
	public void destroyFile() throws IOException, IteratorException,
			UnpinPageException, FreePageException, DeleteFileEntryException,
			ConstructPageException, PinPageException, ReplacerException, PageUnpinnedException, HashEntryNotFoundException, InvalidFrameNumberException, InvalidBufferException, HashOperationException, PageNotReadException, BufferPoolExceededException, PagePinnedException, BufMgrException, DiskMgrException, InvalidRunSizeException, InvalidPageNumberException, FileIOException, FileEntryNotFoundException
	{
		if (header == null) return;
		
		if (header.get_rootId().getPid() != INVALID_PAGE) {
			// destroy all pages by traversing all the nodes
			destroyFileHelper(header.get_rootId(), header.get_keyType());
		}
		Minibase.JavabaseBM.unpinPage(header.getPageId(), false);
		Minibase.JavabaseBM.freePage(header.getPageId());
		Minibase.JavabaseDB.delete_file_entry(db_filename);
		header = null;
	}
	
	private void destroyFileHelper(PageId currPageId, short keyType) throws ConstructPageException, IOException, IteratorException, 
			ReplacerException, PageUnpinnedException, HashEntryNotFoundException, 
			InvalidFrameNumberException, InvalidBufferException, HashOperationException, 
			PageNotReadException, BufferPoolExceededException, PagePinnedException, 
			BufMgrException, DiskMgrException {
		BTSortedPage sortedPage = new BTSortedPage(currPageId, keyType);
		
		if (sortedPage.getType() == BTSortedPage.INDEX) {
			BTIndexPage indexPage = new BTIndexPage((Page)sortedPage, keyType);
			
			// left-most child
			destroyFileHelper(sortedPage.getPrevPage(), keyType);

			RID rid = new RID();
			for (KeyEntry entry = indexPage.getFirst(rid); 
				 entry != null;
				 entry = indexPage.getNext(rid)) {
				destroyFileHelper((PageId)entry.getData(), keyType);
			}
			Minibase.JavabaseBM.unpinPage(currPageId, false);
			Minibase.JavabaseBM.freePage(currPageId);
		} else if (sortedPage.getType() == BTSortedPage.LEAF) {
			Minibase.JavabaseBM.unpinPage(currPageId, false);
			Minibase.JavabaseBM.freePage(currPageId);
		} else {
			Minibase.JavabaseBM.unpinPage(currPageId, false/* not dirty */);
		}
	}


	/**
	 * insert record with the given key and rid
	 * 
	 * @param key
	 *            the key of the record. Input parameter.
	 * @param rid
	 *            the rid of the record. Input parameter.
	 * @exception KeyTooLongException
	 *                key size exceeds the max keysize.
	 * @exception KeyNotMatchException
	 *                key is not integer key nor string key
	 * @exception IOException
	 *                error from the lower layer
	 * @exception LeafInsertRecException
	 *                insert error in leaf page
	 * @exception IndexInsertRecException
	 *                insert error in index page
	 * @exception ConstructPageException
	 *                error in BT page constructor
	 * @exception UnpinPageException
	 *                error when unpin a page
	 * @exception PinPageException
	 *                error when pin a page
	 * @exception NodeNotMatchException
	 *                node not match index page nor leaf page
	 * @exception ConvertException
	 *                error when convert between revord and byte array
	 * @exception DeleteRecException
	 *                error when delete in index page
	 * @exception IndexSearchException
	 *                error when search
	 * @exception IteratorException
	 *                iterator error
	 * @exception LeafDeleteException
	 *                error when delete in leaf page
	 * @exception InsertException
	 *                error when insert in index page
	 */
	public void insert(Key key, RID rid) throws KeyTooLongException,
			KeyNotMatchException, LeafInsertRecException,
			IndexInsertRecException, ConstructPageException,
			UnpinPageException, PinPageException, NodeNotMatchException,
			ConvertException, DeleteRecException, IndexSearchException,
			IteratorException, LeafDeleteException, InsertException,
			IOException
	{
		if (header.get_rootId().pid == INVALID_PAGE) {
			// create a new page root
			BTLeafPage rootPage = new BTLeafPage(header.get_keyType());
			
			rootPage.setPrevPage(new PageId(INVALID_PAGE));
			rootPage.setNextPage(new PageId(INVALID_PAGE));
			
			rootPage.insertRecord(key, rid);
			
			// update header:
			// retrieve header page by declaring a dummy header and pin it
			// (tmpHeader is used to pin the page with pid and get the corresponding SortedPage)
			BTHeaderPage tmpHeader = new BTHeaderPage(header.getPageId());
			
			// update root id
			tmpHeader.set_rootId(rootPage.getCurPage());
			
			try {
				// unpin and set the dirty bit and then write to db
				// one is for the header page, the other one is for the root page
				Minibase.JavabaseBM.unpinPage(header.getPageId(), true);
				Minibase.JavabaseBM.unpinPage(rootPage.getCurPage(), true);
			} catch (ReplacerException | PageUnpinnedException | HashEntryNotFoundException
					| InvalidFrameNumberException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// NOT IMPLEMENTED YET
			KeyEntry newEntry = null;
			try {
				newEntry = insertHelper(header.get_rootId(), key, rid);
			} catch (ReplacerException | PageUnpinnedException | HashEntryNotFoundException
					| InvalidFrameNumberException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (newEntry != null) {
				// New root occurs
				BTIndexPage newRootPage = new BTIndexPage(header.get_keyType());
				newRootPage.insertKey(newEntry.key, (PageId)newEntry.getData());
				
				// Set the new root's left-most pointer to the original page
				// Needn't to set the next point because the newEntry already has this info
				newRootPage.setPrevPage(header.get_rootId());
				
				// update the header with the new root id
				BTHeaderPage tmpHeader = new BTHeaderPage(header.getPageId());
				tmpHeader.set_rootId(newRootPage.getCurPage());
				try {
					Minibase.JavabaseBM.unpinPage(header.getPageId(), true);
					Minibase.JavabaseBM.unpinPage(newRootPage.getCurPage(), true);
				} catch (ReplacerException | PageUnpinnedException | HashEntryNotFoundException
						| InvalidFrameNumberException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private KeyEntry insertHelper(PageId currPage, Key key, RID rid) throws 
		ConstructPageException, IOException, IndexSearchException, ReplacerException, 
		PageUnpinnedException, HashEntryNotFoundException, InvalidFrameNumberException, 
		IteratorException, LeafInsertRecException, KeyNotMatchException,
		DeleteRecException, IndexInsertRecException {
		short keyType = header.get_keyType();
		
		// This will pin page (currPage)
		BTSortedPage sortedPage = new BTSortedPage(currPage, keyType);
		
		if (sortedPage.getType() == BTSortedPage.INDEX) {
			// Use page as argument to get a new indexPage does not pin the page again
			BTIndexPage indexPage = new BTIndexPage((Page)sortedPage, keyType);
			
			// Get the entry(pid) for a given key
			PageId nextPage = indexPage.getPageNoByKey(key);
			
			// Unpin because we've already got the next page info
			Minibase.JavabaseBM.unpinPage(currPage, false/* not dirty */);
			
			// Recursively insert <key, rid> to the next page
			KeyEntry newChildEntry = insertHelper(nextPage, key, rid);
			
			// If newChildEntry is null, then return because there is no child split (push up)
			// Else, handle split
			if (newChildEntry == null) return newChildEntry;
			else {
				indexPage = new BTIndexPage(currPage, keyType); // Read again
				// If there is enough space, then insert then return
				if (indexPage.available_space() >= newChildEntry.getSizeInBytes()) {
					indexPage.insertKey(newChildEntry.key, (PageId)newChildEntry.getData());
					Minibase.JavabaseBM.unpinPage(currPage, true);
					return null;
				} else {
					// NOT IMPLEMENTED YET: Handle split
					BTIndexPage newIndexPage = new BTIndexPage(keyType);
					
					// NOTICE: Same logic as inserting a leaf
					// Get the middle index
					RID dummyRid = new RID();
					int entryCount = 0;
					for (KeyEntry entry = indexPage.getFirst(dummyRid); entry != null; entry = indexPage.getNext(dummyRid)){
						entryCount++;
					}
					
					int midEntryIndex = (int)Math.floor((entryCount - 1) / 2.0);
					entryCount = 0;
					KeyEntry midEntry = indexPage.getFirst(dummyRid);
					while (entryCount < midEntryIndex) {
						midEntry = indexPage.getNext(dummyRid);
						entryCount++;
					}
					
					// Move all entries from currIndex to newIndex, then move half size back
					// Because there is a deletion operation, the whole copy must happen
					KeyEntry tmpEntry = null;
					for (tmpEntry = indexPage.getFirst(dummyRid); tmpEntry != null; 
						 tmpEntry = indexPage.getFirst(dummyRid)) {
						newIndexPage.insertKey(tmpEntry.key, (PageId)tmpEntry.getData());
						indexPage.deleteSortedRecord(dummyRid);
					}
					
					if (newChildEntry.key.compareTo(midEntry.key) <= 0) {
						entryCount = 0;
						// Move back 0 to m-1 entries from newLeaf to currLeaf
						for (KeyEntry oldEntry = newIndexPage.getFirst(dummyRid); entryCount < midEntryIndex;
							 oldEntry = newIndexPage.getFirst(dummyRid)) {
							indexPage.insertKey(oldEntry.key, (PageId)(oldEntry.getData()));
							newIndexPage.deleteSortedRecord(dummyRid);
							entryCount++;
						}
						indexPage.insertKey(newChildEntry.key, (PageId)newChildEntry.getData());
					} else {
						entryCount = 0;
						// Move back 0 to m entries to current
						for (KeyEntry oldEntry = newIndexPage.getFirst(dummyRid); entryCount <= midEntryIndex;
							 oldEntry = newIndexPage.getFirst(dummyRid)) {
							indexPage.insertKey(oldEntry.key, (PageId)(oldEntry.getData()));
							newIndexPage.deleteSortedRecord(dummyRid);
							entryCount++;
						}
						newIndexPage.insertKey(newChildEntry.key, (PageId)newChildEntry.getData());
					}
					
					// push up new parent
					KeyEntry newParent = newIndexPage.getFirst(dummyRid);
					newIndexPage.setPrevPage((PageId)newParent.getData());
					newIndexPage.deleteSortedRecord(dummyRid);
					
					// reset new parent's data to the new index page
					newParent.setData(newIndexPage.getCurPage());
					
					Minibase.JavabaseBM.unpinPage(currPage, true);
					Minibase.JavabaseBM.unpinPage(newIndexPage.getCurPage(), true);
					return newParent;
				}
				
			}
		} else if (sortedPage.getType() == BTSortedPage.LEAF) {
			BTLeafPage currLeafPage = new BTLeafPage((Page)sortedPage, keyType);
			KeyEntry keyEntry = new KeyEntry(key, rid);
			
			// Handle duplicate. No insert when it happens.
			RID scanRid = new RID();
			for (KeyEntry entry = currLeafPage.getFirst(scanRid); entry != null;  entry = currLeafPage.getNext(scanRid)) {
				if (entry.key.equals(key)) {
					Minibase.JavabaseBM.unpinPage(currPage, true);
					return null;
				}
			}
			
			if (currLeafPage.available_space() >= keyEntry.getSizeInBytes()) {
				currLeafPage.insertRecord(keyEntry.key, (RID)keyEntry.getData());
				Minibase.JavabaseBM.unpinPage(currPage, true);
				return null;
			} else {
				// System.out.println("Leaf split starts");
				// Handle leaf split
				// New a leaf page
				BTLeafPage newLeafPage = new BTLeafPage(keyType);
				
				// Set its double links
				newLeafPage.setPrevPage(currLeafPage.getCurPage());
				newLeafPage.setNextPage(currLeafPage.getNextPage());
				PageId nextPageId = newLeafPage.getNextPage();
				if (nextPageId.pid != INVALID_PAGE) {
					BTLeafPage nextPage = new BTLeafPage(nextPageId, keyType);
					nextPage.setPrevPage(newLeafPage.getCurPage());
					Minibase.JavabaseBM.unpinPage(nextPageId, false);
				}
				currLeafPage.setNextPage(newLeafPage.getCurPage());
				
				// Get the middle index
				RID dummyRid = new RID();
				int entryCount = 0;
				for (KeyEntry entry = currLeafPage.getFirst(dummyRid); entry != null;  entry = currLeafPage.getNext(dummyRid)){
					entryCount++;
				}
				
				int midEntryIndex = (int)Math.floor((entryCount - 1) / 2.0);
				entryCount = 0;
				KeyEntry midEntry = currLeafPage.getFirst(dummyRid);
				while (entryCount < midEntryIndex) {
					midEntry = currLeafPage.getNext(dummyRid);
					entryCount++;
				}
				
				// System.out.println("mid entry key: " + midEntry.key);
				
				// Move all entries from currLeaf to newLeaf, then move half size back
				// Because there is a deletion operation, the whole copy must happen
				KeyEntry tmpEntry = null;
				for (tmpEntry = currLeafPage.getFirst(dummyRid); tmpEntry != null; 
					 tmpEntry = currLeafPage.getFirst(dummyRid)) {
					newLeafPage.insertRecord(tmpEntry.key, (RID)(tmpEntry.getData()));
					currLeafPage.deleteSortedRecord(dummyRid);
					// System.out.println("move key to new leaf: " + tmpEntry.key);
				}
				
				// if the insert key is less than the middle key
				//   L1 = [0, m-1] + insert
				//   L2 = [m, n  ]
				// else
				//   L1 = [0  , m]
				//   L2 = [m+1, n] + insert
				if (key.compareTo(midEntry.key) <= 0) {
					entryCount = 0;
					// Move back 0 to m-1 entries from newLeaf to currLeaf
					for (KeyEntry oldEntry = newLeafPage.getFirst(dummyRid); entryCount < midEntryIndex;
						 oldEntry = newLeafPage.getFirst(dummyRid)) {
						currLeafPage.insertRecord(oldEntry.key, (RID)(oldEntry.getData()));
						newLeafPage.deleteSortedRecord(dummyRid);
						entryCount++;
						// System.out.println("undo key: " + oldEntry.key);
					}
					currLeafPage.insertRecord(keyEntry.key, (RID)keyEntry.getData());
				} else {
					entryCount = 0;
					// Move back 0 to m entries to current
					for (KeyEntry oldEntry = newLeafPage.getFirst(dummyRid); entryCount <= midEntryIndex;
						 oldEntry = newLeafPage.getFirst(dummyRid)) {
						currLeafPage.insertRecord(oldEntry.key, (RID)(oldEntry.getData()));
						newLeafPage.deleteSortedRecord(dummyRid);
						entryCount++;
						// System.out.println("undo key: " + oldEntry.key);
					}
					newLeafPage.insertRecord(keyEntry.key, (RID)keyEntry.getData());
				}
				
				// Grab the first entry from newLeaf as new parent
				tmpEntry = newLeafPage.getFirst(dummyRid);
				KeyEntry newParent = new KeyEntry(tmpEntry.key, newLeafPage.getCurPage());
				Minibase.JavabaseBM.unpinPage(currPage, true);
				Minibase.JavabaseBM.unpinPage(newLeafPage.getCurPage(), true);
				return newParent;
			}
		}
		return null;
	}

	/**
	 * delete leaf entry given its <key, rid> pair. `rid' is IN the data entry;
	 * it is not the id of the data entry)
	 * 
	 * @param key
	 *            the key in pair <key, rid>. Input Parameter.
	 * @param rid
	 *            the rid in pair <key, rid>. Input Parameter.
	 * @return true if deleted. false if no such record.
	 * @exception DeleteFashionException
	 *                neither full delete nor naive delete
	 * @exception LeafRedistributeException
	 *                redistribution error in leaf pages
	 * @exception RedistributeException
	 *                redistribution error in index pages
	 * @exception InsertRecException
	 *                error when insert in index page
	 * @exception KeyNotMatchException
	 *                key is neither integer key nor string key
	 * @exception UnpinPageException
	 *                error when unpin a page
	 * @exception IndexInsertRecException
	 *                error when insert in index page
	 * @exception FreePageException
	 *                error in BT page constructor
	 * @exception RecordNotFoundException
	 *                error delete a record in a BT page
	 * @exception PinPageException
	 *                error when pin a page
	 * @exception IndexFullDeleteException
	 *                fill delete error
	 * @exception LeafDeleteException
	 *                delete error in leaf page
	 * @exception IteratorException
	 *                iterator error
	 * @exception ConstructPageException
	 *                error in BT page constructor
	 * @exception DeleteRecException
	 *                error when delete in index page
	 * @exception IndexSearchException
	 *                error in search in index pages
	 * @exception IOException
	 *                error from the lower layer
	 * 
	 */
	public boolean delete(Key key, RID rid) throws DeleteFashionException,
			LeafRedistributeException, RedistributeException,
			InsertRecException, KeyNotMatchException, UnpinPageException,
			IndexInsertRecException, FreePageException,
			RecordNotFoundException, PinPageException,
			IndexFullDeleteException, LeafDeleteException, IteratorException,
			ConstructPageException, DeleteRecException, IndexSearchException,
			IOException
	{
		if (header.get_rootId().pid != -1)
			try {
				deleteHelper(key, rid, header.get_rootId(), null);
			} catch (ReplacerException | PageUnpinnedException | HashEntryNotFoundException
					| InvalidFrameNumberException | InvalidBufferException | HashOperationException 
					| PageNotReadException | BufferPoolExceededException | PagePinnedException 
					| BufMgrException | DiskMgrException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return(false);
	}
	
	private Key deleteHelper(Key key, RID rid, PageId currPage, PageId parentPage) throws IOException, ConstructPageException, IteratorException, 
			KeyNotMatchException, ReplacerException, PageUnpinnedException, HashEntryNotFoundException, InvalidFrameNumberException,
			LeafDeleteException, RecordNotFoundException, IndexSearchException, InvalidBufferException, HashOperationException,
			PageNotReadException, BufferPoolExceededException, PagePinnedException, BufMgrException, DiskMgrException, 
			IndexFullDeleteException, InsertRecException, DeleteRecException, IndexInsertRecException {
		// NOT IMPLEMENTED YET
		
		short keyType = header.get_keyType();
		
		// This will pin page (currPage)
		BTSortedPage sortedPage = new BTSortedPage(currPage, keyType);
		if (sortedPage.getType() == BTSortedPage.INDEX) {
			// NOT IMPLEMENTED YET
			// Find i such that Ki <= entry's key <= Ki+1
			BTIndexPage currIndexPage = new BTIndexPage((Page)sortedPage, keyType);
			PageId nextPage = currIndexPage.getPageNoByKey(key);
			// Unpin because we've already got the next page info
			Minibase.JavabaseBM.unpinPage(currPage, false/* not dirty */);
			
			Key oldChildKey = deleteHelper(key, rid, nextPage, currIndexPage.getCurPage());
			if (oldChildKey == null)
				return null;
			
			// System.out.println("HAS UP ENTRY with key: " + oldChildKey);
			currIndexPage = new BTIndexPage(sortedPage.getCurPage(), keyType);
			currIndexPage.deleteKey(oldChildKey);
			
			// check if current index is the root page
			if (header.get_rootId().pid == currIndexPage.getCurPage().pid) {
				// System.out.println("up entry merges then delete at the root page");
				if (currIndexPage.numberOfRecords() > 0) {
					Minibase.JavabaseBM.unpinPage(currIndexPage.getCurPage(), true);
					return null;
				} else {
				// reset header: set root to prev leaf
				BTHeaderPage tmpHeader = new BTHeaderPage(header.getPageId());
				tmpHeader.set_rootId(currIndexPage.getPrevPage());
				try {
					Minibase.JavabaseBM.unpinPage(header.getPageId(), true);
				} catch (ReplacerException | PageUnpinnedException | HashEntryNotFoundException
						| InvalidFrameNumberException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// free the root page
				Minibase.JavabaseBM.freePage(currIndexPage.getCurPage());
				return null;
				}
			} else { // if current index is not the root page and the merging occurs
				if (currIndexPage.available_space() > ((PAGE_SIZE - HFPage.DPFIXED) / 2)) {
					// System.out.println("[Index] Underflow occurs!!");
					
					// check sibling space
					PageId siblingPage = new PageId();
					BTIndexPage parentIndexPage = new BTIndexPage(parentPage, keyType);
					// 0: no sibling, -1: left sibling, 1: right sibling
					int direction = parentIndexPage.getSibling(key, siblingPage);
					
					// System.out.println("[Index] Sibling direction: " + direction);
					if (direction == 0) {
						// No siblings
						Minibase.JavabaseBM.unpinPage(parentPage, false);
						Minibase.JavabaseBM.unpinPage(currIndexPage.getCurPage(), false);
						return null;
					}
					
					BTIndexPage siblingIndexPage = new BTIndexPage(siblingPage, keyType);
					
					// if sibling has no enough space, then do not merge
					if (siblingIndexPage.available_space() >= ((PAGE_SIZE - HFPage.DPFIXED) - (currIndexPage.available_space()))) {
						// System.out.println("[Index] Sibling has enough space, can do a merge");
						KeyEntry oldChildEntry; // this is used for pop-up then delete
						RID tmpRid = new RID();
						BTIndexPage leftPage, rightPage;
						if (direction == 1) { // right sibling
							oldChildEntry = siblingIndexPage.getFirst(tmpRid);
							leftPage = currIndexPage;
							rightPage = siblingIndexPage;
						} else { // left sibling
							oldChildEntry = currIndexPage.getFirst(tmpRid);
							leftPage = siblingIndexPage;
							rightPage = currIndexPage;
						}
						
						// pull down the parent key and connect its pageNo to rightPage's prevPage
						Key parentKey = parentIndexPage.findKey(oldChildEntry.key);
						leftPage.insertKey(parentKey, rightPage.getPrevPage());
						
						// move all entries from Right to Left
						KeyEntry movingEntry;
						for (movingEntry = rightPage.getFirst(tmpRid); movingEntry != null;
							 movingEntry = rightPage.getFirst(tmpRid)) {
							leftPage.insertRecord(movingEntry);
							rightPage.deleteSortedRecord(tmpRid);
						}
						
						// current is changed to leftPage or rightPage so
						// it will be freed or unpinned.
						Minibase.JavabaseBM.unpinPage(leftPage.getCurPage(), true);
						Minibase.JavabaseBM.unpinPage(parentPage, true);
						Minibase.JavabaseBM.freePage(rightPage.getCurPage());
						return oldChildEntry.key;
					} else {
						// System.out.println("[Index] Sibling has no enough space to merge.");
						Minibase.JavabaseBM.unpinPage(parentPage, false);
						Minibase.JavabaseBM.unpinPage(currIndexPage.getCurPage(), true);
						Minibase.JavabaseBM.unpinPage(siblingPage, true);
						return null;
					}
				}
			}
			
		} else if (sortedPage.getType() == BTSortedPage.LEAF) {
			BTLeafPage currLeafPage = new BTLeafPage((Page)sortedPage, keyType);
			RID dummyRid = new RID();
			KeyEntry tmpEntry = currLeafPage.getFirst(dummyRid);
			KeyEntry delEntry = new KeyEntry(key, rid);
			if (currLeafPage.delEntry(delEntry)) {
				// System.out.println("Successfully delete!!");
				// check underflow
				if (currLeafPage.available_space() > ((PAGE_SIZE - HFPage.DPFIXED) / 2)) {
					// System.out.println("Underflow occurs!!");
					
					// Merge might occur
					// If current leaf is the root, no merge
					if (header.get_rootId().pid == currLeafPage.getCurPage().pid) {
						if (currLeafPage.numberOfRecords() > 0) {
							Minibase.JavabaseBM.unpinPage(currLeafPage.getCurPage(), false);
							return null;
						} else {
							// free the whole tree
							Minibase.JavabaseBM.freePage(currLeafPage.getCurPage());
							
							// reset header:
							BTHeaderPage tmpHeader = new BTHeaderPage(header.getPageId());
							tmpHeader.set_rootId(new PageId(INVALID_PAGE));
							try {
								Minibase.JavabaseBM.unpinPage(header.getPageId(), true);
							} catch (ReplacerException | PageUnpinnedException | HashEntryNotFoundException
									| InvalidFrameNumberException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return null;
						}
					} else {
						// Merge with siblings
						// System.out.println("Merging leaf with sibling.");
						PageId siblingPage = new PageId();
						BTIndexPage parentIndexPage = new BTIndexPage(parentPage, keyType);
						// 0: no sibling, -1: left sibling, 1: right sibling
						int direction = parentIndexPage.getSibling(key, siblingPage);
						
						// System.out.println("Sibling direction: " + direction);
						if (direction == 0) {
							// No siblings
							Minibase.JavabaseBM.unpinPage(parentPage, false);
							Minibase.JavabaseBM.unpinPage(currLeafPage.getCurPage(), false);
							return null;
						}
						
						BTLeafPage siblingLeafPage = new BTLeafPage(siblingPage, keyType);
						
						// if sibling has no enough space, then do not merge
						if (siblingLeafPage.available_space() >= (PAGE_SIZE - HFPage.DPFIXED - currLeafPage.available_space())) {
							// System.out.println("Sibling has enough space, can do a merge");
							KeyEntry oldChildEntry; // this is used for pop-up then delete
							RID tmpRid = new RID();
							BTLeafPage leftPage, rightPage;
							if (direction == 1) { // right sibling
								oldChildEntry = siblingLeafPage.getFirst(tmpRid);
								leftPage = currLeafPage;
								rightPage = siblingLeafPage;
							} else { // left sibling
								oldChildEntry = currLeafPage.getFirst(tmpRid);
								leftPage = siblingLeafPage;
								rightPage = currLeafPage;
							}
							
							// move all entries from Right to Left
							KeyEntry movingEntry;
							for (movingEntry = rightPage.getFirst(tmpRid); movingEntry != null;
								 movingEntry = rightPage.getFirst(tmpRid)) {
								leftPage.insertRecord(movingEntry);
								rightPage.deleteSortedRecord(tmpRid);
							}
							
							// adjust leaf pointers (delete right page in double linked-list)
							if (rightPage.getNextPage().pid != INVALID_PAGE) {
								BTLeafPage rightNextPage = new BTLeafPage(rightPage.getNextPage(), keyType);
								rightNextPage.setPrevPage(leftPage.getCurPage());
								Minibase.JavabaseBM.unpinPage(rightNextPage.getCurPage(), true);
							}
							leftPage.setNextPage(rightPage.getNextPage());
							
							// current is changed to leftPage or rightPage so
							// it will be freed or unpinned.
							Minibase.JavabaseBM.unpinPage(leftPage.getCurPage(), true);
							Minibase.JavabaseBM.unpinPage(parentPage, true);
							Minibase.JavabaseBM.freePage(rightPage.getCurPage());
							return oldChildEntry.key;
						} else {
							// System.out.println("Sibling has no enough space to merge.");
							Minibase.JavabaseBM.unpinPage(parentPage, true);
							Minibase.JavabaseBM.unpinPage(currLeafPage.getCurPage(), true);
							Minibase.JavabaseBM.unpinPage(siblingPage, true);
							return null;
						}
					}
				}
			} else {
				 System.out.println("Delete FAIL!!");
			}

			Minibase.JavabaseBM.unpinPage(currLeafPage.getCurPage(), false);
		}
		return null;
	}


	/**
	 * create a scan with given keys Cases: (1) lo_key = null, hi_key = null
	 * scan the whole index (2) lo_key = null, hi_key!= null range scan from min
	 * to the hi_key (3) lo_key!= null, hi_key = null range scan from the lo_key
	 * to max (4) lo_key!= null, hi_key!= null, lo_key = hi_key exact match (
	 * might not unique) (5) lo_key!= null, hi_key!= null, lo_key < hi_key range
	 * scan from lo_key to hi_key
	 * 
	 * @param lo_key
	 *            the key where we begin scanning. Input parameter.
	 * @param hi_key
	 *            the key where we stop scanning. Input parameter.
	 * @exception IOException
	 *                error from the lower layer
	 * @exception KeyNotMatchException
	 *                key is not integer key nor string key
	 * @exception IteratorException
	 *                iterator error
	 * @exception ConstructPageException
	 *                error in BT page constructor
	 * @exception PinPageException
	 *                error when pin a page
	 * @exception UnpinPageException
	 *                error when unpin a page
	 */
	public BTFileScan new_scan(Key lo_key, Key hi_key)
			throws IOException, KeyNotMatchException, IteratorException,
			ConstructPageException, PinPageException, UnpinPageException

	{
		return(null);
	}


	/**
	 * For debug. Print the B+ tree structure out
	 * 
	 * @param header
	 *            the head page of the B+ tree file
	 * @exception IOException
	 *                error from the lower layer
	 * @exception ConstructPageException
	 *                error from BT page constructor
	 * @exception IteratorException
	 *                error from iterator
	 * @exception HashEntryNotFoundException
	 *                error from lower layer
	 * @exception InvalidFrameNumberException
	 *                error from lower layer
	 * @exception PageUnpinnedException
	 *                error from lower layer
	 * @exception ReplacerException
	 *                error from lower layer
	 */
	public void printBTree() throws IOException,
			ConstructPageException, IteratorException,
			HashEntryNotFoundException, InvalidFrameNumberException,
			PageUnpinnedException, ReplacerException
	{
		BTHeaderPage header = getHeaderPage();
		if (header.get_rootId().pid == INVALID_PAGE)
		{
			System.out.println("The Tree is Empty!!!");
			return;
		}

		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("---------------The B+ Tree Structure---------------");

		System.out.println("header page: " + header.get_rootId());

		_printTree(header.get_rootId(), "", header.get_keyType());

		System.out.println("--------------- End ---------------");
		System.out.println("");
		System.out.println("");
	}

	private void _printTree(PageId currentPageId, String prefix, int keyType) throws IOException, ConstructPageException,
			IteratorException, HashEntryNotFoundException,
			InvalidFrameNumberException, PageUnpinnedException,
			ReplacerException
	{

		BTSortedPage sortedPage = new BTSortedPage(currentPageId, keyType);
		prefix = prefix + "    ";
		
		// for index pages, go through their child pages
		if (sortedPage.getType() == BTSortedPage.INDEX)
		{
			BTIndexPage indexPage = new BTIndexPage((Page) sortedPage, keyType);

			System.out.println(prefix + "index page: " + currentPageId);
			System.out.println(prefix + "  first child: " + 
					indexPage.getPrevPage());
			
			_printTree(indexPage.getPrevPage(), prefix, keyType);

			RID rid = new RID();
			for (KeyEntry entry = indexPage.getFirst(rid); 
				 entry != null;  
				 entry = indexPage.getNext(rid))
			{
				System.out.println(prefix + "  key: " + entry.key + ", page: ");
				_printTree((PageId) entry.getData(), prefix, keyType);
			}
		}
		
		// for leaf pages, iterate through the keys and print them out
		else if (sortedPage.getType() == BTSortedPage.LEAF)
		{
			BTLeafPage leafPage = new BTLeafPage((Page) sortedPage, keyType);
			RID rid = new RID();
			System.out.println(prefix + "leaf page: " + sortedPage);
			for (KeyEntry entry = leafPage.getFirst(rid); 
				 entry != null;  
				 entry = leafPage.getNext(rid))
			{
				if (keyType == AttrType.attrInteger)
					System.out.println(prefix + "  ("
							+ entry.key + ",  "
							+ entry.getData() + " )");
				if (keyType == AttrType.attrString)
					System.out.println(prefix + "  ("
							+ entry.key + ",  "
							+ entry.getData() + " )");
			}
		}		
		
		Minibase.JavabaseBM.unpinPage(currentPageId, false/* not dirty */);
	}


}
