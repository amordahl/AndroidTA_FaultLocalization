import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;


/*
 * We need an aql_answer.xsd to read aql xml files so this makes the aql_answer.xsd if we don't have one
 * */
public class SchemaGenerator {




    public static final String SCHEMA_PATH= "schema/aql_answer.xsd";

    public static void generateSchema() throws IOException {
        File f = Paths.get(SCHEMA_PATH).toFile();
        f.mkdirs();
        if(f.exists())
            f.delete();
        f.createNewFile();

        FileWriter fw = new FileWriter(f);
        fw.write(SCHEMA_STRING);
        fw.flush();
        fw.close();

    }


    public static final String SCHEMA_STRING = "<xsd:schema elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "                <xsd:element name=\"hash\">\n" +
            "                    <xsd:complexType>\n" +
            "                        <xsd:simpleContent>\n" +
            "                            <xsd:extension base=\"xsd:string\">\n" +
            "                                <xsd:attribute type=\"xsd:string\" name=\"type\"/>\n" +
            "                            </xsd:extension>\n" +
            "                        </xsd:simpleContent>\n" +
            "                    </xsd:complexType>\n" +
            "                </xsd:element>\n" +
            "                <xsd:element name=\"file\" type=\"xsd:string\"/>\n" +
            "                <xsd:element name=\"hashes\">\n" +
            "                    <xsd:complexType>\n" +
            "                        <xsd:sequence>\n" +
            "                            <xsd:element ref=\"hash\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "                        </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"method\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"classname\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"app\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"file\"/>\n" +
            "                <xsd:element ref=\"hashes\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"name\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"value\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"attribute\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"name\"/>\n" +
            "                <xsd:element ref=\"value\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"reference\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"statement\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"method\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"classname\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"app\"/>\n" +
            "            </xsd:sequence>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"type\"/>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"attributes\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"attribute\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"permission\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"name\"/>\n" +
            "                <xsd:element ref=\"reference\"/>\n" +
            "                <xsd:element ref=\"attributes\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"type\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"scheme\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"ssp\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"host\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"port\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"path\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"action\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"category\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"data\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"type\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"scheme\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"ssp\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"host\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"port\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"path\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"parameter\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"type\"/>\n" +
            "                <xsd:element ref=\"value\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"statementfull\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"statementgeneric\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"parameters\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"parameter\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"statement\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"statementfull\"/>\n" +
            "                <xsd:element ref=\"statementgeneric\"/>\n" +
            "                <xsd:element ref=\"parameters\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"target\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"action\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
            "                <xsd:element ref=\"category\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
            "                <xsd:element ref=\"data\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
            "                <xsd:element ref=\"reference\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"intentsource\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"target\"/>\n" +
            "                <xsd:element ref=\"reference\"/>\n" +
            "                <xsd:element ref=\"attributes\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"intentsink\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"target\"/>\n" +
            "                <xsd:element ref=\"reference\"/>\n" +
            "                <xsd:element ref=\"attributes\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"intent\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"reference\"/>\n" +
            "                <xsd:element ref=\"target\"/>\n" +
            "                <xsd:element ref=\"attributes\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"intentfilter\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"reference\"/>\n" +
            "                <xsd:element ref=\"action\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
            "                <xsd:element ref=\"category\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
            "                <xsd:element ref=\"data\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
            "                <xsd:element ref=\"attributes\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"flow\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"reference\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"attributes\" minOccurs=\"0\"/>\n" +
            "                <xsd:element ref=\"classification\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"generating_config\"/>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"id\"/>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"justification\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"result\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"crossref\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"permissions\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"permission\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"intentsources\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"intentsource\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"intentsinks\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"intentsink\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"intents\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"intent\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"intentfilters\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"intentfilter\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"flows\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"flow\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"violation\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"flow\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"config1\"/>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"config2\"/>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"type\"/>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"answer\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"flows\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"flowset\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"preserve\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"config1\"/>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"config2\"/>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"type\"/>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"violation\"/>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "    <xsd:element name=\"classification\" type=\"xsd:string\"/>\n" +
            "    <xsd:element name=\"preserve\">\n" +
            "        <xsd:complexType>\n" +
            "            <xsd:sequence>\n" +
            "                <xsd:element ref=\"flow\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "            </xsd:sequence>\n" +
            "            <xsd:attribute type=\"xsd:string\" name=\"config\"/>\n" +
            "        </xsd:complexType>\n" +
            "    </xsd:element>\n" +
            "</xsd:schema>";

}
