package tests;

import exceptions.*;
import global.Convert;
import global.GlobalConst;
import global.PageId;
import global.SystemDefs;
import global.TestDriver;
import heap.Heapfile;
import heap.Tuple;
import java.io.IOException;

import bufmgr.BufMgr;

import diskmgr.Page;
import heap.Tuple;

// #TODO TEST CASES
/*
1. create a new page, pin it
2. modify data in the buffer.
3. unpin the page. check if dirty, flush to disk
4. if requested page not exist in buffer, fetch from disk store into frame?
5.
 */
// need to create a new page, then pin / un pin
//

public class BMDriver extends TestDriver implements GlobalConst
{
    private final static int reclen = 32;

    // private int TRUE = 1;
	// private int FALSE = 0;
	private boolean OK = true;

	private boolean FAIL = false;

	/**
	 * BMDriver Constructor, inherited from TestDriver
	 */
	public BMDriver()
	{
		super("Buffer Manager");
	}

	public void initBeforeTests()
	{
		try {
			SystemDefs.initBufMgr(new BufMgr());
		} catch(Exception ire)
		{
			ire.printStackTrace();
			System.exit(1);
		}
		
		SystemDefs.initDiskMgr("BMDriver", NUMBUF+20);
	}


	/**
	 * createFile - creates a file of a given name with a given number of records.
	 * @param fileName - the name of the file
	 * @param numRec - how many records does the file hold
	 * @return a boolean value indicating whether the create was successful
	 */

    public boolean createFile(String fileName, int numRec)
    {
        boolean status = OK;

        try
        {
            System.out.println("\n  Insert and scan " + numRec +
                    " fixed-size records\n");
            //RID rid = null;
            Heapfile f = null;

            System.out.println("  - Create a heap file\n");
            f = new Heapfile(fileName);

            System.out.println("  - Add " + numRec + " records to the file\n");
            for (int i = 0; (i < numRec) && (status == OK); i++)
            {
                // fixed length record
                DummyRecord rec = new DummyRecord(reclen);
                rec.ival = i;
                rec.fval = (float) (i * 2.5);
                rec.name = "record" + i;

				/*rid =*/ f.insertRecord(rec.toByteArray());
            }

            if (f.getRecCnt() != numRec)
            {
                status = FAIL;
                System.err.println("*** File reports " + f.getRecCnt()
                        + " records, not " + numRec + "\n");
                return(false);
            }

        } catch (Exception e)
        {
            status = FAIL;
            System.err.println("*** Could not create heap file\n");
            e.printStackTrace();
            return(false);
        }

        return status;
    }



    /**
	 * Add your own test here.
	 * 
	 * @return whether test1 has passed
	 */
	public boolean test1() throws IOException, InvalidFrameNumberException, DiskMgrException, PagePinnedException, HashOperationException, BufferPoolExceededException, HashEntryNotFoundException, PageNotReadException, ReplacerException, PageUnpinnedException, BufMgrException {

        System.out.println("invoking test1");

		Page myPage = new Page();
		PageId myPageId = SystemDefs.JavabaseBM.newPage(myPage,60);
        System.out.println("new page created " + myPageId);

        System.out.println("Unpinning");
        SystemDefs.JavabaseBM.unpinPage(myPageId, false);


        SystemDefs.JavabaseBM.pinPage(myPageId, myPage, true);


        Convert.setIntValue(5, 0, myPage.getpage());
        SystemDefs.JavabaseBM.unpinPage(myPageId, true);

        for (int i=0; i < 40; i++)
        {
            PageId localPgId = new PageId(myPageId.getPid()+i+1);
            SystemDefs.JavabaseBM.pinPage(localPgId, myPage, true);

            Convert.setIntValue(i, 0, myPage.getpage());

            SystemDefs.JavabaseBM.unpinPage(localPgId, true);
        }


        System.out.println("attempting to read " + myPageId);
        SystemDefs.JavabaseBM.pinPage(myPageId, myPage, false);
        System.out.println(Convert.getIntValue(0,myPage.getpage()));

//        for (int i = 0; i < 50; i ++)
//        {
//            PageId localPgId = new PageId(myPageId.getPid()+i+1);
//            SystemDefs.JavabaseBM.pinPage(localPgId, myPage, false);
//            System.out.println(Convert.getIntValue(0,myPage.getpage()));
//            SystemDefs.JavabaseBM.unpinPage(localPgId, false);
//
//        }
		return true;
	}

	/**
	 * Add your own test here.
	 * 
	 * @return whether test2 has passed
	 */
	public boolean test2()
	{
		
		System.out.print("\n  Test 2 is not implemented. \n ");
		
		return true;
	}


	/*
	THESE ARE THE TESTS HE PROVIDED.
	MODIFIED TO WORK WITH OUR CODE.
	 */
    /**
     * overrides the test1 function in TestDriver.  It tests some
     * simple normal buffer manager operations.
     *
     * @return whether test1 has passed
     */
    public boolean test1a () {

        System.out.print("\n  Test 1 does a simple test of normal buffer ");
        System.out.print("manager operations:\n");

        // We choose this number to ensure that at least one page will have to be
        // written during this test.
        boolean status = OK;
        int numPages = SystemDefs.JavabaseBM.getNumUnpinnedBuffers() + 1;
//        int numPages = Minibase.BufferManager.getNumUnpinned() + 1;
        Page pg = new Page();
        PageId pid;
        PageId lastPid;
        PageId firstPid = new PageId();

        System.out.print("  - Allocate a bunch of new pages\n");

        try {
            firstPid = SystemDefs.JavabaseBM.newPage( pg, numPages );
//            firstPid = Minibase.BufferManager.newPage( pg, numPages );

        }
        catch (Exception e) {
            System.err.print("*** Could not allocate " + numPages);
            System.err.print (" new pages in the database.\n");
            e.printStackTrace();
            return false;
        }


        // Unpin that first page... to simplify our loop.
        try {
            SystemDefs.JavabaseBM.unpinPage(firstPid, false /*not dirty*/);
//            Minibase.BufferManager.unpinPage(firstPid, false /*not dirty*/);
        }
        catch (Exception e) {
            System.err.print("*** Could not unpin the first new page.\n");
            e.printStackTrace();
            status = FAIL;
        }

        System.out.print("  - Write something on each one\n");

        pid = new PageId();
        lastPid = new PageId();

        for ( pid.pid = firstPid.pid, lastPid.pid = pid.pid+numPages;
              status == OK && pid.pid < lastPid.pid;
              pid.pid = pid.pid + 1 ) {

            try {
                SystemDefs.JavabaseBM.unpinPage(pid, false);
//                Minibase.BufferManager.pinPage( pid, pg, /*emptyPage:*/ false);
            }
            catch (Exception e) {
                status = FAIL;
                System.err.print("*** Could not pin new page "+pid.pid+"\n");
                e.printStackTrace();
            }

            if ( status == OK ) {

                // Copy the page number + 99999 onto each page.  It seems
                // unlikely that this bit pattern would show up there by
                // coincidence.
                int data = pid.pid + 99999;

                try {
                    Convert.setIntValue (data, 0, pg.getpage());
                }
                catch (IOException e) {
                    System.err.print ("*** Convert value failed\n");
                    status = FAIL;
                }

                if (status == OK) {
                    try {
                        SystemDefs.JavabaseBM.unpinPage(pid, true);
//                        Minibase.BufferManager.unpinPage( pid, /*dirty:*/ true );
                    }
                    catch (Exception e)  {
                        status = FAIL;
                        System.err.print("*** Could not unpin dirty page "
                                + pid.pid + "\n");
                        e.printStackTrace();
                    }
                }
            }
        }

        if ( status == OK )
            System.out.print ("  - Read that something back from each one\n" +
                    "   (because we're buffering, this is where "  +
                    "most of the writes happen)\n");

        for (pid.pid=firstPid.pid; status==OK && pid.pid<lastPid.pid;
             pid.pid = pid.pid + 1) {

            try {
                SystemDefs.JavabaseBM.pinPage(pid, pg, false);
//                Minibase.BufferManager.pinPage( pid, pg, /*emptyPage:*/ false );
            }
            catch (Exception e) {
                status = FAIL;
                System.err.print("*** Could not pin page " + pid.pid + "\n");
                e.printStackTrace();
            }

            if ( status == OK ) {

                int data = 0;

                try {
                    data = Convert.getIntValue (0, pg.getpage());
                }
                catch (IOException e) {
                    System.err.print ("*** Convert value failed \n");
                    status = FAIL;
                }

                if (status == OK) {
                    if (data != (pid.pid) + 99999) {
                        status = FAIL;
                        System.err.print ("*** Read wrong data back from page "
                                + pid.pid + "\n");
                    }
                }

                if (status == OK) {
                    try {
                        SystemDefs.JavabaseBM.unpinPage(pid, true);
//                        Minibase.BufferManager.unpinPage( pid, /*dirty:*/ true );
                    }
                    catch (Exception e)  {
                        status = FAIL;
                        System.err.print("*** Could not unpin page " + pid.pid + "\n");
                        e.printStackTrace();
                    }
                }
            }
        }

        if (status == OK)
            System.out.print ("  - Free the pages again\n");

        for ( pid.pid=firstPid.pid; pid.pid < lastPid.pid;
              pid.pid = pid.pid + 1) {

            try {
                SystemDefs.JavabaseBM.freePage(pid);
//                Minibase.BufferManager.freePage( pid );
            }
            catch (Exception e) {
                status = FAIL;
                System.err.print("*** Error freeing page " + pid.pid + "\n");
                e.printStackTrace();
            }

        }

        if ( status == OK )
            System.out.print("  Test 1 completed successfully.\n");

        return status;
    }




    public static void main(String argv[])
	{

		BMDriver bmt = new BMDriver();
		
		boolean dbstatus;

		dbstatus = bmt.runTests();

		if (dbstatus != true)
		{
			System.out.println("Error encountered during buffer manager tests:\n");
			System.out.flush();
			Runtime.getRuntime().exit(1);
		}

		System.out.println("Done. Exiting...");
		Runtime.getRuntime().exit(0);
	}
}


//This is added to substitute the struct construct in C++
class DummyRecord
{

    // content of the record
    public int ival;
    public float fval;
    public String name;

    // length under control
    private int reclen;
    private byte[] data;

    /**
     * Default constructor
     */
    DummyRecord() {}

    /**
     * another constructor
     */
    public DummyRecord(int _reclen)
    {
        setRecLen(_reclen);
        data = new byte[_reclen];
    }

    /**
     * constructor: convert a byte array to DummyRecord object.
     *
     * @param arecord
     *            a byte array which represents the DummyRecord object
     */
    public DummyRecord(byte[] arecord) throws java.io.IOException
    {
        setIntRec(arecord);
        setFloRec(arecord);
        setStrRec(arecord);
        data = arecord;
        setRecLen(name.length());
    }

    /**
     * constructor: translate a tuple to a DummyRecord object it will make a
     * copy of the data in the tuple
     *
     * @param atuple:
     *            the input tuple
     */
    public DummyRecord(Tuple _atuple) throws java.io.IOException
    {
        data = new byte[_atuple.getLength()];
        data = _atuple.getTupleByteArrayCopy();
        setRecLen(_atuple.getLength());

        setIntRec(data);
        setFloRec(data);
        setStrRec(data);

    }

    /**
     * convert this class objcet to a byte array this is used when you want to
     * write this object to a byte array
     */
    public byte[] toByteArray() throws java.io.IOException
    {
        // data = new byte[reclen];
        Convert.setIntValue(ival, 0, data);
        Convert.setFloValue(fval, 4, data);
        Convert.setStrValue(name, 8, data);
        return data;
    }

    /**
     * get the integer value out of the byte array and set it to the int value
     * of the DummyRecord object
     */
    public void setIntRec(byte[] _data) throws java.io.IOException
    {
        ival = Convert.getIntValue(0, _data);
    }

    /**
     * get the float value out of the byte array and set it to the float value
     * of the DummyRecord object
     */
    public void setFloRec(byte[] _data) throws java.io.IOException
    {
        fval = Convert.getFloValue(4, _data);
    }

    /**
     * get the String value out of the byte array and set it to the float value
     * of the HTDummyRecorHT object
     */
    public void setStrRec(byte[] _data) throws java.io.IOException
    {
        // System.out.println("reclne= "+reclen);
        // System.out.println("data size "+_data.size());
        name = Convert.getStrValue(8, _data, reclen - 8);
    }

    // Other access methods to the size of the String field and
    // the size of the record
    public void setRecLen(int size)
    {
        reclen = size;
    }

    public int getRecLength()
    {
        return reclen;
    }
}
