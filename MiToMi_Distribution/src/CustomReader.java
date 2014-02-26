

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

public class CustomReader implements Iterator<String>{
	
	private RandomAccessFile file;
	private long bufferMemory = 0;
	private long usedMemory = 0;
	private MappedByteBuffer buffer;
	private int currentPageSize = 4096;
	private long currentStart = 0;
	private int pageSize = 4096;
	private int pageNumber = 1;
	private byte[] page;
	private int lastDelimiterPosition = 0;
	private int start = 0;
	StringBuffer line = new StringBuffer();
	private char lastDelimiter;
	private boolean ended = false;
	private long lastScannedPosition = 0L;
	private byte[] delimiters;
	
	/**
	 * CustomReader: a customized scanner exploiting the MemoryByteBuffer from java.nio
	 * @param file_name the file to be readed
	 * @param buffer_memory size of the file to be mapped in memory
	 * @param page_number number of pages (whose size is 4KB) to load on subsequent requests.
	 * @param delimiters array of delimiters for the tokens
	 * @throws IOException
	 */
	
	public CustomReader(String file_name, long buffer_memory, int page_number, char[] delimiters) throws IOException {
		file = new RandomAccessFile(file_name, "r");
		bufferMemory = buffer_memory;
		/**	Fetch first page */
		buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, Math.min(buffer_memory, file.length()));
		lastScannedPosition = bufferMemory = usedMemory = Math.min(bufferMemory, file.length());
		pageNumber = page_number;
		page = new byte[pageSize];
		this.setDelimiters(delimiters);
	}
	
	private boolean isDelimiter(byte b) {
		for(byte delimiter: delimiters) {
			if (b == delimiter) return true;
		}
		return false;
	}
	
	public void setDelimiters(char[] delimiters) {
		this.delimiters = new String(delimiters).getBytes();
	}
	
	/** Fetches a new page.*/
	private void fetchPage() throws BufferUnderflowException{
		if (buffer.remaining() < pageSize) {
			currentPageSize = buffer.remaining();
			buffer.get(page, 0, currentPageSize);
		} else { buffer.get(page); currentPageSize = pageSize;}
		lastDelimiterPosition = 0;
		start = 0;
	}
	
	
	private String foundToken() {
		
		/** Add eventually dangling parts, re-allign the starting and ending position for the
		 * scanner, reset the remainder, return the retrieved token.
		 * */
		line.append(new String(Arrays.copyOfRange(page, start, lastDelimiterPosition)));
		start = lastDelimiterPosition+1; lastDelimiterPosition++;
		String newline = line.toString();
		line = new StringBuffer();
		return newline;
	}
		
	/**
	 * Returns the next string 
	 * @return the token
	 * @throws BufferEndedException in case the buffer is ended
	 */
	public String nextToken() throws BufferEndedException {
		
		//boolean found = false;
		//if (closed) return null;
		/* Automatically elaborate new chunks from the buffer and return tokens.*/
		if (lastDelimiterPosition >= currentPageSize) {
			try {
				fetchPage();
			} catch(BufferUnderflowException exc) {throw new BufferEndedException("");}
		}

		/** TODO: add parametric delimiters.*/
		for (; lastDelimiterPosition < currentPageSize; lastDelimiterPosition++) {
			if (isDelimiter(page[lastDelimiterPosition])){
				lastDelimiter=(char)page[lastDelimiterPosition]; return foundToken();
			}
		}

		/*
		if (found) {
			/** Add eventually dangling parts, re-allign the starting and ending position for the
			 * scanner, reset the remainder, return the retrieved token.
			 * 
			line.append(new String(Arrays.copyOfRange(page, start, lastDelimiterPosition)));
			start = lastDelimiterPosition+1; lastDelimiterPosition++;
			String newline = line.toString();
			line = new StringBuffer();
			return newline;
		}*/
		/**
		 * This occurs when the "analyzed" page ends before finding a delimiter token. 
		 * The unreturned part is saved somewhere in memory, start is left untouched, lastDelimiter
		 * is after the end of the page, so that the fact that there's need to load another chunk
		 * can be recognized.
		 */
		line.append(new String(Arrays.copyOfRange(page, start, lastDelimiterPosition)));
		start = lastDelimiterPosition; lastDelimiterPosition++;
		/*Buffer is not ended: nextToken procedure will take care of the analysis of the remaining part*/
		if (buffer.hasRemaining()) return nextToken();
		/*Buffer is ended in this case.*/
		start = 0; lastDelimiterPosition = 0;

		if (line.length() > 0 && ended){ String lastRes = line.toString(); line = new StringBuffer(); return lastRes;}
		//Finally:
		throw new BufferEndedException(line.toString());
	}	
	/** Return a number of pages in string format*/
	public String nextPages() throws BufferEndedException {
		if (closed) return null;
		fetchPage();
		return new String(page);
	}
	/**
	 * Unloads from the memory the previous buffer, then loads page_number(passed in as parameter in the constructor) pages of 4K in memory.
	 * NextToken can be called again
	 * @throws IOException if the file is finished. 
	 */
	public void loadNext() throws IOException {
		if (closed) throw new IOException("Buffer closed.");
		if (file.length() <= lastScannedPosition) 
			if(!ended){ended=true; return;}
			else throw new IOException("Finished");
		long toExpand = Math.min(pageNumber*pageSize, file.length() - lastScannedPosition);
		buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, lastScannedPosition, toExpand);
		lastScannedPosition += toExpand;
		start = 0; lastDelimiterPosition = 0;
		fetchPage();
		
	}
	
	public char getLastDelimiter(){return lastDelimiter;}
	
	public boolean lineEnded() {return lastDelimiter == '\n';}
	
	public boolean bufferFinished(){return !buffer.hasRemaining();}
	
	public long usedMemory(){if(closed) return 0L; return usedMemory;}
	
	
	public boolean fileEnded() throws IOException{return ended && !buffer.hasRemaining() && file.length() <= lastScannedPosition;}
	
	@Override
	public boolean hasNext() {
		try {
			return fileEnded();
		} catch (IOException e) {
			return true;
		}
	}
	
	@Override
	public String next() {
		try {
			return nextToken();
		} catch (BufferEndedException e) {
			try {
				loadNext();
				return nextToken();
			} catch (IOException | BufferEndedException e1) {
				return null;
			}
		}
	}

	/**
	 * The remove operation is not supported.
	 */
	@Override
	public void remove() {/** Does nothing*/}
	
	/**
	 * Restarts the reader from the given position.
	 * @param position the offset in byte from which the reading re-starts.
	 * @throws IOException in case the position is not available
	 */
	public void loadAt(long position) throws IOException {
		if(position < 0) position = 0;
		lastScannedPosition = position;
		ended = false;
		lastDelimiter = '\0';
		loadNext();
		
	}
	
	public long fileLength() {
		try {
			return file.length();
		} catch (IOException e) {
			return 0;
		}
	}
	
	public long currentPosition() {
		return lastScannedPosition-buffer.capacity()+lastDelimiterPosition;
	}
	
	
	/**
	 * Frees the memory. 
	 */	
	private boolean closed;
	public void close() {
		closed = true;
		buffer.clear();
		buffer = null;
		page = null;
		start = 0; lastDelimiterPosition = 0; 
		lastScannedPosition=0;
	}
	
	public void finalize() {
		close();
	}
	
	public static void main(String args[]) throws BufferEndedException, IOException {
		CustomReader cr = null;
		if (args.length > 1) {
			int i = 0;
			StringBuffer strbuf = new StringBuffer();
		try {
			
			cr = new CustomReader(args[0], 1024*1024*10L, 1024, new char[]{'\n'});
		
			while(!cr.fileEnded()) {
				try {
					//System.out.print(cr.nextToken());
					//System.out.print(cr.lastDelimiter);
					strbuf.append(cr.nextToken()); strbuf.append(cr.getLastDelimiter());
					i++;
					if (i % 1000000 == 0) {String line = strbuf.toString(); System.out.print(line); strbuf = new StringBuffer();}
				}	catch (BufferEndedException e) {
					cr.loadNext();
				}
			}
			if (strbuf.length() > 0) System.out.print(strbuf.toString());
			//System.out.println("OK");
			
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
			return;
		} 
		Scanner sc = null;
		try {
			
			sc = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(args[0]))));
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		while (sc.hasNext()) {
			sc.next();
		}
	}
}
