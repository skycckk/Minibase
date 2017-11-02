/*
 * @(#) bt.java   98/03/24
 * Copyright (c) 1998 UW.  All Rights Reserved.
 *         Author: Xiaohu Li (xioahu@cs.wisc.edu).
 *
 */

package btree;

import diskmgr.Page;
import exceptions.AddFileEntryException;
import exceptions.ConstructPageException;
import exceptions.ConvertException;
import exceptions.DeleteFashionException;
import exceptions.DeleteFileEntryException;
import exceptions.DeleteRecException;
import exceptions.FreePageException;
import exceptions.GetFileEntryException;
import exceptions.HashEntryNotFoundException;
import exceptions.IndexFullDeleteException;
import exceptions.IndexInsertRecException;
import exceptions.IndexSearchException;
import exceptions.InsertException;
import exceptions.InsertRecException;
import exceptions.InvalidFrameNumberException;
import exceptions.IteratorException;
import exceptions.KeyNotMatchException;
import exceptions.KeyTooLongException;
import exceptions.LeafDeleteException;
import exceptions.LeafInsertRecException;
import exceptions.LeafRedistributeException;
import exceptions.NodeNotMatchException;
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


	/**
	 * Access method to data member.
	 * 
	 * @return Return a BTreeHeaderPage object that is the header page of this
	 *         btree file.
	 */
	public BTHeaderPage getHeaderPage()
	{
		return null;
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
	 */
	public BTreeFile(String filename) throws GetFileEntryException,
			PinPageException, ConstructPageException
	{
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
	 */
	public BTreeFile(String filename, int keytype, int keysize,
			int delete_fashion) throws GetFileEntryException,
			ConstructPageException, IOException, AddFileEntryException
	{
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
	 */
	public void close() throws PageUnpinnedException,
			InvalidFrameNumberException, HashEntryNotFoundException,
			ReplacerException
	{
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
	 */
	public void destroyFile() throws IOException, IteratorException,
			UnpinPageException, FreePageException, DeleteFileEntryException,
			ConstructPageException, PinPageException
	{
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
