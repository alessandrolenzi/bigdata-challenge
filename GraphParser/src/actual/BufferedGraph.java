package actual;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import net.sourceforge.sizeof.SizeOf;

import com.google.common.base.Predicate;

import common.Arc;
import common.Graph;
import common.Node;
import common.exceptions.ArcNotFound;
import common.exceptions.NodeNotFound;
import common.exceptions.UnvalidArc;

public class BufferedGraph<N extends Node, A extends Arc> extends Graph<N, A> {
	protected String file_name;
	protected boolean ended = false;
	protected Map<Integer, N> nodes;
	protected RandomAccessFile file; 
	private long bufferMemory = 1024L*1024L*100L;
	private long usedMemory = 0;
	private int minLoadedId = Integer.MAX_VALUE;
	private int maxLoadedId = -1;
	private MappedByteBuffer buffer;
	private Constructor<? extends N> NConstructor;
	private Constructor<? extends A> AConstructor;
	
	public BufferedGraph(String str, long max_memory, Class<? extends N> node_class, Class<? extends A> arc_class) throws FileNotFoundException, NoSuchMethodException, SecurityException {
		file_name = str;
		file = new RandomAccessFile(str, "r");
		bufferMemory = max_memory;
		//scanner = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(str))));
		NConstructor = node_class.getConstructor(int.class);
		AConstructor = arc_class.getConstructor(int.class, int.class, double.class);
		nodes = new TreeMap<Integer, N>();
	}
	
	private int page_size = 4096;
	private byte[] page = new byte[4096];
	private int lastDelimiterPosition = 0;
	private int start = 0;
	StringBuffer line = new StringBuffer();
	private boolean newLine = false;
	private boolean newLineDelimiter(){return newLine;}
	private boolean seeking = false;
	private String nextToken() {
		boolean found = false;
		if (seeking)
			try {
				System.out.println(file.getFilePointer());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (start == 0) 
			try {
				if (buffer.remaining() < 4096) {
					page_size = buffer.remaining();
					buffer.get(page, 0, page_size);
				} else buffer.get(page);
			} catch(BufferUnderflowException exc) {return null;}
		
		for (; lastDelimiterPosition < page_size; lastDelimiterPosition++) {
			char c = (char) page[lastDelimiterPosition];
			if (c == '\t' || c == ':'){found = true; newLine=false; break;}
			if (c == '\n'){found = true; newLine=true; break;}
		}
		if (found) {
			line.append(new String(Arrays.copyOfRange(page, start, lastDelimiterPosition)));
			start = lastDelimiterPosition+1;
			lastDelimiterPosition++;
			String newline = line.toString();
			line = new StringBuffer();
			return newline;
		}
		line.append(new String(Arrays.copyOfRange(page, start, lastDelimiterPosition)));
		start = 0; lastDelimiterPosition = 0;
		return (buffer.hasRemaining()) ? nextToken() : null;
	}
	
	public void load() throws IOException, UnvalidFileFormatException {
		try {
			buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, Math.min((long) (bufferMemory*1.2), file.length()));
			int count = 0;
			String nodeId;
			while((nodeId=nextToken()) != null && usedMemory < bufferMemory) {
				parseNode(nodeId);
				count++;
			}
			
			System.out.println(count+"/"+usedMemory);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc){ 
			exc.printStackTrace();
		}
	}
	
	private void parseNode(String nodeId) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnvalidFileFormatException{
		try {
			N node = this.nodeFactory(Integer.parseInt(nodeId));
			while(!newLineDelimiter()) {
				int dest_id = Integer.parseInt(nextToken());
				double weight = Double.parseDouble(nextToken());
				A arc = this.arcFactory(node.getId(), dest_id, weight);
				node.putArc(arc);
				usedMemory += SizeOf.sizeOf(arc);
			}
			nodes.put(node.getId(), node);
			usedMemory+=SizeOf.sizeOf(node);
			minLoadedId = Math.min(minLoadedId, node.getId());
			maxLoadedId = Math.max(maxLoadedId, node.getId());
		} catch(NumberFormatException exc) {
			throw new UnvalidFileFormatException("Unvalid file format");
		}
	}

	private A arcFactory(int id, int dest, double weight) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return AConstructor.newInstance(id, dest, weight);
	}

	private N nodeFactory(int i) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnvalidFileFormatException{
		return NConstructor.newInstance(i);

	}

	@Override
	public N getNode(int node_id) throws NodeNotFound {
		if (node_id > 0 && node_id >= minLoadedId && node_id <= maxLoadedId) {
			N retrieved = nodes.get(node_id);
			if (retrieved != null) return retrieved;
			throw new NodeNotFound("Node with id "+node_id+" not found");
		}
		try {
			long beginning = file.getFilePointer();
			long end = file.length();
			long midPos;
			boolean found = false;
			boolean right = false;
			while (!found && file.getFilePointer() < file.length()) {
				midPos = (beginning + end)/2;
				file.seek(midPos);
				buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, midPos-1024L*1024L, 2*1024L*1024L);
				newLine = false;
				start = 0; lastDelimiterPosition = 0;
				String str;
				while((str = nextToken()) != null && !found && buffer.hasRemaining()) {
					
					while(!newLineDelimiter() && buffer.hasRemaining()) nextToken(); //Scan until the id of the node is found.
					if(!buffer.hasRemaining()) break;
					int firstFound = Integer.parseInt(nextToken()); 
					if (firstFound < node_id) {right = true;}
					else if(firstFound > node_id){right = false;}
					else {
						found = true;
						parseNode(firstFound+"");
					}
				}
				if (!found && right) {beginning = midPos;}
				else if(!found && !right) {end = midPos;}
			}
			if (found) return nodes.get(node_id);
			
		} catch (IOException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | UnvalidFileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}


	@Override
	public Set<A> getAllOutgoingArcs(int node_id) throws NodeNotFound {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void finalize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void putNode(N node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<A> getAllOutgoingArcs(N n) throws NodeNotFound {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public A getArc(int start_node, int dest_node) throws ArcNotFound {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public A getArc(N start_node, N dest_node) throws ArcNotFound {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addArc(A arc) throws UnvalidArc {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Graph<N, A> cutArcs(Predicate<A> p) {
		// TODO Auto-generated method stub
		return null;
	}

}
