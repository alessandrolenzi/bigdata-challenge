package actual;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
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
	private int minLoadedId = Integer.MAX_VALUE;
	private int maxLoadedId = -1;
	private Constructor<? extends N> NConstructor;
	private Constructor<? extends A> AConstructor;
	private long bufferMemory = 0;
	private long usedMemory = 0;
	private CustomReader reader;
	public BufferedGraph(String str, long max_memory, Class<? extends N> node_class, Class<? extends A> arc_class) throws NoSuchMethodException, SecurityException, IOException {
		file_name = str;
		NConstructor = node_class.getConstructor(int.class);
		AConstructor = arc_class.getConstructor(int.class, int.class, double.class);
		nodes = new TreeMap<Integer, N>();
		bufferMemory = max_memory;
		reader = new CustomReader(str, max_memory, 4, new char[]{'\n', ':', '\t'});				
	}
	
	
	public void load() throws IOException, UnvalidFileFormatException {
		try {
			int count = 0;
			String nodeId;
			try {
				while((nodeId=reader.nextToken()) != null && usedMemory < bufferMemory) {
					parseNode(nodeId);
					count++;
					return;
				}
			} catch (BufferEndedException e) {
				e.printStackTrace();
				System.exit(0);
			}
			
			System.out.println(count+"/"+usedMemory);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc){ 
			exc.printStackTrace();
		}
	}
	
	private void parseNode(String nodeId) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnvalidFileFormatException, IOException, BufferEndedException{
		try {
			N node = this.nodeFactory(Integer.parseInt("0"));
			
			while(reader.getLastDelimiter() != '\n') {
				int dest_id=0; double weight=0;
				boolean which = false; 
				try {
					String d = reader.nextToken();
					dest_id = Integer.parseInt(d);
					which = true;
					weight = Double.parseDouble(reader.nextToken());					
				} catch (BufferEndedException buffer_ended) {
					reader.loadNext();
					if(which) {//exception has been thrown while getting the arc weight
						weight = Double.parseDouble(reader.nextToken());
					} else {
						String str = reader.nextToken();
						dest_id = Integer.parseInt(str);
						weight = Double.parseDouble(reader.nextToken());
					}
				} 
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


	@Override
	public N getNode(int node_id) throws NodeNotFound {
	/*	if (node_id > 0 && node_id >= minLoadedId && node_id <= maxLoadedId) {
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
		*/
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
	/** Factories */
	
	private A arcFactory(int id, int dest, double weight) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return AConstructor.newInstance(id, dest, weight);
	}

	private N nodeFactory(int i) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnvalidFileFormatException{
		return NConstructor.newInstance(i);

	}

}
