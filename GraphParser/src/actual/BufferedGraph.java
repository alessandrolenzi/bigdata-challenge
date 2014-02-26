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
import com.google.common.base.Predicates;


import common.Arc;
import common.Graph;
import common.Node;
import common.exceptions.ArcNotFound;
import common.exceptions.NodeNotFound;
import common.exceptions.UnvalidArc;
import customreader.BufferEndedException;
import customreader.CustomReader;

public class BufferedGraph<N extends Node<A>, A extends Arc> extends Graph<N, A> {
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
	private Predicate<? super A> arc_predicate = null;
	
	public BufferedGraph(String str, long max_memory, Class<? extends N> node_class, Class<? extends A> arc_class) throws NoSuchMethodException, SecurityException, IOException {
		file_name = str;
		NConstructor = node_class.getConstructor(int.class);
		AConstructor = arc_class.getConstructor(int.class, int.class, double.class);
		nodes = new TreeMap<Integer, N>();
		bufferMemory = max_memory;
		/** Initialize the reader*/
		reader = new CustomReader(str, 1024*4096, 1024, new char[]{'\n', ':', '\t'});
		/** Initialize the index to access the disk */
		index = new DiskIndex();
		index.put(0, 0L);
		index.put(Integer.MAX_VALUE, reader.fileLength());
	}
	
	
	
	public void load() throws IOException, UnvalidFileFormatException {load(true);}
	
	private boolean newpageBeginning = true;
	
	public void load(boolean readFollowing) throws IOException, UnvalidFileFormatException {
		try {			
			while(!reader.fileEnded()  && usedMemory < bufferMemory) {
				String nodeId = reader.nextToken().trim();
				if (newpageBeginning) {
					newpageBeginning = false;
					index.put(Integer.parseInt(nodeId), Math.max(0l,reader.currentPosition()-1));
				}
				parseNode(nodeId, readFollowing);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc){ 
			//This isn't supposed to happen.
			exc.printStackTrace();
			System.exit(1);
		} catch (BufferEndedException e) {
			//Loading finished correctly, won't load anything else.
			return;
		}
	}
	
	private void parseNode(String nodeId, boolean readFollowing) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnvalidFileFormatException, IOException, BufferEndedException{
		long initialUsedMemory = usedMemory;
		N node = null;
		boolean failing = false;
		try {
			node = this.nodeFactory(Integer.parseInt(nodeId.trim()));
			while(!reader.lineEnded()) { /** EOL => Node Ended*/
				int dest_id=0; double weight=0;
				boolean which = false; 
				try {
					String d = reader.nextToken();
					dest_id = Integer.parseInt(d);
					which = true;
					weight = Double.parseDouble(reader.nextToken());					
				} catch (BufferEndedException buffer_ended) {
					if(!readFollowing) throw buffer_ended; //Don't load anything else: if the node reading is uncomplete isn't a problem.
					reader.loadNext();
					newpageBeginning = true;
					/** Restore normality starting from the next cycle*/
					if(which) {//exception has been thrown while getting the arc weight
						/** TODO: Not very clean, maybe edit the "exception throwing point" detection mechanism*/
						weight = Double.parseDouble(reader.nextToken());
					} else {
						dest_id = Integer.parseInt(reader.nextToken());
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
			failing = true;
			throw new UnvalidFileFormatException("Unvalid file format");
		} catch(BufferEndedException ended) {
			failing = true;
			return;
		} finally {
			if(failing) {
				usedMemory = initialUsedMemory;
				if(node != null) nodes.remove(node.getId());
			}
		}
	}
	/**Utility function to advance directly until newline when jumping.*/
	private void advanceUntilNL() throws BufferEndedException {	while (reader.getLastDelimiter()!='\n') reader.nextToken();}

	private N binaryFindNode(int node_id) throws NodeNotFound{
		Interval<Long> searchInterval = index.getDiskInterval(node_id);		
		long beginning = searchInterval.beginning;
		long end = searchInterval.end;
		long mid = (beginning+end)>>1; 
			boolean found = false;
			boolean indexPosition = true;
			long oldmid = Integer.MIN_VALUE;
			try {
				while(!found) {
					/** Check failure in search procedure*/
					if (mid == oldmid) break;
					oldmid = mid;
					
					/**Load at chosen position; skip uncomplete data*/
					reader.loadAt(mid); this.advanceUntilNL();
					/** Position of the first node id readed in this block*/
					long curPos = reader.currentPosition();
					String foundNode = reader.nextToken();
					int foundNodeId = Integer.parseInt(foundNode);
					/** Add this node to the index*/
					if(indexPosition) index.put(foundNodeId, Math.max(0l, curPos-1));
					if (foundNodeId > node_id){ /** Go searching left*/
						end = mid;
						mid = (beginning+end)>>1;
						continue;
					}
					int curNodeID = Integer.MIN_VALUE;
					boolean loading = false;
					if (foundNodeId == node_id) { /**First loaded node is the first found! Lucky!*/
						found = true;
						loading = true;
						curNodeID = foundNodeId;
						newpageBeginning = false;
						/** In this case we're willing to get the full node & also to get the subsequent ones in the next part.*/
						parseNode(foundNode, true);
					} else	this.advanceUntilNL(); //This is done just to "align" with the subsequent cycle.	
				while (!reader.bufferFinished() && usedMemory <= bufferMemory) {
					long currentPosition = reader.currentPosition();
					curNodeID = Integer.parseInt(reader.nextToken());
					/** If the found node is at the beginning of a new buffer portion, load it into the index*/
					if (newpageBeginning && indexPosition) index.put(curNodeID, currentPosition -1);
					/** If the node found has an id less than the searched one, go on*/
					if (curNodeID < node_id) {this.advanceUntilNL(); continue;}
					/** Found!*/
					if (curNodeID == node_id) {loading = true; found = true;}
					/** Save on disk the nodes following the one just searched*/
					if (loading) 
						/** We want to fetch the next block only if the searched node is "involved".*/
							parseNode(curNodeID+"", (curNodeID == node_id));
				}
				
				if (found) return nodes.get(node_id);
				
				if (curNodeID < node_id) {
					beginning = mid;
					mid = (beginning+end)>>1;
					continue;
				}
			}
			//if not found...
			throw new NodeNotFound("Node not found");
		} catch(BufferEndedException | IOException unvalid_position) {
			//unvalid_position.printStackTrace();
			throw new NodeNotFound("Error.");
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException
				| UnvalidFileFormatException e) {
			throw new NodeNotFound("Error.");
		}
	}
	
	@Override
	public N getNode(int node_id) throws NodeNotFound {
		N retrieved = nodes.get(node_id);
		if (retrieved != null) return retrieved;
		return binaryFindNode(node_id);
	}


	@Override
	public Set<A> getAllOutgoingArcs(int node_id) throws NodeNotFound {
		N node = this.getNode(node_id);
		return node.getArcs(arc_predicate);
	}

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
	public void cutArcs(Predicate<A> p) {
		arc_predicate = (arc_predicate == null) ? p : Predicates.and(arc_predicate, p);
	}
	/** Factories */
	
	private A arcFactory(int id, int dest, double weight) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return AConstructor.newInstance(id, dest, weight);
	}

	private N nodeFactory(int i) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnvalidFileFormatException{
		return NConstructor.newInstance(i);

	}
	

}
