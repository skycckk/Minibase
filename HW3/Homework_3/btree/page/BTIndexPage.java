/*
 * @(#) BTIndexPage.java   98/05/14
 * Copyright (c) 1998 UW.  All Rights Reserved.
 *         Author: Xiaohu Li (xioahu@cs.wisc.edu)
 *
 */

package btree.page;

import global.PageId;
import global.RID;

import index.Key;
import index.KeyEntry;

import java.io.IOException;

import diskmgr.Page;
import exceptions.ConstructPageException;
import exceptions.IndexFullDeleteException;
import exceptions.IndexInsertRecException;
import exceptions.IndexSearchException;
import exceptions.IteratorException;
import exceptions.RedistributeException;

/**
 * A BTIndexPage is an index page on a B+ tree. It holds abstract {key, PageId}
 * pairs; it doesn't know anything about the keys (their lengths or their
 * types), instead relying on the abstract interface in BT.java See those files
 * for our {key,data} pairing interface and implementation.
 */
public class BTIndexPage extends BTSortedPage
{

	/**
	 * pin the page with pageno, and get the corresponding BTIndexPage, also it
	 * sets the type of node to be INDEX.
	 * 
	 * @param pageno
	 *            Input parameter. To specify which page number the BTIndexPage
	 *            will correspond to.
	 * @param keyType
	 *            either AttrType.attrInteger or AttrType.attrString. Input
	 *            parameter.
	 * @exception IOException
	 *                error from the lower layer
	 * @exception ConstructPageException
	 *                error when BTIndexpage constructor
	 */
	public BTIndexPage(PageId pageno, int keyType) throws IOException,
			ConstructPageException
	{
		super(pageno, keyType);
		setType(INDEX);
	}

	/**
	 * associate the BTIndexPage instance with the Page instance, also it sets
	 * the type of node to be INDEX.
	 * 
	 * @param page
	 *            input parameter. To specify which page the BTIndexPage will
	 *            correspond to.
	 * @param keyType
	 *            either AttrType.attrInteger or AttrType.attrString. Input
	 *            parameter.
	 * @exception IOException
	 *                error from the lower layer
	 * @exception ConstructPageException
	 *                error when BTIndexpage constructor
	 */
	public BTIndexPage(Page page, int keyType) throws IOException,
			ConstructPageException
	{
		super(page, keyType);
		setType(INDEX);
	}

	/*
	 * new a page, associate the BTIndexPage instance with the Page instance,
	 * also it sets the type of node to be INDEX. @param keyType either
	 * AttrType.attrInteger or AttrType.attrString. Input parameter. @exception
	 * IOException error from the lower layer @exception ConstructPageException
	 * error when BTIndexpage constructor
	 */
	public BTIndexPage(int keyType) throws IOException, ConstructPageException
	{
		super(keyType);
		setType(INDEX);
	}

	/**
	 * It inserts a <key, pageNo> value into the index page,
	 * 
	 * @key the key value in <key, pageNO>. Input parameter.
	 * @pageNo the pageNo in <key, pageNO>. Input parameter.
	 * @return It returns the rid where the record is inserted; null if no space
	 *         left.
	 * @exception IndexInsertRecException
	 *                error when insert
	 */
	public RID insertKey(Key key, PageId pageNo)
			throws IndexInsertRecException
	{
		RID rid;
		KeyEntry entry;
		try
		{
			entry = new KeyEntry(key, pageNo);
			rid = super.insertRecord(entry);
			return rid;
		} catch (Exception e)
		{
			throw new IndexInsertRecException(e, "Insert failed");

		}
	}

	/*
	 * OPTIONAL: fullDeletekey This is optional, and is only needed if you want
	 * to do full deletion. Return its RID. delete key may != key. But delete
	 * key <= key, and the delete key is the first biggest key such that delete
	 * key <= key @param key the key used to search. Input parameter. @exception
	 * IndexFullDeleteException if no record deleted or failed by any reason
	 * @return RID of the record deleted. Can not return null.
	 */
	public RID deleteKey(Key key) throws IndexFullDeleteException
	{
		KeyEntry entry;
		RID rid = new RID();

		try
		{

			entry = getFirst(rid);

			if (entry == null)
				// it is supposed there is at least a record
				throw new IndexFullDeleteException(null, "No records found");

			if (key.compareTo(entry.key) < 0)
				// it is supposed to not smaller than first key
				throw new IndexFullDeleteException(null, "First key is bigger");

			while (key.compareTo(entry.key) > 0)
			{
				entry = getNext(rid);
				if (entry == null)
					break;
			}

			if (entry == null)
				rid.slotNo--;
			else if (key.compareTo(entry.key) != 0)
				rid.slotNo--; // we want to delete the previous key

			deleteSortedRecord(rid);
			return rid;
		} catch (Exception e)
		{
			throw new IndexFullDeleteException(e, "Full delelte failed");
		}
	} // end of deleteKey

	/*
	 * This function encapsulates the search routine to search a BTIndexPage by
	 * B++ search algorithm @param key the key value used in search algorithm.
	 * Input parameter. @return It returns the page_no of the child to be
	 * searched next. @exception IndexSearchException Index search failed;
	 */
	public PageId getPageNoByKey(Key key) throws IndexSearchException
	{
		KeyEntry entry;
		int i;

		try
		{

			for (i = getSlotCnt() - 1; i >= 0; i--)
			{
				entry = new KeyEntry(getpage(), getSlotOffset(i),
						getSlotLength(i), keyType, INDEX);

				if (key.compareTo(entry.key) >= 0)
				{
					return (PageId) entry.getData();
				}
			}

			return getPrevPage();
		} catch (Exception e)
		{
			throw new IndexSearchException(e, "Get entry failed");
		}

	} // getPageNoByKey

	/**
	 * Iterators. One of the two functions: getFirst and getNext which provide
	 * an iterator interface to the records on a BTIndexPage.
	 * 
	 * @param rid
	 *            It will be modified and the first rid in the index page will
	 *            be passed out by itself. Input and Output parameter.
	 * @return return the first KeyDataEntry in the index page. null if NO MORE
	 *         RECORD
	 * @exception IteratorException
	 *                iterator error
	 */
	public KeyEntry getFirst(RID rid) throws IteratorException
	{

		KeyEntry entry;

		try
		{
			rid.pageNo = getCurPage();
			rid.slotNo = 0; // begin with first slot

			if (getSlotCnt() == 0)
			{
				return null;
			}

			entry = new KeyEntry(getpage(), getSlotOffset(0),
					getSlotLength(0), keyType, INDEX);

			return entry;
		} catch (Exception e)
		{
			throw new IteratorException(e, "Get first entry failed");
		}

	} // end of getFirst

	/**
	 * Iterators. One of the two functions: get_first and get_next which provide
	 * an iterator interface to the records on a BTIndexPage.
	 * 
	 * @param rid
	 *            It will be modified and next rid will be passed out by itself.
	 *            Input and Output parameter.
	 * @return return the next KeyDataEntry in the index page. null if no more
	 *         record
	 * @exception IteratorException
	 *                iterator error
	 */
	public KeyEntry getNext(RID rid) throws IteratorException
	{
		KeyEntry entry;
		int i;
		try
		{
			rid.slotNo++; // must before any return;
			i = rid.slotNo;

			if (rid.slotNo >= getSlotCnt())
			{
				return null;
			}

			entry = new KeyEntry(getpage(), getSlotOffset(i),
					getSlotLength(i), keyType, INDEX);

			return entry;
		} catch (Exception e)
		{
			throw new IteratorException(e, "Get next entry failed");
		}
	} // end of getNext

	/**
	 * Left Link You will recall that the index pages have a left-most pointer
	 * that is followed whenever the search key value is less than the least key
	 * value in the index node. The previous page pointer is used to implement
	 * the left link.
	 * 
	 * @return It returns the left most link.
	 * @exception IOException
	 *                error from the lower layer
	 */
	public PageId getLeftLink() throws IOException
	{
		return getPrevPage();
	}

	/**
	 * You will recall that the index pages have a left-most pointer that is
	 * followed whenever the search key value is less than the least key value
	 * in the index node. The previous page pointer is used to implement the
	 * left link. The function sets the left link.
	 * 
	 * @param left
	 *            the PageId of the left link you wish to set. Input parameter.
	 * @exception IOException
	 *                I/O errors
	 */
	protected void setLeftLink(PageId left) throws IOException
	{
		setPrevPage(left);
	}

	/*
	 * It is used in full delete @param key the key is used to search. Input
	 * parameter. @param pageNo It returns the pageno of the sibling. Input and
	 * Output parameter. @return 0 if no sibling; -1 if left sibling; 1 if right
	 * sibling. @exception IndexFullDeleteException delete failed
	 */

	public int getSibling(Key key, PageId pageNo) throws IndexFullDeleteException
	{

		try
		{
			if (getSlotCnt() == 0) // there is no sibling
				return 0;

			int i;
			KeyEntry entry;
			for (i = getSlotCnt() - 1; i >= 0; i--)
			{
				entry = new KeyEntry(getpage(), getSlotOffset(i),
						getSlotLength(i), keyType, INDEX);
				if (key.compareTo(entry.key) >= 0)
				{
					if (i != 0)
					{
						entry = new KeyEntry(getpage(),
								getSlotOffset(i - 1), getSlotLength(i - 1),
								keyType, INDEX);
						pageNo.pid = ((PageId) entry.getData()).pid;
						return -1; // left sibling
					} else
					{
						pageNo.pid = getLeftLink().pid;
						return -1; // left sibling
					}
				}
			}
			entry = new KeyEntry(getpage(), getSlotOffset(0),
					getSlotLength(0), keyType, INDEX);
			pageNo.pid = ((PageId) entry.getData()).pid;
			return 1; // right sibling
		} catch (Exception e)
		{
			throw new IndexFullDeleteException(e, "Get sibling failed");
		}
	} // end of getSibling

	/*
	 * find the position for old key by findKeyData, where the newKey will be
	 * returned . @newKey It will replace certain key in index page. Input
	 * parameter. @oldKey It helps us to find which key will be replaced by the
	 * newKey. Input parameter. @return false if no key was found; true if
	 * success. @exception IndexFullDeleteException delete failed
	 */

	boolean adjustKey(Key newKey, Key oldKey)
			throws IndexFullDeleteException
	{

		try
		{

			KeyEntry entry;
			entry = findKeyData(oldKey);
			if (entry == null)
				return false;

			RID rid = deleteKey(entry.key);
			if (rid == null)
				throw new IndexFullDeleteException(null, "Rid is null");

			rid = insertKey(newKey, (PageId) entry.getData());
			if (rid == null)
				throw new IndexFullDeleteException(null, "Rid is null");

			return true;
		} catch (Exception e)
		{
			throw new IndexFullDeleteException(e, "Adjust key failed");
		}
	} // end of adjustKey

	/*
	 * find entry for key by B+ tree algorithm, but entry.key may not equal
	 * KeyDataEntry.key returned. @param key input parameter. @return return
	 * that entry if found; otherwise return null; @exception
	 * IndexSearchException index search failed
	 * 
	 */
	KeyEntry findKeyData(Key key) throws IndexSearchException
	{
		KeyEntry entry;

		try
		{

			for (int i = getSlotCnt() - 1; i >= 0; i--)
			{
				entry = new KeyEntry(getpage(), getSlotOffset(i),
						getSlotLength(i), keyType, INDEX);

				if (key.compareTo(entry.key) >= 0)
				{
					return entry;
				}
			}
			return null;
		} catch (Exception e)
		{
			throw new IndexSearchException(e, "finger key data failed");
		}
	} // end of findKeyData

	/*
	 * find a key by B++ algorithm, but returned key may not equal the key
	 * passed in. @param key input parameter. @return return that key if found;
	 * otherwise return null; @exception IndexSearchException index search
	 * failed
	 * 
	 */
	public Key findKey(Key key) throws IndexSearchException
	{
		return findKeyData(key).key;
	}

};
