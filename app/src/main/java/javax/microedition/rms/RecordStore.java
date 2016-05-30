/*
 * Copyright 2012 Kulikov Dmitriy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.microedition.rms;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.microedition.util.ContextHolder;

import android.content.Context;

public class RecordStore
{
	private static final String INDEX_SUFFIX = ".idx";
	private static final String RECORD_SUFFIX = ".dat";
	private static final String SEPARATOR = "-";
	
	private String recordStoreName;
	
	private int nextRecordID;
	private ArrayList<Integer> ids;
	
	private static String getFileNamePrefix(String recordStoreName)
	{
		return recordStoreName + SEPARATOR + Integer.toHexString(recordStoreName.hashCode()).toUpperCase();
	}
	
	private String getIndexFileName()
	{
		return getFileNamePrefix(recordStoreName) + INDEX_SUFFIX;
	}
	
	private String getRecordFileName(int id)
	{
		return getFileNamePrefix(recordStoreName) + SEPARATOR + id + RECORD_SUFFIX;
	}
	
	private RecordStore(String recordStoreName, boolean createIfNecessary) throws RecordStoreException
	{
		this.recordStoreName = recordStoreName;
		
		try
		{
			DataInputStream dis = new DataInputStream(ContextHolder.getContext().openFileInput(getIndexFileName()));
			
			int numRecords = dis.readInt();
			nextRecordID = dis.readInt();
			
			ids = new ArrayList(numRecords);
			
			for(int i = 0; i < numRecords; i++)
			{
				ids.add(dis.readInt());
			}
			
			dis.close();
		}
		catch(FileNotFoundException e)
		{
			if(createIfNecessary)
			{
				nextRecordID = 1;
				ids = new ArrayList();
			}
			else
			{
				throw new RecordStoreNotFoundException(e);
			}
		}
		catch(IOException e)
		{
			throw new RecordStoreException(e);
		}
	}
	
	public static RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws RecordStoreException
	{
		return new RecordStore(recordStoreName, createIfNecessary);
	}
	
	public void closeRecordStore() throws RecordStoreException
	{
		try
		{
			DataOutputStream dos = new DataOutputStream(ContextHolder.getContext().openFileOutput(getIndexFileName(), Context.MODE_PRIVATE));
			
			dos.writeInt(ids.size());
			dos.writeInt(nextRecordID);
			
			for(Integer id : ids)
			{
				dos.writeInt(id);
			}
			
			dos.close();
		}
		catch(IOException e)
		{
			throw new RecordStoreException(e);
		}
	}
	
	public void deleteRecord(int recordID)
	{
		ContextHolder.getContext().deleteFile(getRecordFileName(recordID));
		ids.remove(Integer.valueOf(recordID));
	}
	
	public static void deleteRecordStore(String recordStoreName) throws RecordStoreException
	{
		String[] files = ContextHolder.getContext().fileList();
		String prefix = getFileNamePrefix(recordStoreName);
		
		for(String filename : files)
		{
			if(filename.startsWith(prefix))
			{
				ContextHolder.getContext().deleteFile(filename);
			}
		}
	}
	
	public int addRecord(byte[] data, int offset, int numBytes) throws RecordStoreException
	{
		try
		{
			OutputStream os = ContextHolder.getContext().openFileOutput(getRecordFileName(nextRecordID), Context.MODE_PRIVATE);
			
			if(data != null)
			{
				os.write(data, offset, numBytes);
			}
			
			os.close();
		}
		catch(IOException e)
		{
			throw new RecordStoreException(e);
		}
		
		ids.add(nextRecordID);
		
		return nextRecordID++;
	}
	
	public int getNextRecordID()
	{
		return nextRecordID;
	}
	
	public int getNumRecords()
	{
		return ids.size();
	}
	
	public void setRecord(int recordID, byte[] data, int offset, int numBytes) throws RecordStoreException
	{
		if(!ids.contains(Integer.valueOf(recordID)))
		{
			throw new InvalidRecordIDException();
		}
		
		try
		{
			OutputStream os = ContextHolder.getContext().openFileOutput(getRecordFileName(recordID), Context.MODE_PRIVATE);
			os.write(data, offset, numBytes);
			os.close();
		}
		catch(IOException e)
		{
			throw new RecordStoreException(e);
		}
	}
	
	public byte[] getRecord(int recordID) throws RecordStoreException
	{
		try
		{
			InputStream is = ContextHolder.getContext().openFileInput(getRecordFileName(recordID));
			ByteArrayOutputStream out = new ByteArrayOutputStream(is.available());
			
			byte[] buf = new byte[1024];
			
			while(is.available() > 0)
			{
				out.write(buf, 0, is.read(buf));
			}
			
			is.close();
			
			return out.toByteArray();
		}
		catch(FileNotFoundException e)
		{
			throw new InvalidRecordIDException(e);
		}
		catch(IOException e)
		{
			throw new RecordStoreException(e);
		}
	}
	
	public static String[] listRecordStores()
	{
		String[] files = ContextHolder.getContext().fileList();
		ArrayList<String> res = new ArrayList();
		int index;
		
		for(String filename : files)
		{
			if(filename.endsWith(INDEX_SUFFIX))
			{
				index = filename.lastIndexOf(SEPARATOR);
				res.add(filename.substring(0, index));
			}
		}
		
		return res.toArray(new String[0]);
	}
	
	public RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comparator, boolean keepUpdated) throws RecordStoreException
	{
		if(filter != null || comparator != null)
		{
			throw new RecordStoreException("not implemented; do you really need SUCH a complicated request?");
		}
		
		return new RecordEnumeration(this, ids);
	}
	
	public int getSizeAvailable()
	{
		return (int)ContextHolder.getContext().getFilesDir().getUsableSpace();
	}
}