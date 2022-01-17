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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;


/**
 * This abstract class handles parsing of an XML Flow File.
 * Concrete classes need to implement the {@link #getFlowHandler()}
 * method, which returns a SAX parser created from DefaultParser.
 * <p>
 * I would suggest you consult the Oracle tutorial "Parsing an XML File Using SAX",
 * (last accessed 2020-05-29) to learn more about how to do this.
 * (https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html)
 */
public abstract class XMLFlowFileReader implements FlowFileReader {
    private final Logger LOGGER = Logger.getLogger(XMLFlowFileReader.class.getName());

    /**
     * Converts a filename into a URL, beginning with file:
     *
     * @param filename The filename to convert to a URL.
     * @return The corresponding URL for the given filename.
     */
    private static String convertToFileURL(String filename) {
        // Taken directly from the Oracle tutorial "Parsing an XML File Using SAX", accessed 2020-05-29
        // (https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html)
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }

    /**
     * Processes the file referred to by the filename parameter and returns an iterator
     * over the flows that were in the file. This implementation was inspired by
     * the Oracle tutorial "Parsing an XML File Using SAX", accessed 2020-05-29
     * at (https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html)
     *
     * @param flowFile   The XML file holding the flows.
     * @param schemaFile The schema for validating the XML file. Pass as null if no schems should be used.
     * @return An iterator pointing to the list of flows discovered in the file.
     * @throws ParserConfigurationException if a parser cannot
     *                                      be created which satisfies the requested configuration.
     * @throws SAXException                 If an error occurs while setting up the schema or during parsing.
     */
    public Iterator<Flow> getFlows(File flowFile, File schemaFile) throws IOException, SAXException, ParserConfigurationException {
        // set up SAX parser factory (using SAX because the files can be very large).
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        if (schemaFile != null) {
            // Need to set these properties for the sax parser factory in order for
            // validation to actually occur.
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setValidating(true);
        }
        SAXParser saxParser = saxParserFactory.newSAXParser();
        if (schemaFile != null) {
            saxParser.setProperty(SAXLocalNameCount.JAXP_SCHEMA_LANGUAGE, SAXLocalNameCount.W3C_XML_SCHEMA);
            saxParser.setProperty(SAXLocalNameCount.JAXP_SCHEMA_SOURCE, schemaFile);
        }
        XMLReader xmlReader = saxParser.getXMLReader();
        DefaultHandler fh = getFlowHandler();
        xmlReader.setContentHandler(fh);
        xmlReader.setErrorHandler(fh);
        xmlReader.parse(convertToFileURL(flowFile.getAbsolutePath()));
        return getFlowIterator(fh);
    }
    public ArrayList<ArrayList<Flow>> getPreserveFlowList(File file, File schemaFile) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        if (schemaFile != null) {
            // Need to set these properties for the sax parser factory in order for
            // validation to actually occur.
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setValidating(true);
        }
        SAXParser saxParser = saxParserFactory.newSAXParser();
        if (schemaFile != null) {
            saxParser.setProperty(SAXLocalNameCount.JAXP_SCHEMA_LANGUAGE, SAXLocalNameCount.W3C_XML_SCHEMA);
            saxParser.setProperty(SAXLocalNameCount.JAXP_SCHEMA_SOURCE, schemaFile);
        }
        XMLReader xmlReader = saxParser.getXMLReader();
        DefaultHandler fh = getFlowHandler();
        xmlReader.setContentHandler(fh);
        xmlReader.setErrorHandler(fh);
        xmlReader.parse(convertToFileURL(file.getAbsolutePath()));
        return getPreserveIterator(fh);
    }

    protected abstract ArrayList<ArrayList<Flow>> getPreserveIterator(DefaultHandler fh);



    /**
     * Get a SAX handler to perform the XML parsing. Should override {@link DefaultHandler}
     * by implementing the standard callbacks (e.g., {@link DefaultHandler#startElement(String, String, String, Attributes)},
     * {@link DefaultHandler#endElement(String, String, String)}, and
     * {@link DefaultHandler#characters(char[], int, int)}.
     *
     * @return The implementation of DefaultHandler.
     */
    abstract DefaultHandler getFlowHandler();
    /**
     * Given an implementation of {@link DefaultHandler}, use it to
     * parse the XML file and read the flows.
     *
     * @param dh the implementation of DefaultHandler.
     * @return An iterator pointing to the {@link com.utdallas.cs.alps.flows.Flow Flows} discovered
     * in the XML file.
     */
    abstract Iterator<com.utdallas.cs.alps.flows.Flow> getFlowIterator(DefaultHandler dh);

    /**
     * Processes the given file and returns an iterator to the flows in it.
     *
     * @param flowFile The XML file to be read in.
     * @return An iterator pointing to the list of flows that were parsed.
     */
    @Override
    public Iterator<com.utdallas.cs.alps.flows.Flow> getFlows(File flowFile) {
        try {
            return this.getFlows(flowFile, null);
        } catch (IOException io) {
            throw new RuntimeException(String.format("There was an error during parsing: %s", io.getMessage()));
        } catch (SAXException saxException) {
            throw new RuntimeException(String.format("An error occurred while setting up the schema or while " +
                    "parsing the file: %s", saxException.getMessage()));
        } catch (ParserConfigurationException parserConfigurationException) {
            throw new RuntimeException(String.format("An error occurred while creating the parser: " +
                    "%s", parserConfigurationException.getMessage()));
        }
    }

    public static class SAXLocalNameCount extends DefaultHandler {
        static final String JAXP_SCHEMA_LANGUAGE =
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

        static final String W3C_XML_SCHEMA =
                "http://www.w3.org/2001/XMLSchema";

        static final String JAXP_SCHEMA_SOURCE =
                "http://java.sun.com/xml/jaxp/properties/schemaSource";
    }

}
