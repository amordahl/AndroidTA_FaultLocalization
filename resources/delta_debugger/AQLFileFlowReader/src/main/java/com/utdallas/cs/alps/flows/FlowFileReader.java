package com.utdallas.cs.alps.flows;

import java.io.File;
import java.util.Iterator;

public interface FlowFileReader {
    /**
     * Processes the given file and returns an iterator to the flows in it.
     * @param flowFile The file to be read in.
     * @return An iterator pointing to the list of flows that were parsed.
     * possibly from a byte stream or character stream
     * supplied by the application.
     */
    Iterator<Flow> getFlows(File flowFile);

}