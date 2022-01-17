package edu.utdallas.amordahl.CoverageComparer.parsers.implemented_listeners;

import java.util.ArrayList;
import java.util.Stack;

import edu.utdallas.amordahl.CoverageComparer.parsers.GeneralArrayBaseListener;
import edu.utdallas.amordahl.CoverageComparer.parsers.GeneralArrayParser.ArrayContext;
import edu.utdallas.amordahl.CoverageComparer.parsers.GeneralArrayParser.StringContext;

public class GeneralArrayListener extends GeneralArrayBaseListener {

	public GeneralArrayListener() {
		arrayStack = new Stack<ArrayList<Object>>();
	}
	private ArrayList<Object> master;
	
	public ArrayList<Object> getMaster() {
		return master;
	}

	private Stack<ArrayList<Object>> arrayStack;
	
	
	@Override
	public void enterArray(ArrayContext ctx) {
		arrayStack.add(new ArrayList<Object>());
	}

	@Override
	public void exitArray(ArrayContext ctx) {
		ArrayList<Object> popped = arrayStack.pop();
		if (arrayStack.size() == 0) {
			master = popped;
		}
		else {
			arrayStack.peek().add(popped);
		}
	}

	@Override
	public void exitString(StringContext ctx) {
		arrayStack.peek().add(ctx.getText());
	}
}
