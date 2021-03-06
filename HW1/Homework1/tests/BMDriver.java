package tests;

import bufmgr.BufMgr;
import diskmgr.Page;
import exceptions.*;
import global.*;

import java.io.IOException;

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
	 * Add your own test here.
	 * 
	 * @return whether test1 has passed
	 */
	public boolean test1() throws IOException, InvalidFrameNumberException, DiskMgrException, PagePinnedException, HashOperationException, BufferPoolExceededException, HashEntryNotFoundException, PageNotReadException, ReplacerException, PageUnpinnedException, BufMgrException, InvalidBufferException {
		System.out.println("invoking test1");

        int numFrames = SystemDefs.JavabaseBM.getNumUnpinnedBuffers() + 1;
        System.out.println("numFrames in buffer pool: " + numFrames);
        
		Page myPage = new Page();
		System.out.println("Newing Page.");
		PageId myPageId = SystemDefs.JavabaseBM.newPage(myPage, numFrames);
        System.out.println("new page created with pagdId " + myPageId);

        System.out.println("Unpinning");
        SystemDefs.JavabaseBM.unpinPage(myPageId, false);

        for (int i = 0; i < numFrames; i++) {
            PageId localPgId = new PageId(myPageId.getPid() + i);
//            System.out.println("Testing i = " + i + " PageId: " + localPgId.getPid());
            SystemDefs.JavabaseBM.pinPage(localPgId, myPage, true);

            Convert.setIntValue(i, 0, myPage.getpage());

            SystemDefs.JavabaseBM.unpinPage(localPgId, true);

//            System.out.println("attempting to read " + localPgId);
            SystemDefs.JavabaseBM.pinPage(localPgId, myPage, false);
//            System.out.println("data has read: " + Convert.getIntValue(0, myPage.getpage()));
            SystemDefs.JavabaseBM.unpinPage(localPgId, true);
            SystemDefs.JavabaseBM.freePage(localPgId);
        }
        System.out.println("test 1 has passed");
        return true;
	}

	/**
	 * Add your own test here.
	 * 
	 * @return whether test2 has passed
	 */
	public boolean ztest2()
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
        int numPages = SystemDefs.JavabaseBM.getNumUnpinnedBuffers();
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
                SystemDefs.JavabaseBM.pinPage(pid, pg, true);
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


    /**
     * overrides the test2 function in TestDriver.  It tests whether illeagal
     * operation can be caught.
     *
     * @return whether test2 has passed
     */
    public boolean test2a () {

        System.out.print("\n  Test 2 exercises some illegal buffer " +
                "manager operations:\n");

        // We choose this number to ensure that pinning this number of buffers
        // should fail.
        int numPages = SystemDefs.JavabaseBM.getNumUnpinnedBuffers() + 1;
        Page pg = new Page ();
        PageId pid, lastPid;
        PageId firstPid = new PageId();
        boolean status = OK;

        System.out.print("  - Try to pin more pages than there are frames\n");
        try {
            firstPid = SystemDefs.JavabaseBM.newPage( pg, numPages );
        }
        catch (Exception e) {
            System.err.print("*** Could not allocate " + numPages);
            System.err.print (" new pages in the database.\n");
            e.printStackTrace();
            return false;
        }

        pid = new PageId();
        lastPid = new PageId();

        // First pin enough pages that there is no more room.
        for ( pid.pid=firstPid.pid+1, lastPid.pid=firstPid.pid+numPages-1;
              status == OK && pid.pid < lastPid.pid;
              pid.pid = pid.pid + 1 ) {

            try {
                SystemDefs.JavabaseBM.pinPage( pid, pg, /*emptyPage:*/ false );
            }
            catch (Exception e) {
                status = FAIL;
                System.err.print("*** Could not pin new page "+pid.pid+"\n");
                e.printStackTrace();
            }
        }

        // Make sure the buffer manager thinks there's no more room.
        if ( status == OK  &&  SystemDefs.JavabaseBM.getNumUnpinnedBuffers() != 0 ) {
            status = FAIL;
            System.err.print ("*** The buffer manager thinks it has " +
                    SystemDefs.JavabaseBM.getNumUnpinnedBuffers()
                    + " available frames,\n" +
                    "    but it should have none.\n");
        }

        // Now pin that last page, and make sure it fails.
        if ( status == OK ) {
            try {
                SystemDefs.JavabaseBM.pinPage( lastPid, pg, /*emptyPage:*/ false );
            }
            catch (ChainException e) {
                status = checkException (e, BufferPoolExceededException.class);
//                status = checkException (e, "exceptions.BufferPoolExceededException");
                if (status == FAIL) {
                    System.err.print("*** Pinning too many pages\n");
                    System.out.println ("  --> Failed as expected \n");
                }
            }
            catch (Exception e) {e.printStackTrace();}

            if (status == OK) {
                status = FAIL;
                System.err.print ("The expected exception was not thrown\n");
            }
            else {
                status = OK;
            }
        }

        if ( status == OK ) {
            try {
                SystemDefs.JavabaseBM.pinPage( firstPid, pg, /*emptyPage:*/ false );
            }
            catch (Exception e) {
                status = FAIL;
                System.err.print("*** Could not acquire a second pin on a page\n");
                e.printStackTrace();
            }

            if ( status == OK ) {
                System.out.print ("  - Try to free a doubly-pinned page\n");
                try {
                    SystemDefs.JavabaseBM.freePage( firstPid );
                }

                catch (ChainException e) {
                    status = checkException (e, PagePinnedException.class);
//                    status = checkException (e, "exceptions.PagePinnedException");

                    if (status == FAIL) {
                        System.err.print("*** Freeing a pinned page\n");
                        System.out.println ("  --> Failed as expected \n");
                    }
                }

                catch (Exception e) {
                    e.printStackTrace();
                }

                if (status == OK) {
                    status = FAIL;
                    System.err.print ("The expected exception was not thrown\n");
                }
                else {
                    status = OK;
                }
            }

            if (status == OK) {
                try {
                    SystemDefs.JavabaseBM.unpinPage( firstPid, false );
                }
                catch (Exception e) {
                    status = FAIL;
                    e.printStackTrace();
                }
            }
        }

        if ( status == OK ) {
            System.out.print ("  - Try to unpin a page not in the buffer pool\n");
            try {
                SystemDefs.JavabaseBM.unpinPage( lastPid, false );
            }
            catch (ChainException e) {
                status = checkException (e, HashEntryNotFoundException.class);
//                status = checkException (e, "bufmgr.HashEntryNotFoundException");

                if (status == FAIL) {
                    System.err.print("*** Unpinning a page not in the buffer pool\n");
                    System.out.println ("  --> Failed as expected \n");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (status == OK) {
                status = FAIL;
                System.err.print ("The expected exception was not thrown\n");
            }
            else {
                status = OK;
            }
        }

        for ( pid.pid = firstPid.pid; pid.pid < lastPid.pid;
              pid.pid = pid.pid + 1 ) {
            try {
                SystemDefs.JavabaseBM.unpinPage(pid, false);
                SystemDefs.JavabaseBM.freePage( pid );
            }
            catch (Exception e) {
                status = FAIL;
                System.err.print ("*** Error freeing page " + pid.pid + "\n");
                e.printStackTrace();
            }
        }

        if ( status == OK )
            System.out.print ("  Test 2 completed successfully.\n");

        return status;
    }


    /**
     * overrides the test3 function in TestDriver.  It exercises some of the internal
     * of the buffer manager
     *
     * @return whether test3 has passed
     */
    public boolean test3a () {

        System.out.print("\n  Test 3 exercises some of the internals " +
                "of the buffer manager\n");

        int index;
        int numPages = NUMBUF + 10;
        Page pg = new Page();
        PageId pid = new PageId();
        PageId [] pids = new PageId[numPages];
        boolean status = OK;

        System.out.print("  - Allocate and dirty some new pages, one at " +
                "a time, and leave some pinned\n");

        for ( index=0; status == OK && index < numPages; ++index ) {
            try {
                pid = SystemDefs.JavabaseBM.newPage( pg, 1 );
            }
            catch (Exception e) {
                status = FAIL;
                System.err.print ("*** Could not allocate new page number "
                        + index+1 + "\n");
                e.printStackTrace();
            }

            if ( status == OK )
                pids[index] = pid;

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
                    e.printStackTrace();
                }

                // Leave the page pinned if it equals 12 mod 20.  This is a
                // random number based loosely on a bug report.
                if (status == OK) {
                    if ( pid.pid % 20 != 12 ) {
                        try {
                            SystemDefs.JavabaseBM.unpinPage( pid, /*dirty:*/ true );
                        }
                        catch (Exception e) {
                            status = FAIL;
                            System.err.print("*** Could not unpin dirty page "+pid.pid+"\n");
                        }
                    }
                }
            }
        }

        if ( status == OK ) {
            System.out.print ("  - Read the pages\n");

            for ( index=0; status == OK && index < numPages; ++index ) {
                pid = pids[index];
                try {
                    SystemDefs.JavabaseBM.pinPage( pid, pg, false);
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

                    if ( data != pid.pid + 99999 ) {
                        status = FAIL;
                        System.err.print("*** Read wrong data back from page "+pid.pid+"\n");
                    }
                }

                if ( status == OK ) {
                    try {
                        SystemDefs.JavabaseBM.unpinPage( pid, true ); //might not be dirty
                    }
                    catch (Exception e)  {
                        status = FAIL;
                        System.err.print("*** Could not unpin page "+pid.pid+"\n");
                        e.printStackTrace();
                    }
                }

                if ( status == OK && (pid.pid % 20 == 12) ) {
                    try {
                        SystemDefs.JavabaseBM.unpinPage( pid, /*dirty:*/ true );
                    }
                    catch (Exception e)  {
                        status = FAIL;
                        System.err.print("*** Could not unpin page "+pid.pid+"\n");
                        e.printStackTrace();
                    }
                }
            }
        }

        if ( status == OK )
            System.out.print("  Test 3 completed successfully.\n");

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
