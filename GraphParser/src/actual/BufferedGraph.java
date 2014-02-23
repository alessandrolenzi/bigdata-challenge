package actual;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.sourceforge.sizeof.SizeOf;

import DiskIndex.DiskIndex;
import DiskIndex.DiskIndex.Interval;

import com.google.common.base.Predicate;

import common.Arc;
import common.Graph;
import common.Node;
import common.exceptions.ArcNotFound;
import common.exceptions.NodeNotFound;
import common.exceptions.UnvalidArc;
import customreader.BufferEndedException;
import customreader.CustomReader;

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
	private DiskIndex index;
	
	public BufferedGraph(String str, long max_memory, Class<? extends N> node_class, Class<? extends A> arc_class) throws NoSuchMethodException, SecurityException, IOException {
		file_name = str;
		NConstructor = node_class.getConstructor(int.class);
		AConstructor = arc_class.getConstructor(int.class, int.class, double.class);
		nodes = new TreeMap<Integer, N>();
		bufferMemory = max_memory;
		reader = new CustomReader(str, 1024*4096, 1024, new char[]{'\n', ':', '\t'});
		index = new DiskIndex();
		index.put(0, 0L);
		index.put(Integer.MAX_VALUE, reader.fileLength());
	}
	
	private boolean newpageBeginning = true;
	
	public void load() throws IOException, UnvalidFileFormatException {
		String nodeId = "";
		long nodePosition = 0l;
		try {			
			try {
				while(!reader.fileEnded()  && usedMemory < bufferMemory) {
					//nodePosition = reader.currentPosition();
					nodeId = reader.nextToken();
					index.put(Integer.parseInt(nodeId.trim()), Math.max(0l,reader.currentPosition()-1));
					if (newpageBeginning) {
						newpageBeginning = false;
					}
					parseNode(nodeId, true);
				}
				
			} catch (BufferEndedException e) {
				//index.put(Integer.parseInt(nodeId.trim()), reader.currentPosition());
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc){ 
			exc.printStackTrace();
		}
		
	}
	
	private void parseNode(String nodeId, boolean b) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnvalidFileFormatException, IOException, BufferEndedException{
		try {
			N node = this.nodeFactory(Integer.parseInt(nodeId.trim()));
			
			while(reader.getLastDelimiter() != '\n') {
				int dest_id=0; double weight=0;
				boolean which = false; 
				try {
					String d = reader.nextToken();
					dest_id = Integer.parseInt(d);
					which = true;
					weight = Double.parseDouble(reader.nextToken());					
				} catch (BufferEndedException buffer_ended) {
					if(!b) return;
					reader.loadNext();
					newpageBeginning = true;
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
	
	private void advanceUntilNL() throws BufferEndedException {	while (reader.getLastDelimiter()!='\n') reader.nextToken();}

	@Override
	public N getNode(int node_id) throws NodeNotFound {
		N retrieved = nodes.get(node_id);
		if (retrieved != null) return retrieved;
		Interval<Long> searchInterval = index.getDiskInterval(node_id);		
		long beginning = searchInterval.beginning;
		long end = searchInterval.end;
		int prevn = index.floor(node_id);
		int nextn = index.ceiling(node_id);
		//Start a blinded binary search on the searchInterval on disk.
		long mid = (beginning+end)>>1 ;
		
		boolean found = false;
		boolean donotindex = false;
		long oldmid = -1000;
		try {
			while(!found) {
				if (mid == oldmid) throw new NodeNotFound("Node doesn't exist");
				oldmid = mid;
				//System.out.print("Searching in "+mid+",with b:"+beginning+"("+prevn+") and e:"+end+"("+nextn+")");
				reader.loadAt(mid);
				//Get id of the first node loaded here.
			
				this.advanceUntilNL();
				long curPos = reader.currentPosition();
				String foundNode = reader.nextToken();
				int foundNodeId = Integer.parseInt(foundNode);
				index.put(foundNodeId, Math.max(0l, curPos-1));
				if (foundNodeId > node_id){
					/*System.out.println("Search on the left");
					System.out.println("LastN:"+foundNodeId+";Beginning:"+beginning+"; END:"+end+"; SearchPoint:"+mid);
					try {
						System.in.read();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}*/
					end = mid;
					mid = (beginning+end)>>1;
					continue; //search on the left.
				}
				int curNodeID = Integer.MIN_VALUE;
				boolean loading = false;
				if (foundNodeId == node_id) {
					/*System.out.println("Immediately found!");*/
					found = true;
					loading = true;
					curNodeID = foundNodeId;
					try {
						parseNode(foundNode, false);
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException
							| InvocationTargetException
							| UnvalidFileFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new NodeNotFound("Error in processing");
					}
				} else {
					//System.out.println("Search right");
					this.advanceUntilNL();
				}/*		
				try {
					System.in.read();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
			//Scan the portion of the buffer, since now it is loaded.
		
			
			int count = 0;
			try{
			while (!reader.bufferFinished() && usedMemory <= 2*bufferMemory) {
				curNodeID = Integer.parseInt(reader.nextToken());
				if (curNodeID < node_id) {this.advanceUntilNL(); continue;}
				if (curNodeID == node_id) {loading = true; found = true;}
				if (loading) {count++;
					try {
						parseNode(curNodeID+"", false);
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException
							| InvocationTargetException
							| UnvalidFileFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}} catch(BufferEndedException not_found) {
				//System.out.println("All scanned without finding.");
			}
			if (found) return nodes.get(node_id);
			//Search right.
			if (curNodeID < node_id) {
				beginning = mid;
				mid = (beginning+end)>>1;
				//System.out.println("LastN:"+curNodeID+";Beginning:"+beginning+"; END:"+end+"; SearchPoint:"+mid);
				//System.out.println("Start searching at.."+mid);
				continue;
			}
		}
	} catch(BufferEndedException | IOException unvalid_position) {
		unvalid_position.printStackTrace();
		return null;
	}
	//1. load file
	//2. scan it
	//3. save beginning and end of lowest chunk inside the index, if they were not present.
	//4. if not found, goto 1
	
	/*	
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
	
	public void verifyFirst(long position) throws BufferEndedException, IOException {
		reader.loadAt(position);
		this.advanceUntilNL();
		String foundNode = reader.nextToken();
		int foundNodeId = Integer.parseInt(foundNode);
		System.out.println(foundNodeId);
	}

}
