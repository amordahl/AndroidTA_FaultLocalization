/*
  This file is part of Tamis.

  Tamis is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Tamis is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Tamis.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
  Created by Austin Mordahl, June 2020.
 */

package com.utdallas.cs.alps.flows;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * An XMLFLowFileReader for reading flows in the AQL format. For more information
 * about the AQL format, see the paper that introduced it:
 * Felix Pauck, Eric Bodden, and Heike Wehrheim. 2018.
 * <p>
 * Do Android taint analysis tools keep their promises?
 * In Proceedings of the 2018 26th ACM Joint Meeting on European Software
 * Engineering Conference and Symposium on the Foundations of Software Engineering
 * (ESEC/FSE 2018). Association for Computing Machinery, New York, NY, USA, 331–341.
 * DOI:https://doi.org/10.1145/3236024.3236029
 */
public class AQLFlowFileReader extends XMLFlowFileReader {
    private final Logger LOGGER = Logger.getLogger(XMLFlowFileReader.class.getName());
    private static File SCHEMA = null;
    private Violation thisViolation;
    @Override
    DefaultHandler getFlowHandler() {
        return new AQLFlowHandler();
    }
    public AQLFlowFileReader(String schemaFilePath){
        SCHEMA = Paths.get(schemaFilePath).toFile();
        thisViolation=new Violation();
    }
    /**
     * Returns the flows from the default handler
     * after it has processed the AQL flows file.
     *
     * @param dh The SAX handler; should be a subtype of DefaultHandler.
     * @return An iterator pointing to the flows discovered.
     */
    @Override
    Iterator<Flow> getFlowIterator(DefaultHandler dh) {
        // First, check that dh is actually an instance of AQLFlowHandler.
        if (!(dh instanceof AQLFlowHandler)) {
            throw new IllegalArgumentException("dh is not an instance of AQLFlowHandler!");
        } else {
            // Then, if the flows object is null, throw a state exception.
            AQLFlowHandler afh = (AQLFlowHandler) dh;
            if (afh.flows == null) {
                throw new IllegalStateException("getFlowIterator was called before an XML " +
                        "file was processed!");
            } else {
                // Otherwise, return the iterator from the flows object.
                return afh.flows.iterator();
            }
        }
    }


    public Violation getThisViolation(File file){
        Iterator<Flow> theseFlows = getFlows(file);
        ArrayList<Flow> flowList = new ArrayList<>();
        while(theseFlows.hasNext()){

            flowList.add(theseFlows.next());
            thisViolation.setApk(flowList.get(0).getApk());
        }
        thisViolation.setFlowList(flowList);
        return thisViolation;
    }
    /**
     * Reads the passed file and returns a list of flows. Validates flows with Felix Pauck's AQL-Answer schema.
     *
     * @param file The XML file holding the flows.
     * @return a iterator flows
     */
    @Override
    public Iterator<Flow> getFlows(File file) {
        try {
            return super.getFlows(file, SCHEMA);
        }
        catch (ParserConfigurationException parserConfigurationException) {
            LOGGER.severe("The parser could not be created successfully.");
            throw new RuntimeException(parserConfigurationException.getMessage());
        }
        catch (SAXException saxException) {
            LOGGER.severe("The SAX parser threw an error when setting up the schema or while performing parsing.");
            throw new RuntimeException(saxException.getMessage());
        }
        catch (IOException ioException) {
            LOGGER.severe("Something went wrong while parsing the file.");
            throw new RuntimeException(ioException);
        }
    }


    /**
     * A subtype of DefaultHandler for reading in XML files in AQL format.
     * <p>
     * Implemented this with help from various online resources,
     * such as Pankaj's Tutorial on SAX Parsers, accessed 2020-05-29
     * (https://www.journaldev.com/1198/java-sax-parser-example#sax-parser-methods-to-override),
     * and the Oracle tutorial "Parsing an XML File Using SAX", accessed 2020-05-29
     * (https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html)
     */
    private class AQLFlowHandler extends DefaultHandler {

        // Logger
        private final Logger LOGGER = Logger.getLogger(AQLFlowHandler.class.getName());

        // Container to hold the discovered flows.
        public ArrayList<Flow> flows;

        // The current flow we're working on
        private Flow currentFlow;

        // StringBuilder to hold the data from characters
        private StringBuilder data;

        // flag to indicate whether elements should be added as a source or sink.
        // turned on when we're processing a source, if false then we're processing
        // a sink.
        private boolean source_mode;

        // indicates whether to strip newlines
        private final boolean stripNewlines;

        public AQLFlowHandler(boolean stripNewlines) {
            super();
            this.stripNewlines = stripNewlines;
        }

        public AQLFlowHandler() {
            this(true);
        }

        /**
         * This method is triggered upon the event of an element starting.
         */
        @Override
        public void startDocument() {
            flows = new ArrayList<>();
            source_mode = false;
        }

        /**
         * Receive notification of the start of an element.
         *
         * <p>By default, do nothing.  Application writers may override this
         * method in a subclass to take specific actions at the start of
         * each element (such as allocating a new tree node or writing
         * output to a file).</p>
         *
         * @param uri        The Namespace URI, or the empty string if the
         *                   element has no Namespace URI or if Namespace
         *                   processing is not being performed.
         * @param localName  The local name (without prefix), or the
         *                   empty string if Namespace processing is not being
         *                   performed.
         * @param qName      The qualified name (with prefix), or the
         *                   empty string if qualified names are not available.
         * @param attributes The attributes attached to the element.  If
         *                   there are no attributes, it shall be an empty
         *                   Attributes object.
         * @throws org.xml.sax.SAXException Any SAX exception, possibly
         *                                  wrapping another exception.
         * @see org.xml.sax.ContentHandler#startElement
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // the only thing we need to do here is keep track of whether we're currently in source
            // or sink processing mode, and grab IDs from the flow header.

            // we also want to reset data, or else the first element will have all of the junk
            // from the beginning of the file.
            data = null;
            switch (qName.toLowerCase()) {
                case "flow":
                    // If this is a flow, then start a new flow.
                    currentFlow = new Flow();
                    // if these attributes don't exist, getValue returns null, which is fine
                    currentFlow.setId(attributes.getValue("id"));
                    currentFlow.setGeneratingConfig(attributes.getValue("generating_config"));
                    break;
                case "reference":
                    // Check whether this is a source or sink.
                    // sinks are annotated with "to"
                    // sources are annotated with "from"
                    // references should not have a type that's not "from" or "to"
                    switch (attributes.getValue("type").toLowerCase()) {
                        case "to":
                            source_mode = false;
                            break;
                        case "from":
                            source_mode = true;
                            break;
                        default:
                            throw new SAXNotRecognizedException("reference element had a value different" +
                                    "from 'from' or 'to'");
                    }
                    break;
                case "violation":
                    thisViolation.setConfig1(attributes.getValue("config1"));
                    thisViolation.setConfig2(attributes.getValue("config2"));
                    thisViolation.setType(attributes.getValue("type"));
                    break;
            }
        }

        /**
         * Receive notification of character data inside an element.
         * <p>
         * Simply appends the characters to the data stringbuffer, which is handled in
         * endElement. If stripNewlines is true, newlines are replaced with spaces.
         * Otherwise. They will be preserved.
         *
         * @param ch     The characters.
         * @param start  The start position in the character array.
         * @param length The number of characters to use from the
         *               character array.
         * @see ContentHandler#characters
         */
        @Override
        public void characters(char[] ch, int start, int length) {
            // append the characters to the data string buffer.
            if (data == null) data = new StringBuilder();
            // strip new lines if desired.
            if (this.stripNewlines) {
                for (int i = 0; i < ch.length; i++) {
                    if (ch[i] == '\n') {
                        ch[i] = ' ';
                    }
                }
            }
            for (int i = 0; i < length; i++) {
                data.append(ch[start + i]);
            }
        }

        /**
         * Receive notification of the end of an element.
         * <p>
         * Depending on the element that is ending, takes various different actions.
         * But basically, it consumes the data buffer depending on the element and resets it to null.
         * If this is the end of the flow, we add it to the flows list.
         *
         * @param uri       The Namespace URI, or the empty string if the
         *                  element has no Namespace URI or if Namespace
         *                  processing is not being performed.
         * @param localName The local name (without prefix), or the
         *                  empty string if Namespace processing is not being
         *                  performed.
         * @param qName     The qualified name (with prefix), or the
         *                  empty string if qualified names are not available.
         * @see org.xml.sax.ContentHandler#endElement
         */
        @Override
        public void endElement(String uri, String localName, String qName) {
            String dataString = cleanText(data == null ? " " : data.toString());
            switch (qName.toLowerCase()) {
                case "flow":
                    // Add the flow to the flows list, and nullify it so we don't modify it further.
                    flows.add(currentFlow);
                    currentFlow = null;
                    break;
                case "statementgeneric":
                    if (source_mode) {
                        currentFlow.getSource().setStatement(dataString);
                    } else {
                        currentFlow.getSink().setStatement(dataString);
                    }
                    break;
                case "statementfull":
                    if (source_mode) {
                        currentFlow.getSource().setStatementFull(dataString);
                    } else {
                        currentFlow.getSink().setStatementFull(dataString);
                    }
                    break;
                case "method":
                    if (source_mode) {
                        currentFlow.getSource().setMethod(dataString);
                    } else {
                        currentFlow.getSink().setMethod(dataString);
                    }
                    break;
                case "classname":
                    if (source_mode) {
                        currentFlow.getSource().setClassname(dataString);
                    } else {
                        currentFlow.getSink().setClassname(dataString);
                    }
                    break;
                case "file":
                    currentFlow.setApk(dataString);
                default:
                    break;
            }
            // No matter what, we always want data to be null at the end of an element
            // so that we don't get extra data from tags we don't specifically handle.
            data = null;
        }

        @Override
        public void warning(SAXParseException e) {
            LOGGER.warning(String.format("Parsing threw a warning: %s", e.toString()));
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        /**
         * Processes text and cleans it. Performs the following operations:
         * - Removes beginning and ending whitespace.
         * - Removes consecutive spaces (only one space maximum between elements).
         * - If enabled, replaces newlines with spaces.
         *
         * @param t The string to process.
         * @return The cleaned string.
         */
        private String cleanText(String t) {
            // Remove beginning and ending whitespace.
            t = t.trim();
            // If enabled, replace newlines with spaces.
            if (this.stripNewlines) {
                t = t.replace('\n', ' ');
            }
            // Removes consecutive spaces
            t = t.replaceAll("\\s{2,}", " ");
            // Add < and > tags back in.
//            t = t.replaceAll("\\$lt;", "<");
//            t = t.replaceAll("\\$gt;", ">");
            // originally had this ^^ as part of the clean but this should be handled not here but when we match flows.
            return t;
        }
    }
}
