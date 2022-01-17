package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.nio.file.Path;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.parsers.GeneralArrayLexer;
import edu.utdallas.amordahl.CoverageComparer.parsers.GeneralArrayParser;
import edu.utdallas.amordahl.CoverageComparer.parsers.implemented_listeners.GeneralArrayListener;
import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageLocation;
import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageRecord;

/**
 * This processor reads in the content of each data structure as a String.
 * @author austin
 *
 */
public class DataStructureContentLogProcessor extends AbstractCoverageTaskProcessor<DataStructureCoverageLocation, Object> {

	private static Logger logger = LoggerFactory.getLogger(DataStructureContentLogProcessor.class);
	@Override
	public String getName() {
		return "DataStructureContentLogProcessor";
	}


	@Override
	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".datastructurecontentlog" + ".intermediate");
	}
	
	// TODO Implement this so that it works recursively.
	protected AbstractCollection<?> parseCollection(String content) {
		GeneralArrayLexer l = new GeneralArrayLexer(CharStreams.fromString(content));
		GeneralArrayParser p = new GeneralArrayParser(new CommonTokenStream(l));
		p.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
			}
		});

		GeneralArrayListener gla = new GeneralArrayListener();
		p.addParseListener(gla);
		p.array().enterRule(gla);
		return gla.getMaster();
	}
	
	private Object parseObject(String content) {
		try {
			AbstractCollection<?> collection = parseCollection(content);
			return collection;
		} catch (IllegalStateException ise) {
			logger.info("Could not parse {} as a collection.", content);
			return content;
		}
	}
	/**
	 * Processes a line produced by the coverage instrumenter.
	 */
	@Override
	public Map<DataStructureCoverageLocation, Object> processLine(String line) {
		// Format of line: SampleClass:0-1,java.lang.String,sampleString
		Map<DataStructureCoverageLocation, Object> result = new HashMap<DataStructureCoverageLocation, Object>();
		if (!line.startsWith("DATASTRUCTURE:")) {
			return result;
		}
		
		String location = line.split(",")[0];
		String type = line.split(",")[1];
		String content = line.split(",")[2];
		
		result.put(new DataStructureCoverageLocation(location, type), parseObject(content));
		return result;
	}


	@Override
	protected boolean allowParallelLineProcessing() {
		// TODO Auto-generated method stub
		return true;
	}

}
