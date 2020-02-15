package interpreter;

import function.Function;

import java.util.LinkedList;
import java.util.List;

/* 在java中自定义堆栈 */
public class FunctionStack {

	public List<FunctionStateMark> functionStateStack = new LinkedList<>();
	public boolean toEnd = false;

	public void push(FunctionStateMark fsm)
	{
		this.functionStateStack.add(fsm);
		text();
	}

	public void text() //如果存在匹配那么抵消
	{
		int count = functionStateStack.size();
		Function lastFunction;
		Function lastTwoFunction;
		FunctionState lastState = functionStateStack.get(count - 1).
		getFunctionState();
		if(functionStateStack.size() == 0)
		{
			toEnd = true;
		    return;
		}
		if(lastState == FunctionState.FunctionBegin || count < 1)
		 return;
		lastFunction = functionStateStack.get(count - 1).getFunction();
		lastTwoFunction = functionStateStack.get(count - 2).getFunction();
		if(lastFunction.getName().equals(lastTwoFunction.getName()))  //判断是不是同
		{
			functionStateStack.remove(count - 1);
			functionStateStack.remove(count - 2);
		}
		if(functionStateStack.size() == 0)
			toEnd = true;
	}

	public String printStackInformation()   //
	{
		String functionStackInformation = "";
		for(int i = 0; i < functionStateStack.size(); i++)
		{
			functionStackInformation += functionStateStack.get(i).getFunction().getName();
		}
		return functionStackInformation;
	}
}

class FunctionStateMark
{
	public FunctionState functionState;
	public Function function;
	public FunctionStateMark(Function function, FunctionState functionState)
	{
		this.functionState = functionState;
		this.function = function;
	}
	public FunctionState getFunctionState()
	{
		return this.functionState;
	}
	public Function getFunction()
	{
		return this.function;
	}
}

enum FunctionState
{
	FunctionBegin,
	FunctionEnd;
}