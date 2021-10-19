package edu.utdallas.amordahl.CoverageComparer.parsers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.utdallas.amordahl.CoverageComparer.parsers.GeneralArrayLexer;
import edu.utdallas.amordahl.CoverageComparer.parsers.GeneralArrayParser.ArrayContext;
import edu.utdallas.amordahl.CoverageComparer.parsers.GeneralArrayParser.ElementContext;
import edu.utdallas.amordahl.CoverageComparer.parsers.GeneralArrayParser.StringContext;
import edu.utdallas.amordahl.CoverageComparer.parsers.implemented_listeners.GeneralArrayListener;

@RunWith(Parameterized.class)
public class GeneralArrayTests {

	private String arrayAsString;
	private ArrayList<Object> array;

	private ArrayList<Object> makeParserForString(String input) {
		GeneralArrayLexer l = new GeneralArrayLexer(CharStreams.fromString(input));
		GeneralArrayParser p = new GeneralArrayParser(new CommonTokenStream(l));
		p.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
			}
		});

		final ArrayList<ArrayList<String>> arrays = new ArrayList<>();
		final AtomicReference<String> token = new AtomicReference<String>();

		GeneralArrayListener gla = new GeneralArrayListener();
		p.addParseListener(gla);
		p.array().enterRule(gla);
		return gla.getMaster();

	}

	public GeneralArrayTests(Object arrayAsString, Object array) {
		this.arrayAsString = (String) arrayAsString;
		this.array = (ArrayList<Object>) array;
	}

	@Parameters
	public static Collection<Object[]> getParams() {
		ArrayList<Object[]> result = new ArrayList<Object[]>();
		// Empty array
		result.add(new Object[] { "[]", new ArrayList<Object>() });

		// Singleton array
		result.add(new Object[] { "[a]", new ArrayList<Object>(Arrays.asList("a")) });

		// Array of multiple elements.
		result.add(new Object[] { "[a, aa, aba]", new ArrayList<Object>(Arrays.asList("a", "aa", "aba")) });

		// Array of multiple elements (no spaces)
		result.add(new Object[] { "[a,aa,aba]", new ArrayList<Object>(Arrays.asList("a", "aa", "aba")) });

		// Array of multiple elements, including numbers.
		result.add(new Object[] { "[a, 1, 1.0, -184]", new ArrayList<Object>(Arrays.asList("a", "1", "1.0", "-184")) });

		// Nested array of characters
		result.add(new Object[] { "[//, Ljava/lang/Object;, [a, b], -184]", new ArrayList<Object>(
				Arrays.asList("//", "Ljava/lang/Object;", new ArrayList<Object>(Arrays.asList("a", "b")), "-184")) });

		// Nested array of characters
		result.add(new Object[] { "[//, Ljava/lang/Object;, [a, [1, 2], [], b], -184]", new ArrayList<Object>(
				Arrays.asList("//", "Ljava/lang/Object;", 
						new ArrayList<Object>(Arrays.asList("a", 
								new ArrayList<Object>(Arrays.asList("1", "2")),
								new ArrayList<Object>(),
								"b")), "-184")) });

		return result;

	}

	@Test
	public void testArrayParsing() {
		assertEquals(makeParserForString(this.arrayAsString), this.array);
	}
}
