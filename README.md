Content of this folder
GraphParser : contains the BufferedGraph to read the graph and a custom reader exploiting java.nio to read tokens faster from the files. The bufferedGraph buffers and caches some parts of the graph in memory, thus allowing to have faster accesses. Contains
=== DiskIndex === 
An indexer of the file in which the graph representation is stored, used to restrict the interval on which the binary search is performed when a (uncached) node is required
=== actual ===
BufferedGraph implementation
=== common ===
classes used by other packages, i.e. abstract Graph class, node class, arc class
=== customreader ===
implementation of a simple tokenizer with java.nio
=== stronglyconnectedcomponents ===
tarjan algorithm implementation for the search of strongly connected components
