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

package javax.microedition.io.file;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.StreamConnection;
import javax.microedition.util.StringPattern;

public class FileConnection implements StreamConnection
{
	public static final String READ = "r";
	public static final String READ_WRITE = "rw";
	
	protected File file;
	protected String mode;
	
	protected RandomAccessFile raf;
	
	protected class InternalInputStream extends InputStream
	{
		protected long markedpos = 0;
		
		public int available() throws IOException
		{
			return (int)(raf.length() - raf.getFilePointer());
		}
		
		public void close()
		{
		}
		
		public void mark(int readLimit)
		{
			try
			{
				markedpos = raf.getFilePointer();
			}
			catch(IOException e)
			{
			}
		}
		
		public boolean markSupported()
		{
			return true;
		}
		
		public int read() throws IOException
		{
			return raf.read();
		}
		
		public int read(byte[] b) throws IOException
		{
			return raf.read(b, 0, b.length);
		}
		
		public int read(byte[] b, int off, int len) throws IOException
		{
			return raf.read(b, off, len);
		}
		
		public void reset() throws IOException
		{
			raf.seek(markedpos);
		}
		
		public long skip(long n) throws IOException
		{
			return raf.skipBytes((int)n);
		}
	}
	
	protected class InternalOutputStream extends OutputStream
	{
		public void close()
		{
		}
		
		public void flush()
		{
		}
		
		public void write(int b) throws IOException
		{
			raf.write(b);
		}
		
		public void write(byte[] b) throws IOException
		{
			raf.write(b, 0, b.length);
		}
		
		public void write(byte[] b, int off, int len) throws IOException
		{
			raf.write(b, off, len);
		}
	}
	
	public FileConnection(File file, String mode)
	{
		this.file = file;
		this.mode = mode;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public RandomAccessFile getRandomAccessFile() throws IOException
	{
		if(raf == null)
		{
			raf = new RandomAccessFile(file, mode);
		}
		
		return raf;
	}
	
	public void close() throws IOException
	{
		if(raf != null)
		{
			raf.close();
			raf = null;
		}
	}
	
	public boolean isOpen()
	{
		return true;
	}
	
	public InputStream openInputStream() throws IOException
	{
		getRandomAccessFile();
		return new InternalInputStream();
	}
	
	public DataInputStream openDataInputStream() throws IOException
	{
		return new DataInputStream(openInputStream());
	}
	
	public OutputStream openOutputStream() throws IOException
	{
		getRandomAccessFile();
		return new InternalOutputStream();
	}
	
	public DataOutputStream openDataOutputStream() throws IOException
	{
		return new DataOutputStream(openOutputStream());
	}
	
	public OutputStream openOutputStream(long byteOffset) throws IOException
	{
		getRandomAccessFile().seek(byteOffset);
		return new InternalOutputStream();
	}
	
	public long totalSize()
	{
		return file.getTotalSpace();
	}
	
	public long availableSize()
	{
		return file.getFreeSpace();
	}
	
	public long usedSize()
	{
		return file.getTotalSpace() - file.getFreeSpace();
	}
	
	public long directorySize(boolean includeSubDirs) throws IOException
	{
		File[] files = file.listFiles();
		long size = 0;
		
		for(File subfile : files)
		{
			if(subfile.isFile())
			{
				size += subfile.length();
			}
			else if(includeSubDirs)
			{
				try
				{
					size += (new FileConnection(subfile, READ)).directorySize(true);
				}
				catch(IOException e)
				{
				}
			}
		}
		
		return size;
	}
	
	public long fileSize() throws IOException
	{
		return file.length();
	}
	
	public boolean canRead()
	{
		return file.canRead();
	}
	
	public boolean canWrite()
	{
		return file.canWrite();
	}
	
	public boolean isHidden()
	{
		return file.isHidden();
	}
	
	public void setReadable(boolean readable) throws IOException
	{
		if(!file.setReadable(readable))
		{
			throw new IOException();
		}
	}
	
	public void setWritable(boolean writable) throws IOException
	{
		if(!file.setWritable(writable))
		{
			throw new IOException();
		}
	}
	
	public void setHidden(boolean hidden) throws IOException
	{
		if(hidden)
		{
			throw new IOException("to hide a file, add a dot at the beginning of it's name");
		}
		else
		{
			throw new IOException("to unhide a file, remove the dot at the beginning of it's name");
		}
	}
	
	public Enumeration list() throws IOException
	{
		return createFileEnumeration(file.listFiles(), false);
	}
	
	public Enumeration list(String filter, boolean includeHidden) throws IOException
	{
		final StringPattern pattern = new StringPattern(filter, false);
		
		FilenameFilter namefilter = new FilenameFilter()
		{
			public boolean accept(File dir, String filename)
			{
				return pattern.matchesWith(filename);
			}
		};
		
		return createFileEnumeration(file.listFiles(namefilter), includeHidden);
	}
	
	public static Enumeration createFileEnumeration(File[] files, boolean includeHidden)
	{
		if(files != null)
		{
			Vector res = new Vector(files.length);
			String filename;
			
			for(int i = 0; i < files.length; i++)
			{
				filename = files[i].getName();
				
				if(includeHidden || !filename.startsWith("."))
				{
					if(files[i].isDirectory())
					{
						res.addElement(filename + "/");
					}
					else
					{
						res.addElement(filename);
					}
				}
			}
			
			return res.elements();
		}
		else
		{
			return (new Vector()).elements();
		}
	}
	
	public void create() throws IOException
	{
		if(!file.createNewFile())
		{
			throw new IOException();
		}
	}
	
	public void mkdir() throws IOException
	{
		if(!file.mkdirs())
		{
			throw new IOException();
		}
	}
	
	public boolean exists()
	{
		return file.exists();
	}
	
	public boolean isDirectory()
	{
		return file.isDirectory();
	}
	
	public void delete() throws IOException
	{
		if(!file.delete())
		{
			throw new IOException();
		}
	}
	
	public void rename(String newName) throws IOException
	{
		if(!file.renameTo(new File(file.getParentFile(), newName)))
		{
			throw new IOException();
		}
	}
	
	public void truncate(long byteOffset) throws IOException
	{
		getRandomAccessFile().setLength(byteOffset);
	}
	
	public void setFileConnection(String fileName) throws IOException
	{
		close();
		file = new File(file, fileName);
	}
	
	public String getName()
	{
		String name = file.getName();
		
		if(file.isDirectory() && !name.endsWith("/"))
		{
			return name + "/";
		}
		else
		{
			return name;
		}
	}
	
	public String getPath()
	{
		String path = file.getParent();
		
		if(path != null)
		{
			if(!path.endsWith("/"))
			{
				path += "/";
			}
			
			return FileSystemRegistry.androidFileToMIDP(path);
		}
		else
		{
			return null;
		}
	}
	
	public String getURL()
	{
		return "file://" + getPath() + getName();
	}
	
	public long lastModified()
	{
		return file.lastModified();
	}
}