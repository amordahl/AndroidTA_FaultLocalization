package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.nio.file.Path;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
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
import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageRecord;

/**
 * This processor reads in the content of each data structure as a String.
 * @author austin
 *
 */
public class DataStructureContentLogProcessor extends AbstractCoverageTaskProcessor<DataStructureCoverageRecord> {

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
	
	private Function<String, Object> getParserForType(Class<?> type) {
		if (AbstractCollection.class.isAssignableFrom(type)) {
			return s -> parseCollection(s);
		} else { // we can return other handlers here for different types
			return s -> s;
		}
	}
	/**
	 * Processes a line produced by the coverage instrumenter.
	 */
	@Override
	public Collection<DataStructureCoverageRecord> processLine(String line) {
		// Format of line: SampleClass:0-1,java.lang.String,sampleString
		Collection<DataStructureCoverageRecord> result = new ArrayList<>();
		String location = line.split(",")[0];
		String clazz = line.split(",")[1];
		
		// The rest of the string, location + clazz + 2 (for the commas)
		String content = line.substring(location.length() + clazz.length() + 2);
		Class<?> type;
		try {
			type = Class.forName(clazz);
		} catch (ClassNotFoundException cnfe) {
			logger.warn("Could not convert string {} to a class. Constructing its record with java.lang.Object",
					clazz);
			type = Object.class;
		}
		result.add(new DataStructureCoverageRecord(location, type, getParserForType(type).apply(content)));
		return result;
	}

}
