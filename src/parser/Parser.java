package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private Node tree;//语法树根节点
    private List<Token> tokens;//token list
    private int current;//指针指向当前token
    private int tokenSize;//token list的大小
    private boolean b;//程序是否被正确运行
    public List<Error> errors;//errors list

    //构造函数
    public Parser(String text) {
        tokens = new Lexer(text).getTokens();
        tokenSize=tokens.size();

        current = -1;
        b=true;
        tree = new Node(NodeType.PROGRAM);
        errors=new ArrayList<>();
    }
    //指针向前移动1，取下一个token
    public Node getTree() {
        return tree;
    }

    /**
     * current pointer move forward
     * @return the next token
     */
    //指针向前移动1，取下一个token
    private Token getNextToken() {
        current++;
        if (current <tokenSize) {
            return tokens.get(current);
        }
        return new Token(TokenType.EOF,"EOF",tokens.get(tokenSize-1).line);
    }

    /**
     * current pointer don't move
     * @return the next token
     */
    //指针不动，取下一个token
    private Token nextToken() {
        int temp = current + 1;
        if (temp <tokenSize) {
            return tokens.get(temp);
        }
        return new Token(TokenType.EOF,"EOF",tokens.get(tokenSize-1).line);
    }

    /**
     * current pointer don't move
     * @return the token after the next token
     */
    //指针不动，取下下个token
    private Token next2Token(){
        int temp=current+2;
        if(temp< tokenSize){
            return tokens.get(temp);
        }
        return new Token(TokenType.EOF,"EOF",tokens.get(tokenSize-1).line);
    }

    /**
     * current pointer don't move
     * @return the last token
     */
    //指针不动，取上一个token
    private Token lastToken(){
        int temp=current-1;
        if(temp<tokenSize&&temp>-1){
            return tokens.get(temp);
        }
        return new Token(TokenType.EOF,"EOF",tokens.get(tokenSize-1).line);
    }

    /**
     * match the token by value and add child node to the node
     * @param value the value that need to be matched
     * @param node the node that would be add child node
     * @return true if successfully
     */
    //对于输入，指针前移；对于输出，为语法树增加节点或者为error list增加元素
    //第一种match函数，根据token的value，包含RESERVED_WORD，COMPOSITE_SYMBOL，SIMPLE_SYMBOL
    private boolean matchToken(String value, Node node) {

        Token token = getNextToken();
        String value_t = token.getValue();
        TokenType type_t = token.getType();
        if (value_t.equals(value)) {
            switch (type_t) {
                case RESERVED_WORD:
                    switch (value_t) {
                        case "int":
                            node.addChild(new Node(NodeType.INT,"int",token.line));
                            break;
                        case "real":
                            node.addChild(new Node(NodeType.REAL,"real",token.line));
                            break;
                        case "if":
                            node.addChild(new Node(NodeType.IF,"if",token.line));
                            break;
                        case "else":
                            node.addChild(new Node(NodeType.ELSE,"else",token.line));
                            break;
                        case "while":
                            node.addChild(new Node(NodeType.WHILE,"while",token.line));
                            break;
                        case "read":
                            node.addChild(new Node(NodeType.READ,"read",token.line));
                            break;
                        case "write":
                            node.addChild(new Node(NodeType.WRITE,"write",token.line));
                            break;
                        case "void":
                            node.addChild(new Node(NodeType.VOID,"void",token.line));
                            break;
                        case "return":
                            node.addChild(new Node(NodeType.RETURN,"return",token.line));
                            break;
                        case "continue":
                            node.addChild(new Node(NodeType.CONTINUE,"continue",token.line));
                            break;
                        case "break":
                            node.addChild(new Node(NodeType.BREAK,"break",token.line));
                            break;
                    }
                    break;
                case COMPOSITE_SYMBOL:
                    node.addChild(new Node(NodeType.RELOP, value_t,token.line));
                    break;
                case SIMPLE_SYMBOL:
                    switch (value_t) {
                        case ">":
                        case "<":
                            node.addChild(new Node(NodeType.RELOP,value_t,token.line));
                            break;
                        case "+":
                        case "-":
                            node.addChild(new Node(NodeType.ADDLOP, value_t,token.line));
                            break;
                        case "*":
                        case "/":
                            node.addChild(new Node(NodeType.MULOP,value_t,token.line));
                            break;
                        default:
                            node.addChild(new Node(NodeType.DELIMITER,value_t,token.line));
                    }
                    break;
            }
            b=true;
            return true;
        } else {//value不match，error list增加一个节点，next token should be+预估值
            errors.add(new Error("line "+lastToken().line+" "+lastToken().getValue()+" next token should be:"+value));
            current--;
            b=false;
            return false;
        }

    }

    /**
     * match the token by type and add child node to the node
     * for negative number
     * @param type the type that need to be matched
     * @param node the node that would be add child node
     * @return true if successfully
     */
    //第二种match函数，根据token的type，包含ID，NUM
    private boolean matchToken(TokenType type, Node node,boolean isNegative) {
        Token token=getNextToken();
        if (token.getType() == type) {
            switch (type) {
                case ID:
                    node.addChild(new Node(NodeType.IDENTIFIER, token.getValue(),token.line));
                    break;
                case NUM:
                    Boolean isReal=false;
                    String valueS=token.getValue();
                    //check out of number
                    for(char c: valueS.toCharArray()) {
                        if (c == '.') {
                            isReal=true;
                        }
                    }
                    try{
                        if(isReal)
                        { Float.valueOf(valueS);

                        }else
                        {
                            Integer.valueOf(valueS);
                        }
                    }catch (Exception e)
                    {
                        errors.add(new Error("line "+lastToken().line+":"+lastToken().getValue()+" value is out of bound"));
                        break;
                    }


                    if (isNegative){
                        node.addChild(new Node(NodeType.NUMBER, "-"+token.getValue(),token.line));
                    }else {
                        node.addChild(new Node(NodeType.NUMBER, token.getValue(),token.line));
                    }

                    break;
            }
            b=true;
            return true;
        } else {//type不match，error list增加节点，输出next token should be an identifier/number
            switch (type) {
                case ID:
                    errors.add(new Error("line "+lastToken().line+":"+lastToken().getValue()+" next token should be an identifier"));
                    break;
                case NUM:
                    errors.add(new Error("line "+token.line+" unexpected token:"+token.getValue()));
                    break;
            }
            current--;
        }
        b=false;
        return false;
    }


    /**
     * match the token by type and add child node to the node
     * @param type the type that need to be matched
     * @param node the node that would be add child node
     * @return true if successfully
     */
    //第二种match函数，根据token的type，包含ID，NUM
    private boolean matchToken(TokenType type, Node node) {
        Token token=getNextToken();
        if (token.getType() == type) {
            switch (type) {
                case ID:
                    node.addChild(new Node(NodeType.IDENTIFIER, token.getValue(),token.line));
                    break;
                case NUM:
                    Boolean isReal=false;
                    String valueS=token.getValue();
                    //check out of number
                    for(char c: valueS.toCharArray()) {
                        if (c == '.') {
                            isReal=true;
                        }
                    }
                        try{
                            if(isReal)
                            { Float.valueOf(valueS);

                            }else
                            {
                                Integer.valueOf(valueS);
                            }
                        }catch (Exception e)
                        {
                            errors.add(new Error("line "+lastToken().line+":"+lastToken().getValue()+" value is out of bound"));
                            break;
                        }
                    node.addChild(new Node(NodeType.NUMBER, token.getValue(),token.line));
                    break;
            }
            b=true;
            return true;
        } else {//type不match，error list增加节点，输出next token should be an identifier/number
            switch (type) {
                case ID:
                    errors.add(new Error("line "+lastToken().line+":"+lastToken().getValue()+" next token should be an identifier"));
                    break;
                case NUM:
                    errors.add(new Error("line "+token.line+" unexpected token:"+token.getValue()));
                    break;
            }
            current--;
        }
        b=false;
        return false;
    }

    /**
     * the entrance of program
     * BNF：program → declaration-list
     */
    //入口可以分为四种情况，判断并调用分别的函数
    public void parse() {
        while (true) {
            //取第一个字符
            String value = nextToken().getValue();
            char start=value.charAt(0);
            //case1：如果是空文件，返回EOF
            if(nextToken().getType()==TokenType.EOF) return;
            //case2：如果是int,real,voidl类型，那么调用声明函数declaration()
            if (value.equals("int") || value.equals("real") || value.equals("void")) {
                tree.addChild(declaration());
            }
            //case3：如果是数字、字母，那么调用语句函数statement()
            else if((start>='a' && start<='z') ||  (start>='A' && start<='Z'))
            {
                tree.addChild(statement());
            }
            //case4：如果以上都不是，那么返回error list增加节点，unexpected token
            else {
                errors.add(new Error("line "+nextToken().line+" unexpected token:"+value));
                break;
            }
        }
    }

    /**
     * BNF:declaration-list → declaration-list declaration | declaration
     * declaration → var-declaration | fun-declaration
     * var-declaration → type-specifier ID ; | type-specifier ID [ NUM ] ;
     * type-specifier → int | void| real
     * fun-declaration → type-specifier ID ( params ) compound-stmt
     * params → param-list | void
     * param-list → param-list , param | param
     * param → type-specifier ID | type-specifier ID [ ]
     * @return declaration node
     */
    //声明语句（变量声明+函数声明）
    private Node declaration() {
        Node node = new Node(NodeType.DECLARATION);
        //1）类型的保留字int，real，void
        switch (nextToken().getValue()) {
            case "int":
                matchToken("int", node);
                break;
            case "real":
                matchToken("real", node);
                break;
            case "void":
                matchToken("void", node);
                break;
        }//0
        //2）下一个token是标识符，匹配掉
        matchToken(TokenType.ID, node);//1
        /*
        3）下一个分情况
        如果是逗号，：还要match掉后面的ID和;，比如int a,b,c;
        如果是分号；：比如说 int a;
        如果是等号=：要调用expression()，并且match掉；，比如int a=1234;
        如果是数组[:调用expression()，消耗掉数组大小；并且判断有没有赋值，有的话还要调用expression()。比如int a[5]={1,2,3,4,5}
        如果是函数(:讨论函数是否有参数，有参数的需要消耗掉参数类型和参数ID，match掉函数)后，调用语句序列compoundStmt()
        不属于以上任意一个：
        如果文件读尽：expected token:；提示应该有；
        如果文件未读尽：unexpected token:提示值不对
        */
        switch (nextToken().getValue()) {
            //如果是逗号，：还要match掉后面的ID和;，比如int a,b,c;
            case ",":
                //多变量声明
                while (nextToken().getValue().equals(","))
                {
                    matchToken(",", node);
                    matchToken(TokenType.ID, node);//1

                    if(nextToken().getValue().equals("="))
                    {
                        matchToken("=",node);//
                        node.addChild(expression());
                    }
                }

                matchToken(";", node);//4
                break;
            //如果是分号；：比如说 int a;
            case ";":
                matchToken(";", node);
                break;
            //如果是等号=：要调用expression()，并且match掉；，比如int a=1234;
            case "=":
                matchToken("=",node);//2
//                matchToken(TokenType.NUM, node);//3
                node.addChild(expression());


                while (nextToken().getValue().equals(","))
                {
                    matchToken(",", node);
                    matchToken(TokenType.ID, node);//1

                    if(nextToken().getValue().equals("="))
                    {
                        matchToken("=",node);//
                        node.addChild(expression());
                    }
                }

                matchToken(";", node);//4
                break;
            //如果是数组[:调用expression()，消耗掉数组大小；并且判断有没有赋值，有的话还要调用expression()。比如int a[5]={1,2,3,4,5}
            case "[":
                //一位数组或者多维数组int a[3][4]
                while (nextToken().getValue().equals("["))
                {
                    matchToken("[", node);//2
                    node.addChild(expression());
                    matchToken("]", node);//4
                }
                //数组赋值
                //int a[3]={1，2，3}
                if(nextToken().getValue().equals("="))//5
                {
                    matchToken("=", node);//4
                    matchToken("{", node);//6
                    node.addChild(expression());
                    while (nextToken().getValue().equals(",")) {
                        matchToken(",", node);//4
                        node.addChild(expression());
                    }
                    matchToken("}", node);//4
                }
                    matchToken(";", node);//5
                break;
            case "("://2
                matchToken("(", node);
                //如果是函数没有参数，int a(){}
                if(nextToken().getValue().equals(")")){
                    matchToken(")",node);
                }
                //如果是函数有参数，int a(int/real b){}
                else {
                    switch (nextToken().getValue()) {
                        case "int":
                            matchToken("int", node);
                            break;
                        case "real":
                            matchToken("real", node);
                            break;
                        default: {
                            errors.add(new Error("line " + nextToken().line + " unexpected token:" + nextToken().getValue()));
                        }
                    }
                    //match掉函数参数的标识符
                    matchToken(TokenType.ID, node);
                    //如果参数是数组
                    //void myFunction(int param[]){}
                    if (nextToken().getValue().equals("[")) {
                    if (nextToken().getValue().equals("["))
                        matchToken("[", node);
                        matchToken("]", node);
                    }
                    //match掉函数的一系列用逗号分隔的参数
                    while(nextToken().getValue().equals(",")){
                        matchToken(",",node);
                        switch (nextToken().getValue()) {
                            case "int":
                                matchToken("int", node);
                                break;
                            case "real":
                                matchToken("real", node);
                                break;
                            default: {
                                errors.add(new Error("line " + nextToken().line + " unexpected token:" + nextToken().getValue()));
                            }
                        }
                        matchToken(TokenType.ID, node);
                        if (nextToken().getValue().equals("[")) {
                            matchToken("[", node);
                            matchToken("]", node);
                        }
                    }
                    //match掉函数右括号
                    matchToken(")",node);
                }
                //调用语句序列compoundStmt()
                node.addChild(compoundStmt());
                break;
                default:{
                    if(nextToken().getType()==TokenType.EOF){
                        errors.add(new Error("line "+nextToken().line+" expected token:"+";"));
                    }else{
                        errors.add(new Error("line "+nextToken().line+" unexpected token:"+nextToken().getValue()));
                    }
                }
        }
        return node;
    }




    /**
     * BNF:compound-stmt → { local-declarations statement-list }
     * @return compoundStmt node
     */
    //语句序列
    private Node compoundStmt() {
        Node node = new Node(NodeType.COMPOUND_STMT);
        //1）match掉{
        matchToken("{", node);//0
        boolean flag=true;
        //2）没遇到对应的}前
        while (!nextToken().getValue().equals("}")&&b&&flag) {
            //case1：看type。如果是ID或者NUM，调用expressionStmt()
            if(nextToken().getType()==TokenType.ID||nextToken().getType()==TokenType.NUM){
                node.addChild(expressionStmt());//1 or ...
            }
            /*
            case2：看value。
            如果是int/real:调用declaration()
            如果是read：调用readStmt()
            如果是print：调用printStmt()
            如果是if：调用ifStmt()
            如果是while：调用whileStmt()
            如果是return：调用returnStmt()
            如果是{：调用compoundStmt()
            其他的，error list增加unexpected token节点
            */
            else {
                switch (nextToken().getValue()) {
                    case "int":
                    case "real":
                        node.addChild(declaration());//1 or ...
                        break;
                    case "read":
                        node.addChild(readStmt());
                        break;
                    case "write":
                        node.addChild(writeStmt());
                        break;
                    case "if":
                        node.addChild(ifStmt());
                        break;
                    case "while":
                        node.addChild(whileStmt());
                        break;
                    case "return":
                        node.addChild(returnStmt());
                        break;
                    case "continue":
                        node.addChild(continueStmt());
                        break;
                    case "break":
                        node.addChild(breakStmt());
                        break;
                    case "{":
                        node.addChild(compoundStmt());
                        break;
                        default:{
                            flag=false;
                            errors.add(new Error("line "+nextToken().line+" unexpected token:"+nextToken().getValue()));
                        }
                }
            }
        }
        //消耗掉}
        matchToken("}",node);
        return node;
    }

    /**
     * BNF:statement → expression-stmt | compound-stmt | selection-stmt | iteration-stmt | return-stmt
     * @return statement node
     */
    //statement
    private Node statement(){
        Node node=new Node(NodeType.STATEMENT);
        //case1：看type。如果是ID或者NUM，调用expressionStmt()
        if(nextToken().getType()==TokenType.ID || nextToken().getType()==TokenType.NUM){
            node.addChild(expressionStmt());
        }
        /*
            case2：看value。
            注：statement不包含int/real
            如果是if：调用ifStmt()
            如果是while：调用whileStmt()
            如果是return：调用returnStmt()
            如果是continue：调用continueStmt()
            如果是print：调用printStmt()
            如果是break：调用breakStmt()
            如果是{：调用compoundStmt()
            如果是(：调用expressionStmt()
            其他的，error list增加unexpected token节点
            */
        else {
            switch (nextToken().getValue()) {
                case "if":
                    node.addChild(ifStmt());
                    break;
                case "while":
                    node.addChild(whileStmt());
                    break;
                case "return":
                    node.addChild(returnStmt());
                    break;
                    case "write":
                    node.addChild(writeStmt());
                    break;
                case "continue":
                    node.addChild(continueStmt());
                    break;
                case "break":
                    node.addChild(breakStmt());
                    break;
                case "{":
                    node.addChild(compoundStmt());
                    break;
                case "(":
                    node.addChild(expressionStmt());
                    break;
                    default:{
                        errors.add(new Error("line "+nextToken().line+" unexpected token:"+nextToken().getValue()));
                    }
            }
        }
        return node;
    }

    /**
     *BNF:read-stmt→ read (var-list);
     * @return readStmt node
     */
    //read statement
    private Node readStmt(){
        Node node=new Node(NodeType.READ_STMT);
        matchToken("read",node);//0
        matchToken("(",node);
        //match掉变量或者变量序列,调用var()
        node.addChild(var());//2
        while(nextToken().getValue().equals(",")){
            matchToken(",",node);
            node.addChild(var());
        }
        matchToken(")",node);
        matchToken(";",node);
        return node;
    }

    /**
     * BNF:write-stmt→ write(expression-list);
     * @return writeStmt node
     */
    //print statement
    private Node writeStmt(){
        Node node=new Node(NodeType.WRITE_STMT);
        matchToken("write",node);//0
        matchToken("(",node);//1
        //match掉表达式或者表达式序列,调用expression()
        node.addChild(expression());//2
        while(nextToken().getValue().equals(",")){
            matchToken(",",node);//3
            node.addChild(expression());//4
        }
        matchToken(")",node);
        matchToken(";",node);
        return node;
    }


    /**
     *BNF:if-stmt→ if ( expression ) statement | if ( expression ) statement else statement
     * @return ifStmt node
     */
    //if statement
    private Node ifStmt(){

        Node node=new Node(NodeType.IF_STMT);
        matchToken("if",node);//0
        matchToken("(",node);
        node.addChild(expression());
        matchToken(")",node);
        node.addChild(statement());//4

        Token a=next2Token();
        Token b=nextToken();
        while (nextToken().getValue().equals("else") && next2Token().getValue().equals("if") ){//else if
            matchToken("else",node);//5
            matchToken("if",node);//6
            matchToken("(",node);
            node.addChild(expression());
            matchToken(")",node);
            node.addChild(statement());//7

        }
        //判断是否有else部分
        if(nextToken().getValue().equals("else"))//else
        {
            matchToken("else",node);//5
            node.addChild(statement());//6
        }

        return node;
    }

    /**
     * BNF:while-stmt→ while ( expression ) statement
     * @return whileStmt node
     */
    //while statement
    private Node whileStmt(){
        Node node=new Node(NodeType.WHILE_STMT);
        matchToken("while",node);//0
        //match掉表达式,调用expression()
        matchToken("(",node);
        node.addChild(expression());//2
        matchToken(")",node);
        if (nextToken().getValue()==";")
        {
            getNextToken();

        }else {
                node.addChild(statement());//4
        }

        return node;
    }

    /**
     * BNF:expression-stmt → expression ; | ;
     * @return expressionStmt node
     */
    //expression statement
    private Node expressionStmt(){
        Node node=new Node(NodeType.EXPRESSION_STMT);
        if(!nextToken().getValue().equals(";")){
            node.addChild(expression());
        }
        matchToken(";",node);
        return node;
    }

    /**
     * BNF:return-stmt → return ; | return expression ;
     * @return returnStmt node
     */
    //continue statement
    private Node continueStmt(){
        Node node=new Node(NodeType.CONTINUE_STMT);
        matchToken("continue",node);
        matchToken(";",node);
        return node;
    }



    /**
     * BNF:return-stmt → return ; | return expression ;
     * @return returnStmt node
     */
    //break statement
    private Node breakStmt(){
        Node node=new Node(NodeType.BREAK_STMT);
        matchToken("break",node);
        matchToken(";",node);
        return node;
    }

    /**
     * BNF:return-stmt → return ; | return expression ;
     * @return returnStmt node
     */
    //return statement
    private Node returnStmt(){
        Node node=new Node(NodeType.RETURN_STMT);
        matchToken("return",node);
        if(!nextToken().getValue().equals(";")){
            node.addChild(expression());
        }
        matchToken(";",node);
        return node;
    }

    /**
     * BNF:expression → var = expression | simple-expression
     * @return expression node
     */
    //包含函数调用表达式和简单表达式
    private Node expression(){
        Node node=new Node(NodeType.EXPRESSION);
        //如果第一个token是标识符
        if(nextToken().getType()==TokenType.ID) {
            //如果再下一个token是(,则为函数调用，为函数的参数调用simpleExpression()
            if (next2Token().getValue().equals("(")) {//这个是call
                node.addChild(simpleExpression());
            } else {
                //如果不是函数调用，那就是这种形式var = xxx，为xxx调用expression()
                int temp = current;
                Node varNode = var();
                if (nextToken().getValue().equals("=")) {//这个是 var = xxx
                    node.addChild(varNode);
                    matchToken("=", node);
                    node.addChild(expression());
                }else{ //这个就是最简单的表达式，但是是变量开头的
                    current=temp;
                    node.addChild(simpleExpression());
                }
            }
        }else{//这个就是最简单的表达式，开头不是变量
            node.addChild(simpleExpression());
        }



        return node;
    }

    /**
     * BNF:simple-expression → additive-expression relop additive-expression | additive-expression
     * @return simpleExpression node
     */
    //简单表达式
    private Node simpleExpression(){
        Node node=new Node(NodeType.SIMPLE_EXPRESSION);
        node.addChild(additiveExpression());
        String value=nextToken().getValue();
        //逻辑运算符 <,>,COMPOSITE_SYMBOL(Lexer文件，token type)
        while(nextToken().getType()==TokenType.COMPOSITE_SYMBOL||value.equals("<")||value.equals(">")){
            matchToken(value,node);
            node.addChild(additiveExpression());
            value=nextToken().getValue();
        }
        return node;
    }

    /**
     * BNF:additive-expression → additive-expression addop term | term
     * @return additiveExpression node
     */
    //加性表达式
    private Node additiveExpression(){
        Node node=new Node(NodeType.ADDITIVE_EXPRESSION);
        //增加项节点term
        node.addChild(term());//0
        //加性操作符+，-
        String value=nextToken().getValue();
        while(value.equals("+")||value.equals("-")){
            matchToken(value,node);//1
            //+-后面的项term
            node.addChild(term());//2
            value=nextToken().getValue();
        }
        return node;
    }

    /**
     * BNF:var → ID | ID [ expression ]
     * @return var node
     */
    /*
    <变量> -> ID | ID [expression]
    */
    private Node var(){
        Node node=new Node(NodeType.VAR);
        //普通变量
        matchToken(TokenType.ID,node);//0
        //数组变量
        while (nextToken().getValue().equals("[")){
            matchToken("[",node);//1
//            matchToken(TokenType.NUM,node);
            node.addChild(expression());//2
            matchToken("]",node);
        }
        return node;
    }

    /**
     * BNF:term → term mulop factor | factor
     * @return term node
     */
    //term
    private Node term(){
        Node node=new Node(NodeType.TERM);
        node.addChild(factor());
        String value=nextToken().getValue();
        //乘性运算符
        while(value.equals("*")||value.equals("/")){
            matchToken(value,node);
            node.addChild(factor());
            value=nextToken().getValue();
        }
        return node;
    }





    /**
     * BNF:factor → ( expression ) | var | call | NUM
     * @return factor node
     */
    private Node factor(){
        Node node=new Node(NodeType.FACTOR);
        //case1：如果第一个是ID
        if(nextToken().getType()==TokenType.ID){
            //判断ID下一个是不是(,也就是判断是不是函数调用，是的话调用call
            if(next2Token().getValue().equals("(")){
                node.addChild(call());
            }
            //如果没有(,则不是函数调用，调用var
            else{
                node.addChild(var());
            }
        }
        //case2：如果第一个是NUM，匹配掉
        else if(nextToken().getType()==TokenType.NUM){
            matchToken(TokenType.NUM,node);
        }
        //如果第一个(
        else if(nextToken().getValue().equals("(")){
            matchToken("(",node);
            node.addChild(expression());
            matchToken(")",node);
        }
        else if (nextToken().getValue().equals("+")  )
        {
            getNextToken();
            matchToken(TokenType.NUM,node);
        }else if(nextToken().getValue().equals("-"))
        {
            getNextToken();
            matchToken(TokenType.NUM,node,true);
        }
        else{
            errors.add(new Error("line "+nextToken().line+" unexpected token:"+nextToken().getValue()));
        }
        return node;
    }

    /**
     * BNF:call → ID ( args )
     * @return call node
     */
    private Node call(){
        Node node=new Node(NodeType.CALL);
        matchToken(TokenType.ID,node);//0
        matchToken("(",node);//1
        while(!nextToken().getValue().equals(")")&&b){
            node.addChild(expression());//2
            while(nextToken().getValue().equals(",")){
                matchToken(",",node);//3
                node.addChild(expression());//4
            }
        }
        matchToken(")",node);//2 or ...
        return node;
    }

    /**
     * print information about node
     * @param node specify node
     * @param s
     * @return the information about node
     */
    private String printNode(Node node,String s){
        StringBuffer sb=new StringBuffer();
        sb.append(node.toString());
        sb.append("\n");
        if(!node.getChild().isEmpty()){
            for(Node n:node.getChild()){
                String s1=s+"\b";
                sb.append(s1);
                sb.append(printNode(n,s1));
            }
        }
        return sb.toString();
    }


    public String printTreeRe(Node node,StringBuffer sb,String layerPrefix,Boolean isLastChild)
    {


        sb.append(layerPrefix+arrow+node.toString());
        sb.append("\n");
        //如果是最后一个孩子，没有竖线
        if(isLastChild)
        {
            layerPrefix=layerPrefix.substring(0,layerPrefix.length()-3)+"   ";
        }

        if(!node.getChild().isEmpty()){
            String nextLayerPrefix="";
            Boolean lastChild=false;//标志
            //有多个孩子，就有竖线
            if(node.getChild().size()>1)
            {
                nextLayerPrefix=layerPrefix+"  |";
            }
            //没有多个孩子，就没竖线，只是空格
            else {
                nextLayerPrefix=layerPrefix+"   ";
            }
            for(Node n:node.getChild()){
                if (n == node.getChild().get(node.getChild().size()-1))
                {
                    lastChild=true;//如果是最后一个孩子，没有竖线
                }
                printTreeRe(n,sb,nextLayerPrefix,lastChild);
            }
        }
        return sb.toString();
    }

    private String arrow="-->";

    public String printTree()
    {
        Node node=this.tree;
        StringBuffer sb=new StringBuffer();
        sb.append(node.toString());
        sb.append("\n");
        //如果不是空的
        if(!node.getChild().isEmpty()){
            String nextLayerPrefix="";
            Boolean lastChild=false;
            if(node.getChild().size()>1)//多个孩子
            {
                nextLayerPrefix="  |";
            }else {
                nextLayerPrefix="   ";//一个孩子
            }
            for(Node n:node.getChild()){
                if (n == node.getChild().get(node.getChild().size()-1))//最后一个孩子
                {
                    lastChild=true;
                }
                printTreeRe(n,sb,nextLayerPrefix,lastChild);
            }
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * the entrance of grammer analyzer
     * @return information about grammer tree
     */
    public String analyze(){
        parse();
        return printNode(tree,"");
    }

    /**
     *
     * @return information about error
     */
    public String printError(){
        StringBuffer sb=new StringBuffer();
        for(Error e: errors){
            sb.append(e.getMessage()).append("\n");
        }
        return sb.toString();
    }
    public static void main(String[] args){

        String fileName="D:\\Users\\Raine\\Desktop\\test\\expr2_test_reduce.txt";
        StringBuffer text=new StringBuffer();
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                text.append(str+'\n');
            }
            bf.close();
            fr.close();
            } catch (IOException ex) {
            ex.printStackTrace();
        }

        String textS=text.toString();
        Parser parser=new Parser(textS);
        //语法分析
        parser.parse();
        String result=parser.printNode(parser.tree,"");
//        System.out.print(result);
        parser.printTree();
        //输出error list
        for(Error e: parser.errors){
            System.out.println(e+"");
        }
    }
}
