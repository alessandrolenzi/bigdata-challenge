package actual;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;

public class CustomReader implements Iterator<String>{
	private RandomAccessFile file;
	private long bufferMemory;
	private long usedMemory = 0;
	private MappedByteBuffer buffer;
	private int currentPageSize = 4096;
	private int pageSize = 4096;
	private int pageNumber = 1;
	private byte[] page;
	private int lastDelimiterPosition = 0;
	private int start = 0;
	StringBuffer line = new StringBuffer();
	private char lastDelimiter;
	private boolean ended = false;
	private long lastScannedPosition = 0L;
	
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
		lastScannedPosition = bufferMemory = usedMemory = Math.min(buffer_memory, file.length());
		pageNumber = page_number;
		page = new byte[pageNumber*pageSize];
		
	}
	
	/** Fetches a new page.*/
	private void fetchPage() throws BufferUnderflowException{
		if (buffer.remaining() < pageNumber*pageSize) {
			currentPageSize = buffer.remaining();
			buffer.get(page, 0, currentPageSize);
		} else { buffer.get(page); currentPageSize = pageSize;}
		lastDelimiterPosition = 0;
		start = 0;
	}
	
	/**
	 * Returns the next string 
	 * @return the token
	 * @throws BufferEndedException in case the buffer is ended
	 */
	public String nextToken() throws BufferEndedException {
		boolean found = false;
		/* Automatically elaborate new chunks from the buffer and return tokens.*/
		if (lastDelimiterPosition >= currentPageSize) {
			try {
				fetchPage();
			} catch(BufferUnderflowException exc) {throw new BufferEndedException("");}
		}
		
		/** TODO: add parametric delimiters.*/
		for (; lastDelimiterPosition < currentPageSize; lastDelimiterPosition++) {
			char c = (char) page[lastDelimiterPosition];
			if (c == '\t' || c == ':' || c == '\n'){
				found = true; lastDelimiter=c;break;
			}
		}
		
		if (found) {
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
		/**Manage EOF*/
		try {
			if (file.length() <= lastScannedPosition) ended = true;
		} catch (IOException e) {
			throw new BufferEndedException("Reached end of file"); //create new exception for this case
		}
		if (line.length() > 0 && ended){ String lastRes = line.toString(); line = new StringBuffer(); return lastRes;}
		//Finally:
		throw new BufferEndedException(line.toString());
	}	
	/** Return a number of pages in string format*/
	public String nextPages() throws BufferEndedException {
		fetchPage();
		return new String(page);
	}
	/**
	 * Unloads from the memory the previous buffer, then loads page_number(passed in as parameter in the constructor) pages of 4K in memory.
	 * NextToken can be called again
	 * @throws IOException if the file is finished. 
	 */
	public void loadNext() throws IOException {
		if (file.length() <= lastScannedPosition) throw new IOException();
		buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, lastScannedPosition, Math.min(pageNumber*pageSize, file.length() - lastScannedPosition));
		lastScannedPosition += Math.min(pageNumber*pageSize, file.length() - lastScannedPosition);
		start = 0; lastDelimiterPosition = 0;
		fetchPage();
	}
	
	public char getLastDelimiter(){return lastDelimiter;}
	
	public boolean lineEnded() {return lastDelimiter == '\n';}
	
	public boolean bufferFinished(){return buffer.hasRemaining();}
	
	public boolean fileEnded(){return ended && !buffer.hasRemaining();}

	@Override
	public boolean hasNext() {
		return fileEnded();
	}
	
	@Override
	public String next() {
		try {
			return nextToken();
		} catch (BufferEndedException e) {
			return null;
		}
	}

	/**
	 * The remove operation is not supported.
	 */
	@Override
	public void remove() {//Do nothing
		
	}
	
	public static void main(String args[]) throws BufferEndedException {
		CustomReader cr = null;;
		try {
			cr = new CustomReader(args[0], 4096, 1, new char[]{'\n'});
		
			while(!cr.fileEnded()) {
				try {
					System.out.println(cr.nextToken()+cr.getLastDelimiter());
				} catch (BufferEndedException e) {
					if(!cr.fileEnded())cr.loadNext();
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
