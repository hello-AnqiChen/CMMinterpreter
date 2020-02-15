package interpreter;

import interpretException.InterpretException;
import java.util.LinkedList;

public class InterpreterSymbolTable {

    private static InterpreterSymbolTable interpreterSymbolTable = new InterpreterSymbolTable();

    LinkedList<Symbol> symbolList = new LinkedList<>();
    LinkedList<Symbol> tempNames = new LinkedList<>();
    private static final String TEMP_PREFIX = "temp";

    public static InterpreterSymbolTable getInterpreterSymbolTable()
    {
        return interpreterSymbolTable;
    }

    public LinkedList<Symbol> getSymbolList() {
        return symbolList;
    }

    public void clearSymbolList() {
        if (symbolList != null) {
            symbolList.clear();
            symbolList = null;
        }
        if (tempNames != null) {
            tempNames.clear();
            tempNames = null;
        }
    }

    public void register(Symbol symbol) throws InterpretException
    {
        //检查是否出现过同名变量
        for (int i = 0; i < symbolList.size(); i++) {
            if (symbolList.get(i).getName().equals(symbol.getName()))
            {
                //出现重名变量时，检查所在level，若符合要求则使用链表相连
                if (symbolList.get(i).getLevel() < symbol.getLevel())
                {
                    symbol.setNext(symbolList.get(i));
                    symbolList.set(i, symbol);
                    return;
                }else
                {
                    throw new InterpretException("变量 <" + symbol.getName());
                }
            }
        }
        symbolList.add(symbol);
    }

    public void deregister(int level)
    {
        for (int i = 0; i < symbolList.size(); i++) {
            if (symbolList.get(i).getLevel() == level)
                symbolList.set(i, symbolList.get(i).getNext());
        }

        for (int i = symbolList.size() - 1; i >= 0; i--) {
            if (symbolList.get(i) == null)
                symbolList.remove(i);
        }
    }

    public boolean variableExisted(Symbol symbol)
    {
        if (this.symbolList == null)
            return false;
        for (Symbol symbol_ : this.symbolList)
        {
            if (symbol_.getName().equals(symbol.getName()))
                return true;
        }
        return false;
    }

    public boolean variableExisted(String variableName)
    {
        if (this.symbolList == null)
            return false;
        for (Symbol symbol_ : this.symbolList)
        {
            if (symbol_.getName().equals(variableName))
                return true;
        }
        return false;
    }

    public void addNewVariable(Symbol symbol)
    {
        this.symbolList.add(symbol);
    }

    public void updateVariableValue(Symbol symbol)
    {
        if (variableExisted(symbol))
        {
            for (Symbol symbol_ : this.symbolList)
            {
                if (symbol_.getName().equals(symbol.getName()))
                {
                    symbol_.setValue(symbol_.getValue());
                }
            }
        }
    }

    /**
     * 通过符号名获取符号表中的符号
     * 对于临时变量temp，若已经在tempNames中存在则返回该符号，若不存在则将其加入tempNames并返回
     * @param name
     * @return
     * @throws InterpretException
     */
    public Symbol getSymbol(String name) throws InterpretException
    {
        if (variableExisted(name))
        {
            for (Symbol symbol: this.symbolList)
            {
                if (symbol.getName().equals(name))
                {
                    return symbol;
                }
            }
        }else {
            for (Symbol symbol: tempNames)
            {
                if (symbol.getName().equals(name))
                {
                    return symbol;
                }
            }
            if (name.startsWith(TEMP_PREFIX))
            {
                Symbol returnSymbol = new Symbol(name, Symbol.TEMP, -1);
                tempNames.add(returnSymbol);
                return returnSymbol;
            }
        }
        throw new InterpretException("变量 <" + name + "> 不存在或者不以temp开头");
    }

    /**
     * 为简单元素（非数组）赋值
     * @param name
     * @param value
     * @throws InterpretException
     */
    public void setSymbolValue(String name, Value value) throws InterpretException
    {
        getSymbol(name).setValue(value);
    }

    /**
     * 为整型数组元素赋值
     * @param name
     * @param value
     * @param index
     * @throws InterpretException
     */
    public void setSymbolValue(String name, int value, int index) throws InterpretException
    {
        if (getSymbol(name).getValue().getArrayInt().length > index)
            getSymbol(name).getValue().getArrayInt()[index] = value;
        else
            throw new InterpretException("数组 <" +  name + "> 下标" + index + "越界");
    }

    /**
     * 为浮点型数组赋值
     * @param name
     * @param value
     * @param index
     * @throws InterpretException
     */
    public void setSymbolValue(String name, double value, int index) throws InterpretException
    {
        getSymbol(name).getValue().getArrayReal()[index] = value;
    }

    /**
     * 取值用这个函数
     * @param name
     * @param index -1时表示单值,否则表示索引值
     * @return
     * @throws InterpretException
     */
    public Value getSymbolValue(String name, int index) throws InterpretException
    {
        Symbol symbol = getSymbol(name);

        if (index == -1)
            return symbol.getValue();
        else
        {

            if (symbol.getType() == Symbol.ARRAY_INT)
            {
                if (symbol.getValue().getArrayInt().length < index + 1)
                    throw new InterpretException("数组" + name + "> 下标 " + index + "越界");


                Value returnValue = new Value(Symbol.SINGLE_INT);
                returnValue.setInt(symbol.getValue().getArrayInt()[index]);
                return returnValue;
            }else
            {
                if (symbol.getValue().getArrayReal().length < index + 1)
                    throw new InterpretException("数组" + name + "> 下标 " + index + "越界");


                Value returnValue = new Value(Symbol.SINGLE_REAL);
                returnValue.setReal(symbol.getValue().getArrayReal()[index]);
                return returnValue;
            }
        }
    }

    /**
     * 取单值用这个函数
     * @param name
     * @return
     * @throws InterpretException
     */
    public Value getSymbolValue(String name) throws InterpretException {
        return getSymbolValue(name, -1);
    }

    /**
     * 返回Symbol中的类型
     * @param name
     * @return
     * @throws InterpretException
     */
    public int getSymbolType(String name) throws InterpretException
    {
        return getSymbol(name).getType();
    }

    public String toString()
    {
        String result = "";
        if (this.symbolList == null)
            return result;
        for (Symbol symbol_ : this.symbolList)
            result += symbol_.getName() + " " + symbol_.getValue() + "\n";
        return result;
    }
}