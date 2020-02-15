package interpreter;

import GUI.MainFrame;
import GUI.outputListener;
import interpretException.InterpretException;
import semantics.FourCode;
import function.Function;
import semantics.Semantic;

import javax.swing.*;
import java.util.*;


/**
 * 四元式
 * call 函数名 参数个数 返回值
 * parameters 形参名 形参索引 null
 * return 返回值 null 目标
 * jmp 条件  null 目标  是条件为假时跳转到目标
 * jmp null null 目标  无条件跳转到目标, 超过语句数,则程序结束
 * assign 元素 null 目标
 * int/real null 元素个数/null 变量名
 * read/write null null 元素
 * in null null null 进入语句块
 * out null null null 出语句块
 * assign 值 null 目标
 * +
 * -
 * *
 * /
 */
public class Interpreter {
    private String output;
    private outputListener listener;

    public void registerListener(MainFrame listener){
        this.listener = listener;
    }

    private static int mLevel;
    private static int pc;
    private static InterpreterSymbolTable callerSymbolTable = new InterpreterSymbolTable();

    private FunctionStack functionStack = new FunctionStack();
    private List<Function> functions = new LinkedList<>();

    //用于临时变量temp命名
    private int[] temp = new int[100];

    public String interpret(Semantic semantic) throws InterpretException{
        for (FourCode fourCode: semantic.codes) {
            interpretSingleCode(fourCode);
        }
        passFunctionList(semantic.functions);
        for (Function func: semantic.functions) {
            if (func.getName().equals("main"))
                interpretMainFunction(func);
        }
        return output;
    }

    /**
     * 初始化函数数组
     * @param functionArrayList
     */
    public void passFunctionList(ArrayList<Function> functionArrayList)
    {
        this.functions = functionArrayList;
    }

    /**
     * 解释执行全局变量
     * @param fourCode
     * @throws InterpretException
     */
    public void interpretSingleCode(FourCode fourCode) throws InterpretException
    {
        InterpreterSymbolTable singleSymbolTable = callerSymbolTable;
        interpretCode(null, fourCode, false, singleSymbolTable);
    }

    /**
     * 解释执行主函数的四元式
     * @param function
     * @throws InterpretException
     */
    public void interpretMainFunction(Function function) throws InterpretException
    {
        List<FourCode> fourCodes;
        FunctionStateMark functionStateMark1 = new FunctionStateMark(function, FunctionState.FunctionBegin);
        FunctionStateMark functionStateMark2 = new FunctionStateMark(function,FunctionState.FunctionEnd);

        //将全局变量添加到主函数的符号表里
        InterpreterSymbolTable mainSymbolTable = copySymbolTable(callerSymbolTable);
        function.setInterpreterSymbolTable(mainSymbolTable);

        //函数开始信息入栈
        functionStack.push(functionStateMark1);

        fourCodes = function.getCodes();
        for (function.getPc(); function.getPc() < fourCodes.size(); )
        {
            //解释函数的四元式
            interpretCode(getFunctionFromTopOfStack(), fourCodes.get(getFunctionFromTopOfStack().getPc()), true, function.getInterpreterSymbolTable());
        }
        functionStack.push(functionStateMark2);
    }

    /**
     * 解释执行四元式
     * @param function
     * @param code
     * @param isFunction
     * @throws InterpretException
     */
    public void interpretCode(Function function, FourCode code, boolean isFunction, InterpreterSymbolTable symbolTable) throws InterpretException
    {
        String operatorType = code.getOp();

        if (isFunction)
            symbolTable = function.getInterpreterSymbolTable();

        /**
         * (call, 函数名, 参数个数， 返回值temp)
         * (parameters, 参数名, 参数索引，null)
         * ...有可能存在多个参数
         */
        if (operatorType.equals(FourCode.CALL))
        {
            if (function == null)
                throw new InterpretException("主函数未定义！");

            int parametersCount = Integer.parseInt(code.getArg2());
            String parametersName[] = new String[parametersCount];
            String functionName = code.getArg1();
            getFunctionFromTopOfStack().pcPlus();


            //变量callee指向被调用的函数
            Function callee = new Function();
            for (int i = 0; i < functions.size(); i++) {
                if (functions.get(i).getName().equals(functionName))
                {
                    callee = copyFunction(functions.get(i));
                }
            }

            //获取函数形参
            List<String> parameters = callee.getParams();

            for (int i = 0; i < parametersCount; i++) {
                Function tempFunc = getFunctionFromTopOfStack();
                FourCode tempCode = tempFunc.getCodes().get(tempFunc.getPc());
                parametersName[i] = tempCode.getArg1();
                getFunctionFromTopOfStack().pcPlus();
            }

            int returnMark;
            returnMark = getTempPostfix(code.getResult());

            FunctionStateMark functionBegin = new FunctionStateMark(callee, FunctionState.FunctionBegin);
            functionStack.push(functionBegin);
            getFunctionFromTopOfStack().setReturnMark(returnMark);

            //将caller的符号表加入到callee的符号表，传参
            InterpreterSymbolTable calleeInterpreterSymbolTable = new InterpreterSymbolTable();
            for (int i = 0, j = 1; i < parametersCount; i++, j += 2) {
                //将实参的值获取出来
                Symbol symbol = symbolTable.getSymbol(parametersName[i]);
                //将名字改为形参名
                symbol.setName(parameters.get(j));
                calleeInterpreterSymbolTable.addNewVariable(symbol);
            }
            callee.setInterpreterSymbolTable(calleeInterpreterSymbolTable);

            //解释执行被调用的函数
            List<FourCode> fourCodes = callee.getCodes();
            for (callee.getPc(); callee.getPc() < fourCodes.size(); )
            {
                interpretCode(callee, fourCodes.get(callee.getPc()), true, callee.getInterpreterSymbolTable());
            }

            FunctionStateMark functionEnd = new FunctionStateMark(getFunctionFromTopOfStack(), FunctionState.FunctionEnd);
            functionStack.push(functionEnd);

            return;
        }

        //(return, temp, null, temp) 或者 (return, 变量名, null, temp)
        if (operatorType.equals(FourCode.RETURN))
        {

            if (function.getName().equals("main"))
                return;

            Value value = getValue(code.getArg1(), symbolTable);
            setValue(code.getResult(), value, symbolTable);

            getFunctionFromTopOfStack().setReturnValue(getValue(code.getArg1(), symbolTable).getInt());

            //将返回的值赋给调用者
            setValue("temp" + getFunctionFromTopOfStack().getReturnMark(), value, symbolTable);
            Function caller = functionStack.functionStateStack.get(functionStack.functionStateStack.size() - 2).getFunction();
            setValue("temp" + getFunctionFromTopOfStack().getReturnMark(), value, caller.getInterpreterSymbolTable());
        }

        //jmp 条件  null 目标  是条件为假时跳转到目标
        if (operatorType.equals(FourCode.JMP))
        {
            if (code.getArg1() == null || symbolTable.getSymbolValue(code.getArg1()).getType() == Symbol.FALSE)
            {
                if (!isFunction)
                    pc = getValue(code.getResult(), symbolTable).getInt();
                else
                    function.setPc(getValue(code.getResult(), symbolTable).getInt());
                return;
            }

        }
        //read/write null null 元素
        if (operatorType.equals(FourCode.READ))
        {
            String input = JOptionPane.showInputDialog("请输入：");
            //System.out.println("请输入：");
            //Scanner scanner = new Scanner(System.in);
            //String input = scanner.next();
            int type = symbolTable.getSymbolType(getId(code.getResult()));
            //read语句读取用户输入并将值赋给变量
            switch (type)
            {
                case Symbol.SINGLE_INT:
                case Symbol.ARRAY_INT: {
                    Value value = parseValue(input);
                    if (value.getType() == Symbol.SINGLE_INT)
                        setValue(code.getResult(), value, symbolTable);
                    else
                        throw new InterpretException("类型不匹配");
                    break;
                }
                case Symbol.SINGLE_REAL:
                case Symbol.ARRAY_REAL:
                {
                    Value value = parseValue(input);
                    setValue(code.getResult(), value, symbolTable);
                    break;
                }
                case Symbol.TEMP:
                default:
                    break;
            }
        }
        if (operatorType.equals(FourCode.WRITE))
        {
            int index = -1;
            //if (symbolTable.variableExisted(code.getResult()))
            //{
                if (isArrayElement(code.getResult()))
                    index = getIndex(code.getResult(), symbolTable);
                //System.out.println(symbolTable.getSymbolValue(code.getResult(), index));
//                output += symbolTable.getSymbolValue(code.getResult(), index) + "\n";

                //gui
                listener.output(symbolTable.getSymbolValue(code.getResult(), index) + "\n");
            /*}else {
                //System.out.println(code.getResult());
                listener.output(code.getResult() + "\n");
                //output += code.getResult() + "\n";
            }*/
        }

        if (operatorType.equals(FourCode.IN))
        {
            mLevel++;
        }
        if (operatorType.equals(FourCode.OUT))
        {
            symbolTable.deregister(mLevel);
            mLevel--;
        }
        //int/real null 元素个数/null 变量名
        //处理声明语句
        //???
        if (operatorType.equals(FourCode.INT))
        {
            //若是数组，四元式中的第三个元素为数组维度
            if (code.getArg2() != null)
            {
                Symbol symbol = new Symbol(code.getResult(), Symbol.ARRAY_INT, mLevel);
                symbol.getValue().initArray(getInt(code.getArg2(), symbolTable));
                symbolTable.register(symbol);
            }else{
                int intValue = 0;
                if (code.getArg1() != null)
                    intValue = getInt(code.getArg1(), symbolTable);
                Symbol symbol = new Symbol(code.getResult(), Symbol.SINGLE_INT, mLevel, intValue);
                symbolTable.register(symbol);
            }
        }
        if (operatorType.equals(FourCode.REAL))
        {
            if (code.getArg2() != null)
            {
                Symbol symbol = new Symbol(code.getResult(), Symbol.ARRAY_REAL, mLevel);
                symbol.getValue().initArray(getInt(code.getArg2(), symbolTable));
                symbolTable.register(symbol);
            }else{
                double doubleValue = 0;
                if (code.getArg1() != null)
                    doubleValue = getDouble(code.getArg1(), symbolTable);
                Symbol symbol = new Symbol(code.getResult(), Symbol.SINGLE_REAL, mLevel, doubleValue);
                symbolTable.register(symbol);
            }
        }
        //assign 值 null 目标
        if (operatorType.equals(FourCode.ASSIGN))
        {
            Value value = getValue(code.getArg1(), symbolTable);
            setValue(code.getResult(), value, symbolTable);

        }
        if (operatorType.equals(FourCode.PLUS))
        {
            String codeResult = code.getResult();
            Value valueArg1 = getValue(code.getArg1(), symbolTable);
            Value valueArg2 = getValue(code.getArg2(), symbolTable);
            Value plusResult = valueArg1.plus(valueArg2);
            setValue(codeResult, plusResult, symbolTable);
//            setValue(code.getResult(), getValue(code.getArg1(), symbolTable).plus(getValue(code.getArg2(), symbolTable)), symbolTable);

//            int i;
//            if (startsWithTemp(code.getResult()))
//            {
//                i = getTempPostfix(code.getResult());
//                temp[i] = getValue(code.getResult()).getInt()
//            }

        }
        if (operatorType.equals(FourCode.MINUS))
        {
            if (code.getArg2() != null)
                setValue(code.getResult(), getValue(code.getArg1(), symbolTable).minus(getValue(code.getArg2(), symbolTable)), symbolTable);
                //boolean型取相反，数值型取相反数
            else
                setValue(code.getResult(), Value.not(getValue(code.getArg1(), symbolTable)), symbolTable);
        }
        if (operatorType.equals(FourCode.MUL))
        {
            setValue(code.getResult(), getValue(code.getArg1(), symbolTable).multiply(getValue(code.getArg2(), symbolTable)), symbolTable);
        }
        if (operatorType.equals(FourCode.DIV))
        {
            setValue(code.getResult(), getValue(code.getArg1(), symbolTable).divide(getValue(code.getArg2(), symbolTable)), symbolTable);
        }
        if (operatorType.equals(FourCode.MORE))
        {
            setValue(code.getResult(), getValue(code.getArg1(), symbolTable).greaterThan(getValue(code.getArg2(), symbolTable)), symbolTable);

            String result = code.getResult();
            int postfix = getTempPostfix(result);
            temp[postfix] = getValue(code.getResult(), symbolTable).getInt();
        }
        if (operatorType.equals(FourCode.LESS))
        {
            setValue(code.getResult(), getValue(code.getArg1(), symbolTable).lessThan(getValue(code.getArg2(), symbolTable)), symbolTable);
        }
        if (operatorType.equals(FourCode.EQ))
        {
            setValue(code.getResult(), getValue(code.getArg1(), symbolTable).equalTo(getValue(code.getArg2(), symbolTable)), symbolTable);

            String result = code.getResult();
            int postfix = getTempPostfix(result);
            temp[postfix] = getValue(code.getResult(), symbolTable).getInt();
        }
        if (operatorType.equals(FourCode.MOREE))
        {
            setValue(code.getResult(), getValue(code.getArg1(), symbolTable).greaterOrEqualTo(getValue(code.getArg2(), symbolTable)), symbolTable);

            String result = code.getResult();
            int postfix = getTempPostfix(result);
            temp[postfix] = getValue(code.getResult(), symbolTable).getInt();
        }
        if (operatorType.equals(FourCode.LESSE))
        {
            setValue(code.getResult(), getValue(code.getArg1(), symbolTable).lessOrEqualTo(getValue(code.getArg2(), symbolTable)), symbolTable);

            String result = code.getResult();
            int postfix = getTempPostfix(result);
            temp[postfix] = getValue(code.getResult(), symbolTable).getInt();
        }
        if (operatorType.equals(FourCode.NEQ))
        {
            setValue(code.getResult(), getValue(code.getArg1(), symbolTable).notEqualTo(getValue(code.getArg2(), symbolTable)), symbolTable);

            String result = code.getResult();
            int postfix = getTempPostfix(result);
            temp[postfix] = getValue(code.getResult(), symbolTable).getInt();
        }
        if (!isFunction)
            pc++;
        else
            function.pcPlus();
    }

    /**
     * 给xx[xx]或者xx赋值
     * @param id
     * @param value
     * @throws InterpretException
     */
    //为变量赋值
    //包含类型检查，若所赋的值的类型与变量类型不匹配则报错
    private void setValue(String id, Value value, InterpreterSymbolTable symbolTable) throws InterpretException
    {
        int index = -1;
        if (isArrayElement(id)) {
            index = getIndex(id, symbolTable);
        }
        int type = symbolTable.getSymbolType(getId(id));

        switch (type)
        {
            case Symbol.SINGLE_INT:
                if(value.getType()==Symbol.SINGLE_INT) {
                    symbolTable.setSymbolValue(getId(id), value);
                }
                else  if(value.getType()==Symbol.TRUE)
                {
                    Value convertValue=new Value(Symbol.SINGLE_INT);
                    convertValue.setInt(1);
                    symbolTable.setSymbolValue(getId(id),convertValue);
                }
                else  if(value.getType()==Symbol.FALSE)
                {
                    //value函数实现强制转换
                    Value convertValue=new Value(Symbol.SINGLE_INT);
                    convertValue.setInt(0);
                    symbolTable.setSymbolValue(getId(id),convertValue);
                }
            case Symbol.SINGLE_REAL:
                if (type == Symbol.SINGLE_REAL)
                    symbolTable.setSymbolValue(getId(id), value.toReal());
                else
                    {
                    if (value.getType() == Symbol.SINGLE_REAL)
                        throw new InterpretException("表达式" + id + "与变量类型不匹配");
                    else
                        symbolTable.setSymbolValue(getId(id), value);
                }
                break;
            case Symbol.ARRAY_INT:
            case Symbol.ARRAY_REAL:
            {
                if (symbolTable.getSymbolValue(getId(id), index).getType() == Symbol.SINGLE_REAL)
                    symbolTable.setSymbolValue(getId(id), value.toReal().getReal(), index);
                else
                if (value.getType() == Symbol.SINGLE_REAL)
                    throw new InterpretException("表达式 <" + id + "> 与变量类型不匹配");
                else
                    symbolTable.setSymbolValue(getId(id), value.getInt(), index);
                break;
            }
            case Symbol.TEMP:
                symbolTable.setSymbolValue(getId(id), value);
                break;
            default:
                break;
        }

    }

    /**
     * 修改临时变量temp的符号表
     * @param tempIndex
     * @param value
     * @param symbolTable
     * @throws InterpretException
     */
    public void setTempValue(int tempIndex, Value value, InterpreterSymbolTable symbolTable) throws InterpretException
    {
        String id = "temp" + tempIndex;

        int index = -1;
        if (isArrayElement(id))
            index = getIndex(id, symbolTable);
        int type = symbolTable.getSymbolType(getId(id));

        switch (type)
        {
            case Symbol.SINGLE_INT:
            case Symbol.SINGLE_REAL:
                if (type == Symbol.SINGLE_REAL)
                    symbolTable.setSymbolValue(getId(id), value.toReal());
                else {
                    if (value.getType() == Symbol.SINGLE_REAL)
                        throw new InterpretException("表达式" + id + "与变量类型不匹配");
                    else
                        symbolTable.setSymbolValue(getId(id), value);
                }
                break;
            case Symbol.ARRAY_INT:
            case Symbol.ARRAY_REAL:
            {
                if (symbolTable.getSymbolValue(getId(id), index).getType() == Symbol.SINGLE_REAL)
                    symbolTable.setSymbolValue(getId(id), value.toReal().getReal(), index);
                else
                if (value.getType() == Symbol.SINGLE_REAL)
                    throw new InterpretException("表达式 <" + id + "> 与变量类型不匹配");
                else
                    symbolTable.setSymbolValue(getId(id), value.getInt(), index);
                break;
            }
            case Symbol.TEMP:
                symbolTable.setSymbolValue(getId(id), value);
                break;
            default:
                break;
        }
    }

    /**
     * 将用户输入的数据转为Value
     * @param str
     * @return
     * @throws InterpretException
     */
    private Value parseValue(String str) throws InterpretException
    {
        if (str.matches("^(-?\\d*)(\\.\\d+)$"))
        {
            Value value = new Value(Symbol.SINGLE_REAL);
            value.setReal(Double.parseDouble(str));
            return value;
        }
        if (str.matches("^(-?\\d+)$"))
        {
            Value value = new Value(Symbol.SINGLE_INT);
            value.setInt(Integer.parseInt(str));
            return value;
        }
        throw new InterpretException("输入非法");
    }

    private Value getValue(String id, InterpreterSymbolTable symbolTable) throws InterpretException
    {
        if (id.matches("(\\+|\\-)?(\\d*\\.\\d*|\\.\\d*)"))
        {
            Value value = new Value(Symbol.SINGLE_REAL);
            value.setReal(Double.parseDouble(id));
            return value;
        }
        if (id.matches("(\\+|\\-)?\\d+"))
        {
            Value value = new Value(Symbol.SINGLE_INT);
            value.setInt(Integer.parseInt(id));
            return value;
        }
        int index = -1;
        if (isArrayElement(id))
            index = getIndex(id, symbolTable);

        return symbolTable.getSymbolValue(getId(id), index);
    }

    /**
     * 判断是否是数组
     * @param id
     * @return
     */
    private boolean isArrayElement(String id) {
        return id.contains("[");
    }

    /**
     * 传入形如xx[xx]或者xx 获取前面的id
     * @param id
     * @return
     */
    private String getId(String id)
    {
        if (isArrayElement(id))
        {
            return id.substring(0, id.indexOf("[")) + "";
        }
        return id;
    }

    /**
     * 传入形如 xx[xx],获取其中的索引值（获取数组大小）
     * @param id
     * @return
     * @throws InterpretException
     */
    private int getIndex(String id, InterpreterSymbolTable symbolTable) throws InterpretException {
        String indexStr = id.substring(id.indexOf("[") + 1, id.length() - 1) + "";
        return getInt(indexStr, symbolTable);
    }

    /**
     * 传入一个字面值或者标识符,获取对应int值
     * @param value
     * @return
     * @throws InterpretException
     */
    private int getInt(String value, InterpreterSymbolTable symbolTable) throws InterpretException {
        if (value.matches("^(-?\\d+)$")) {
            return Integer.parseInt(value);
        }
        Value valueInt;
        if (functionStack.functionStateStack.isEmpty())
            valueInt = symbolTable.getSymbolValue(value);
        else
        {
            Function function = functionStack.functionStateStack.
                    get(functionStack.functionStateStack.size() - 1).getFunction();
            valueInt = function.getInterpreterSymbolTable().getSymbolValue(value);
        }
        if (valueInt.getType() == Symbol.SINGLE_INT) {
            return valueInt.getInt();
        } else {
            throw new InterpretException("不是整数");
        }
    }
    private double getReal(String value, InterpreterSymbolTable symbolTable) throws InterpretException {
        if (value.matches("^(-?\\d+)(\\.\\d+)?$")) {
            return Double.parseDouble(value);
        }
        Value valueReal;
        if (functionStack.functionStateStack.isEmpty())
            valueReal = symbolTable.getSymbolValue(value);
        else
        {
            Function function = functionStack.functionStateStack.
                    get(functionStack.functionStateStack.size() - 1).getFunction();
            valueReal = function.getInterpreterSymbolTable().getSymbolValue(value);
        }
        if (valueReal.getType() == Symbol.SINGLE_REAL) {
            return valueReal.getReal();
        } else {
            throw new InterpretException("不是整数");
        }
    }

    /**
     * 传入一个字面值或者标识符,获取对应double值
     * @param value
     * @return
     * @throws InterpretException
     */
    private double getDouble(String value, InterpreterSymbolTable symbolTable) throws InterpretException
    {
        if (value.matches("^(-?\\d+)(\\.\\d+)?$"))
            return Double.parseDouble(value);
        Value valueDouble;
        if (functionStack.functionStateStack.isEmpty())
            valueDouble = symbolTable.getSymbolValue(value);
        else
        {
            Function function = functionStack.functionStateStack.
                    get(functionStack.functionStateStack.size() - 1).getFunction();
            valueDouble = function.getInterpreterSymbolTable().getSymbolValue(value);
        }
        return valueDouble.toReal().getReal();
    }


    /**
     * 返回当前处于栈顶的函数
     * 通过查找FunctionStateStack（即FunctionStateMark的List）的最后一个元素对应的函数
     * @return Fuction
     */
    public Function getFunctionFromTopOfStack()
    {
        Function function = functionStack.functionStateStack.get(functionStack.functionStateStack.size() - 1).getFunction();
        return function;
    }

    /**
     * 用于将数组functions中的某个被调用函数的函数复制出来
     * @param function_
     * @return
     */
    public Function copyFunction(Function function_)
    {
        Function result = new Function();
        InterpreterSymbolTable interpreterSymbolTable = copySymbolTable(function_.getInterpreterSymbolTable());
        result.setName(function_.getName());
        result.setParams(function_.getParams());
        result.setCodes(function_.getCodes());
        result.setReturnType(function_.getReturnType());
        result.setInterpreterSymbolTable(interpreterSymbolTable);
        result.setPc(function_.getPc());
        result.setReturnMark(function_.getReturnMark());
        result.setReturnValue(function_.getReturnValue());
        return result;
    }

    /**
     * 函数调用时将调用者的符号表复制给被调用者（传参）
     * @param symbolTale_
     * @return
     */
    public InterpreterSymbolTable copySymbolTable(InterpreterSymbolTable symbolTale_)
    {
        if (symbolTale_ == null)
            return null;

        InterpreterSymbolTable result = new InterpreterSymbolTable();
        int symbolNum = symbolTale_.getSymbolList().size();

        String[] name = new String[symbolNum];
        int[] type = new int[symbolNum];
        Integer[] level = new Integer[symbolNum];
        Integer[] value = new Integer[symbolNum];
        Symbol[] symbols = new Symbol[symbolNum];

        Iterator<Symbol> iterators = symbolTale_.symbolList.iterator();
        int i = 0;
        while (iterators.hasNext())
        {
            symbols[i] = iterators.next();
            i++;
        }
        for (int j = 0; j < symbolNum; j++) {
            name[j] = symbols[j].getName();
            type[j] = symbols[j].getType();
            level[j] = symbols[j].getLevel();
            value[j] = symbols[j].getValue().getInt();
            Symbol symbol = new Symbol(name[j], type[j], level[j], value[j]);
            result.addNewVariable(symbol);
        }
        return result;
    }

    /**
     * 获取临时变量名后缀（如传入temp0，得到0）
     * @param tempName
     * @return
     */
    public int getTempPostfix(String tempName)
    {
        int size = tempName.length();
        int result = 0;
        char c[] = tempName.toCharArray();
        for(int a = 4; a < size; a++)
        {
            result += result * 10 + (int)c[a] - 48;
        }
        return result;
    }

    /**
     * 判断变量是否为临时变量temp
     * @param variableName
     * @return
     */
    public boolean startsWithTemp(String variableName)
    {
        if (variableName.length() < 6)
            return false;
        char chars[] = variableName.toCharArray();

        if (chars[0] == 't' && chars[1] == 'e' && chars[2] == 'm' && chars[3] == 'p' && chars[4] == '0')
            return true;
        else
            return false;
    }

}