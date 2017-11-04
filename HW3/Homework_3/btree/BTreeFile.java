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
import exceptions.FileEntryNotFoundException;
import exceptions.FileIOException;
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
	 */
	public BTreeFile(String filename, int keytype, int keysize,
			int delete_fashion) throws GetFileEntryException,
			ConstructPageException, IOException, AddFileEntryException, FileIOException, InvalidPageNumberException, DiskMgrException, PinPageException
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
		}
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
		return(false);
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
