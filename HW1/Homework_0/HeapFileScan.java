

import global.Convert;
import global.DummyBufMgr;
import global.GlobalConst;
import global.RID;
import global.Minibase;
import heap.Heapfile;
import heap.Scan;
import heap.Tuple;

/**
 * Note that in JAVA, methods can't be overridden to be more private. Therefore,
 * the declaration of all private functions are now declared protected as
 * opposed to the private type in C++.
 */

public class HeapFileScan implements GlobalConst
{
	private final static boolean OK = true;
	private final static boolean FAIL = false;
	private final static int reclen = 32;

	public static void main(String argv[])
	{
		HeapFileScan hd = new HeapFileScan(); // big enough for >1 data page

		Runtime.getRuntime().exit(0);
	}
	
	
	/**
	 * HeapFileScan constructor
	 * 
	 * Initializes system variables, creates some files, and scans them to 
	 * make sure they have the correct number of records.
	 */

	public HeapFileScan()
	{
		//initialize the database
		String filePrefix = "HeapFile-" + System.getProperty("user.name");
		String dbpath = filePrefix + ".minibase-db";
		//new SystemDefs(dbpath, 100, 100, "Clock");
		try {
			Minibase.initBufMgr(new DummyBufMgr());
//			SystemDefs.initBufMgr(new BufMgr(100,"bufmgr.Clock"));
		} catch(Exception ire)
		{
			ire.printStackTrace();
			System.exit(1);
		}
		
		Minibase.initDiskMgr(dbpath, 100);
		
		createFile("file_100",100);
		createFile("file_2000",2000);
		
		scanFile("file_100",100);
		scanFile("file_2000",2000);
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
	 * scanFile - scans a file created by createFile, and checks to make sure
	 * that all records are the correct size, and have the correct sequence data
	 * that was set by createFile.
	 * 
	 * @param fileName - the name of the file
	 * @param expectedNumRec - how many records should be in the file
	 * @return a boolean value indicating if the scan was successful
	 */
	public boolean scanFile(String fileName, int expectedNumRec)
	{
		boolean status = OK;

		try
		{
			// In general, a sequential scan won't be in the same order as the
			// insertions. However, we're inserting fixed-length records here, and
			// in this case the scan must return the insertion order.

			System.out.println("  Scan file " + fileName + " expecting: " + 
					expectedNumRec + " records");

			Heapfile f = new Heapfile(fileName);
			Scan scan = f.openScan();
			RID rid = new RID();
			scan = f.openScan();

			int len, i = 0;
			DummyRecord rec = null;
			Tuple tuple = null;

			boolean done = false;
			while (!done && status == OK)
			{
				tuple = scan.getNext(rid);
				if (tuple == null)
				{
					done = true;
					break;
				}

				rec = new DummyRecord(tuple);

				len = tuple.getLength();
				if (len != reclen)
				{
					System.err.println("*** Record " + i
							+ " had unexpected length " + len + "\n");
					status = FAIL;
					break;
				} else if (Minibase.JavabaseBM.getNumUnpinnedBuffers() == Minibase.JavabaseBM
						.getNumBuffers())
				{
					System.err.println("On record " + i + ":\n");
					System.err
					.println("*** The heap-file scan has not left its "
							+ "page pinned\n");
					status = FAIL;
					break;
				}
				String name = ("record" + i);

				if ((rec.ival != i) || (rec.fval != (float) i * 2.5)
						|| (!name.equals(rec.name)))
				{
					System.err.println("*** Record " + i
							+ " differs from what we inserted\n");
					System.err.println("rec.ival: " + rec.ival
							+ " should be " + i + "\n");
					System.err.println("rec.fval: " + rec.fval
							+ " should be " + (i * 2.5) + "\n");
					System.err.println("rec.name: " + rec.name
							+ " should be " + name + "\n");
					status = FAIL;
					break;
				}
				++i;
			}

			// If it gets here, then the scan should be completed
			if (status == OK)
			{
				if (i != (expectedNumRec))
				{
					status = FAIL;

					System.err.println("*** Scanned " + i
							+ " records instead of " + expectedNumRec + "\n");
				}
			}

			if (status == OK)
				System.out.println("  Scan completed successfully.\n");

		} catch (Exception e)
		{
			status = FAIL;
			System.err.println("*** Could not create heap file\n");
			e.printStackTrace();
			return(false);
		}

		return status;
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

