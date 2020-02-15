package semantics;

import function.Function;
import parser.Node;
import parser.NodeType;
import parser.Parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Semantic {
    //每一个函数由对应的语义对象用来分析
    //四元式链表，一个语义对象有一个链表
    public  LinkedList<FourCode> codes;
    private int level;
    private int line;
    private Node tree;
    //符号表
    private SemanticSymbolTable table;
    //函数表
    public ArrayList<Function> functions;
    //临时符号链表
    private LinkedList<Symbol> temps;
    //用于代码回填
    private Stack<Integer> whileheads;//while循环的头部四元式的地址
    private Stack<FourCode> whilebreaks;//break的四元式
    private Stack<Integer> breakNums;//记录一个while层次当中的break数目

    public Semantic(Node tree){
        level=0;
        line=-1;
        this.tree=tree;
        codes=new LinkedList<FourCode>();
        table=new SemanticSymbolTable();
        functions=new ArrayList<Function>();
        temps=new LinkedList<Symbol>();
        whileheads=new Stack<Integer>();
        whilebreaks=new Stack<FourCode>();
        breakNums=new Stack<Integer>();

    }


    //取全部四元式，但是不执行
    public String getCodes(){
        int lineNo=0;
        String code = "";
        //【case1：全局变量的四元式】
        for(int i = 0; i < codes.size(); i++){
            //行号+四元式+换行
            code += String.format("%d:",lineNo) + codes.get(i).toString() + "\n";
            //行号++
            lineNo++;
        }
        //【case2：main函数中的四元式】
        for (Function func: functions) {
            if (func.getName().equals("main"))
                for(int i = 0; i < func.getCodes().size(); i++){
                    //行号+四元式+换行
                    code +=String.format("%d:",lineNo)+ func.getCodes().get(i).toString() + "\n";
                    lineNo++;
                }
        }
        return code;
    }


    //取临时符号链表
    public LinkedList<Symbol> getTemps() {
        return temps;
    }

    //取临时符号temp+i，遍历使不重名
    private Symbol getTempItem(){
        String temp=null;
        //遍历temp+i,temp1,temp2,temp3...
        for(int i=1;;i++){
            temp="temp"+i;
            boolean exist=false;//默认不存在

            //【case1:遍历temps链表看是否有同名temp+i】
            for(Symbol symbol : temps){
                //如果有同名temp+i
                if(symbol.getName().equals(temp)){
                    exist=true;//存在
                    break;//跳出本层for循环
                }
            }
            //【case2:遍历符号表看是否有同名temp+i】
            for(Symbol symbol :table.getTable()){
                //如果有同名temp+i
                if(symbol.getName().equals(temp)){
                    exist=true;//存在
                    break;//跳出本层for循环
                }
            }
            //如果存在，则跳出for循环，查找temp+（i+1）是否存在
            if(exist){
                continue;
            }
            //【如果既不在temps链表里面，也不在符号表table里面，则为temp增加这个tempi】
            Symbol symbol =new Symbol(temp, Symbol.TEMP);
            temps.add(symbol);
            return symbol;
        }

    }

    //【四元式链表】中增加四元式
    private void addCode(FourCode fc){
        //向四元式链表中增加四元式
        codes.add(fc);
        //行数++
        line++;
        //如果遇到IN，level++
        if(fc.op.equals(FourCode.IN)){
            level++;
        }
        //如果遇到OUT，销毁scope(即销毁这个level的symbol，添加NextSymbol进符号表)，level--
        if(fc.op.equals(FourCode.OUT)){
            //销毁scope，即销毁这个level的symbol，添加NextSymbol进符号表
            table.endScope(level);
            level--;
        }
    }

    //向【函数表】增加函数对象
    private void addFunction(Function f){
        functions.add(f);
    }

    //向【符号表】增加symbol
    private void addItem(Node node,int type) throws SemException{
        String name=node.getValue();
        Symbol symbol =new Symbol(name,type,level);
        //error，抛异常（如果符号表中的同名symbol优先级反而高）
        boolean b=table.enter(symbol);
        if(!b){
            throw new SemException("line "+node.getLine()+": identifier "+name+" has been declared");
        }
    }

    //根据node类型分别生成四元式
    private void generate(Node node) throws SemException{
        switch (node.getType()){
            //case1:statement
            case STATEMENT:
                //statement的第一个孩子子节点
                Node stmtNode=node.getChild().get(0);
                switch (stmtNode.getType()){
                    //case1.1:IF_STMT
                    case IF_STMT:
                        ifStmt(stmtNode);
                        break;
                    case WHILE_STMT:
                        whileStmt(stmtNode);
                        break;
                    case READ_STMT:
                        readStmt(stmtNode);
                        break;
                    case WRITE_STMT:
                        writeStmt(stmtNode);
                        break;
                    case EXPRESSION_STMT:
                        if(stmtNode.getChild().size()>1) {
                            expression(stmtNode.getChild().get(0));
                        }
                        break;
                    case COMPOUND_STMT:
                        compoundStmt(stmtNode,false);
                        break;
                    case RETURN_STMT:
                        returnStmt(stmtNode);
                        break;
                }
                break;
            //case2:declaration
            case DECLARATION:
                declaration(node);
                break;
            //case3:IF_STMT
            case IF_STMT:
                ifStmt(node);
                break;
            //case4:WHILE_STMT
            case WHILE_STMT:
                whileStmt(node);
                break;
            //case5:READ_STMT
            case READ_STMT:
                readStmt(node);
                break;
            //case6:WRITE_STMT
            case WRITE_STMT:
                writeStmt(node);
                break;
            //case7:EXPRESSION_STMT
            case EXPRESSION_STMT:
                if(node.getChild().size()>1) {
                    expression(node.getChild().get(0));
                }
                break;
            //case8:COMPOUND_STMT
            case COMPOUND_STMT:
                compoundStmt(node,false);
                break;
            //case9:RETURN_STMT
            case RETURN_STMT:
                returnStmt(node);
                break;
            //case10:BREAK_STMT
            case  BREAK_STMT:
                breakStmt(node);
                break;
            //case11:CONTINUE_STMT
            case CONTINUE_STMT:
                continueStmt(node);
                break;
        }
    }

    //continue语句，直接跳转到while的condition部分
    private void continueStmt(Node node)throws SemException
    {
        addCode(new FourCode(FourCode.JMP,null,null,whileheads.peek()+""));
    }

    //break语句，
    private void breakStmt(Node node)throws SemException
    {
        //result需要代码回填
        FourCode breakJmp=new FourCode(FourCode.JMP,null,null,null);
        addCode(breakJmp);
        //当前while的break数量+1，breakNum取出加一再放回
        int breakNum=breakNums.pop();
        breakNums.push(breakNum+1);
        whilebreaks.add(breakJmp);
    }

    //if语句
    //三部分：if-else_if-else
    private void ifStmt(Node node)throws SemException{
        List<Node> children=node.getChild();
        ArrayList<FourCode> falseJumpList=new ArrayList<FourCode>();
        ArrayList<FourCode> outJumpList=new ArrayList<FourCode>();


        //【part1】：if
        //falseJmp:if_condition错了才jump，没错就顺序执行
        FourCode falseJmp=new FourCode(FourCode.JMP,expression(children.get(2)),null,null);
        addCode(falseJmp);
        falseJumpList.add(falseJmp);

        //进入if(){}代码块
        addCode(new FourCode(FourCode.IN,null,null,null));
        //为{}内的代码生成四元式
        generate(node.getChild().get(4));
        //出if(){}代码块
        addCode(new FourCode(FourCode.OUT,null,null,null));

        FourCode outJump;
        //else or else if or none
        int index=5;

        //【part2】：else if
        while ( index<children.size()-1 &&   children.get(index).getValue().equals("else") && children.get(index+1).getValue().equals("if"))
        {
            //代码回填，把上一个if或者else if的result回填，调到自己这个else if这里，写的是当前行line+1
            outJump=new FourCode(FourCode.JMP,null,null,null);
            addCode(outJump);
            outJumpList.add(outJump);

            falseJumpList.get(falseJumpList.size()-1).result=String.valueOf(line+1);


            int boolexIndex=index+3;//else if的expression
            //else if错了就jump，调到else{}的第一个语句。例如，10:(jmp, temp2, null, 15)
            falseJmp=new FourCode(FourCode.JMP,expression(children.get(boolexIndex)),null,null);
            addCode(falseJmp);
            falseJumpList.add(falseJmp);

            //进入else if(){}代码块
            addCode(new FourCode(FourCode.IN,null,null,null));
            //为{}内的代码生成四元式
            generate(node.getChild().get(boolexIndex+2));
            //出else if(){}代码块
            addCode(new FourCode(FourCode.OUT,null,null,null));
            index=boolexIndex+3;
        }

        //【part3】：else
        //如果有else
        if( index<=children.size()-1 &&  children.get(index).getValue().equals("else") ){
            //如果前面有满足的if或者else if，则跳出if-else_if-else
            outJump=new FourCode(FourCode.JMP,null,null,null);
            addCode(outJump);
            outJumpList.add(outJump);
            //为else if的falsejmp代码回填
            falseJmp.result=String.valueOf(line+1);
            //进入else{}代码块
            addCode(new FourCode(FourCode.IN,null,null,null));
            //为{}内的代码生成四元式
            generate(node.getChild().get(index+1));
            //出else{}代码块
            addCode(new FourCode(FourCode.OUT,null,null,null));
            //为outjmp的result代码回填
            for(FourCode outjmp : outJumpList)
            {
                outjmp.result=String.valueOf(line+1);
            }
        }
        //如果没有else
        else{
            //为else if的falsejmp代码回填
            falseJmp.result=String.valueOf(line+1);
        }
    }

    //while
    private void whileStmt(Node node) throws SemException{
        //doline为将要构造的四元式行号，line为当前function写完的一行
        int doline=line+1;
        //把while的condition部分【压入栈中】
        whileheads.push(doline);
        //初始化这一层while的break次数为0
        int breakNum=0;
        //把while的breakNums部分【压入栈中】
        breakNums.push(breakNum);


        //不满足while condition的【jump四元式】，需回填result
        FourCode falsejmp=new FourCode(FourCode.JMP,expression(node.getChild().get(2)),null,null);
        addCode(falsejmp);
        //进入while(){}
        addCode(new FourCode(FourCode.IN,null,null,null));
        generate(node.getChild().get(4));
        //出while(){}
        addCode(new FourCode(FourCode.OUT,null,null,null));
        //【跳转到当前行的while condition部分】
        addCode(new FourCode(FourCode.JMP,null,null,whileheads.peek()+""));
        //代码回填result，不满足condition直接跳出while代码块
        falsejmp.result=String.valueOf(line+1);

        //得到while(){}里的【break】数量
        breakNum=breakNums.pop();
        for(int i=0;i<breakNum;i++)
        {
            //为每一个break生成break四元式
            FourCode breakJmp=whilebreaks.pop();
            //为每一个break回填result
            breakJmp.result=String.valueOf(line+1);
        }

        //while结束，while condition出栈
        whileheads.pop();
    }


    //read读一个数进来
    private void readStmt(Node node){
        //拿到变量名
        String var_name=node.getChild().get(2).getChild().get(0).getValue();
        //根据变量类型添加四元式（普通变量、数组）
        switch (table.getItemType(var_name)){
            case Symbol.INT:
            case Symbol.REAL:
                addCode(new FourCode(FourCode.READ,null,null,var_name));
                break;
            case Symbol.ARRAY_INT:
            case Symbol.ARRAY_REAL:
                //算数组的维度大小
                int index=1;
                List<Node> children=node.getChild().get(2).getChild();
                int sum=0;
                while (index<children.size() && children.get(index).getValue().equals("["))
                {
                    index=index+3;
                    sum++;
                }
                //change index position to [
                Node add=new Node(NodeType.ADDITIVE_EXPRESSION);
                index=1;
                int count=0;
                Node term_node=null;
                //构建新的语法树，实现加法的计算
                while (index<children.size() && children.get(index).getValue().equals("["))
                {
                    term_node=new Node(NodeType.TERM);
                    //calculate allocation of memory
                    Node factor=new Node(NodeType.FACTOR);
                    term_node.addChild(factor);

                    Node DELIMITER1=new Node(NodeType.DELIMITER,"(",0);
                    Node expression=children.get(index+1);
                    Node DELIMITER2=new Node(NodeType.DELIMITER,")",0);
                    factor.addChild(DELIMITER1);
                    factor.addChild(expression);
                    factor.addChild(DELIMITER2);



                    add.addChild(term_node);
                    if(children.get(index).getValue().equals("["))
                    {
                        int index_small=sum-1-count;
                        //构建新的语法树，实现乘法的计算
                        while (index_small>0)
                        {
                            term_node.addChild(new Node(NodeType.MULOP,"*",0));
                            Node factor_=new Node(NodeType.FACTOR);
                            Node var_=new Node(NodeType.VAR," ",0);
                            Node iden_=new Node(NodeType.IDENTIFIER,var_name+"_"+Integer.toString(sum-index_small)+"_index_record" ,0);
                            var_.addChild(iden_);
                            factor_.addChild(var_);
                            term_node.addChild(factor_);
                            index_small--;
                        }

                        add.addChild(new Node(NodeType.ADDLOP,"+",0));
                    }

                    index=index+3;
                    count++;
                }

                String temp;
                //如果是一维数组
                if (sum==1)
                {
                    temp=term(term_node);
                }
                //如果不是一维数组，算出真实的位置
                else
                {
                    temp=addctiveExpression(add);
                }
                addCode(new FourCode(FourCode.READ,null,null,var_name+"["+temp+"]"));
                break;
        }
    }

    //write
    private void writeStmt(Node node){
        addCode(new FourCode(FourCode.WRITE,null,null,expression(node.getChild().get(2))));
        int o_size=node.getChild().size()-5;//存在多个exp o_size=#exp*2 不存在则o_size=0
        //write多个，例如：write(a,c);
        for(int i=0;i<o_size;i=i+2){
            addCode(new FourCode(FourCode.WRITE,null,null,expression(node.getChild().get(4+i))));
        }
    }


    //声明
    //函数声明+变量声明（单一变量声明、赋值+数组声明、赋值）
    private void declaration(Node node) throws SemException {
        //type
        NodeType nt=node.getChild().get(0).getType();
        //case1:如果是int，得到四元式类型int，添加symbol类型int
        String fourCodeType=FourCode.INT;
        int symbolType=Symbol.INT;
        //case2:如果是real，得到四元式类型real，添加symbol类型real
        if(nt== NodeType.REAL) {
            fourCodeType=FourCode.REAL;
            symbolType=Symbol.REAL;
        }

        //得到第一个identifier node
        Node id_node=node.getChild().get(1);
        //得到第一个identifier name
        String id=id_node.getValue();

        //声明分为：变量声明+函数声明
        //isfunc
        boolean isFunc=false;
        //如果找到括号就是函数声明
        for (Node child:node.getChild())
        {
            if(child.getValue().equals("("))
            {
                isFunc=true;
            }
        }

        //【case1：函数声明】
        if(isFunc )
        {
            //函数返回值类型
            String returnType=node.getChild().get(0).getValue();
            //函数名ID
            String name=id_node.getValue();
            //函数最后一个元素 compoundStmt
            int index=node.getChild().size()-1;
            //函数参数数组
            List<Node> paramsNode=new ArrayList<Node>();
            //参数node数组
            for(int i=0;i<node.getChild().size();i++){
                if(i!=0&&i!=1&&i!=2&&i!=index-1&&i!=index) {
                    paramsNode.add(node.getChild().get(i));
                }
            }
            //参数string数组
            List<String> params=new ArrayList<String>();
            if(paramsNode.size()>0){
                for(Node n:paramsNode){
                    String value=n.getValue();
                    //去掉逗号
                    if(!value.equals(",")&& (!value.equals("["))&&(!value.equals("]"))){
                        params.add(value);
                    }
                }
            }
            //由以上的组成了函数四元式：返回值类型+函数名+参数string数组+{}四元式
            //【向函数表添加对象】
            addFunction(new Function(returnType,name,params,compoundStmt(node.getChild().get(index),true)));

        }
        //【case2：单一变量、数组声明】
        else//array  or single
        {
            int index=1;
            //declaration下的子节点集合
            List<Node> children=node.getChild();

            //一个大while，每次循环确定一个ID
            while (!children.get(index).getValue().equals(";"))
            {
                //取identifier
                id_node=children.get(index);
                id=id_node.getValue();

                //【case1:单个变量+赋值】如果是int/real ID= 形式
                if(children.get(index+1).getValue().equals("="))//single declare + assign
                {
                    //声明四元式
                    addCode(new FourCode(fourCodeType,null,null,id));//是否考虑赋初始值0？
                    addItem(id_node, symbolType);
                    Node value=node.getChild().get(index+2);
                    //赋值四元式
                    addCode(new FourCode(FourCode.ASSIGN,expression(value),null,id));
                    index=index+3;

                }
                //【case2:多维数组+赋值】如果是int/real ID[] 形式
                else if (children.get(index+1).getValue().equals("["))//array
                {
                    symbolType=symbolType+2;
                    //change index position to [
                    index=index+1;
                    //为【计算多维数组空间大小】，int array[2][3][5]  (2)*(3)*(5)
                    //构建语法树
                    Node term_node=new Node(NodeType.TERM);
                    int count=0;
                    while (children.get(index).getValue().equals("["))
                    {
                        //calculate allocation of memory
                        Node factor=new Node(NodeType.FACTOR);
                        Node DELIMITER1=new Node(NodeType.DELIMITER,"(",0);
                        Node expression=children.get(index+1);
                        Node DELIMITER2=new Node(NodeType.DELIMITER,")",0);
                        factor.addChild(DELIMITER1);
                        factor.addChild(expression);
                        factor.addChild(DELIMITER2);
                        term_node.addChild(factor);
                        index=index+3;
                        //做乘法
                        if(children.get(index).getValue().equals("["))
                        {
                            term_node.addChild(new Node(NodeType.MULOP,"*",0));
                        }
                        //保存每一维的大小 为了读取和赋值计算在内存中的位置
                        //int array[2][3][5]； array[1][2][3]  1*3*5+2*5+3
                        //calculate size of index
                        String index_term=expression(expression);
                        String arraryIndexS=id+"_"+Integer.toString(count)+"_index_record";
                        addCode(new FourCode(FourCode.INT,index_term,null,arraryIndexS));
                        //加入符号表
                        Node arrayIndexNode= new Node(NodeType.DELIMITER,arraryIndexS,0);
                        addItem(arrayIndexNode, symbolType);
                        count++;

                    }
                    String term=term(term_node);//term1
                    //数组声明的四元式
                    addCode(new FourCode(fourCodeType,null,term,id));
                    addItem(id_node, symbolType);


                    //【数组赋值】int array[2]={1,2}
                    if (children.get(index).getValue().equals("=") && children.get(index+1).getValue().equals("{") )
                    {
                        //change position to expression one
                        index=index+2;
                        int arrayIndex=0;
                        //给每一个数组元素赋值
                        while (children.get(index).getType()==NodeType.EXPRESSION)
                        {
                            Node value=node.getChild().get(index);
                            addCode(new FourCode(FourCode.ASSIGN,expression(value),null,id+"["+arrayIndex+"]"));//assign在解释中再执行

                            arrayIndex++;
                            index=index+2;
                        }

                    }

                }
                //【case3：单变量声明+不赋值】
                else if (children.get(index).getType()==NodeType.IDENTIFIER )//declare
                {
                    addCode(new FourCode(fourCodeType,null,null,id));//是否考虑赋初始值0？
                    addItem(id_node, symbolType);
                    index=index+1;
                }

                //跳过逗号，例如：int a,b,c=
                if(children.get(index).getValue().equals(","))
                {
                    index=index+1;
                }
            }
        }

    }

    //复合语句
    private List<FourCode> compoundStmt(Node node,boolean isfunc)throws SemException {
        List<FourCode> fc = new ArrayList<FourCode>();
        List<Node> nodes = node.getChild();
        //【case1：是函数】
        if (isfunc) {
            //为函数生成新的语义对象
            Semantic s=new Semantic(node);
            //遍历节点生成四元式
            for (Node n : nodes) {
                s.generate(n);
            }
            //把所有四元式取出来，统一放到四元式链表中，并返回
            for(FourCode f:s.codes){
                fc.add(f);
            }
        }
        //【case2：不是函数】例如if{}else{}while{}
        else {
            //遍历节点生成四元式
            for (Node n : nodes) {
                generate(n);
            }
        }
        return fc;
    }

    //return
    private void returnStmt(Node node){
        //return有返回值
        if(node.getChild().size()>2){
            String temp=getTempItem().getName();
            addCode(new FourCode(FourCode.RETURN,expression(node.getChild().get(1)),null,temp));
        }
    }

    //判断是不是数字
    private static boolean isNumber(String str){
        String reg = "^[0-9]+(.[0-9]+)?$";
        return str.matches(reg);
    }

    //调用函数
    private String call(Node node){
        String temp=getTempItem().getName();
        String name=node.getChild().get(0).getValue();
        int param_len=0;
        if(node.getChild().size()!=3){
            for(int i=2;i<node.getChild().size()-1;i=i+2){
                param_len++;
            }
        }
        //call四元式，包含：名字+参数长度+起始临时变量（传参）
        addCode(new FourCode(FourCode.CALL,name,String.valueOf(param_len),temp));
        //参数赋值
        for(int i=2,j=0;i<node.getChild().size()-1;i=i+2,j++){
            String value=expression(node.getChild().get(i));
            //如果是数字
            if(isNumber(value)){
                String temp1=getTempItem().getName();
                addCode(new FourCode(FourCode.ASSIGN,value,null,temp1));
                addCode(new FourCode(FourCode.PARAMETERS,temp1,j+"",null));
            }
            //如果不是数字
            else{
                addCode(new FourCode(FourCode.PARAMETERS,value,j+"",null));
            }

        }
        return temp;
    }

    //表达式
    private String expression(Node node){
        String s=null;
        //简单表达式
        if(node.getChild().size()==1){//simple-expression
            Node simpleExp=node.getChild().get(0);
            s=simpleExpression(simpleExp);
        }
        //赋值表达式
        else{
            String value=expression(node.getChild().get(2));
            Node var=node.getChild().get(0);
            String id=var.getChild().get(0).getValue();
            //如果是单一变量（不是array[3]）
            if(var.getChild().size()==1){
                addCode(new FourCode(FourCode.ASSIGN,value,null,id));
            }
            //数组
            else{
                //算数组的维度大小
                int index=1;
                List<Node> children=var.getChild();
                int sum=0;
                while (index<children.size() && children.get(index).getValue().equals("["))
                {
                    index=index+3;
                    sum++;
                }



                //change index position to [
                Node add=new Node(NodeType.ADDITIVE_EXPRESSION);
                index=1;
                int count=0;
                Node term_node=null;
                //构建新的语法树，实现加法的计算
                while (index<children.size() && children.get(index).getValue().equals("["))
                {
                    term_node=new Node(NodeType.TERM);
                    //calculate allocation of memory
                    Node factor=new Node(NodeType.FACTOR);
                    term_node.addChild(factor);

                    Node DELIMITER1=new Node(NodeType.DELIMITER,"(",0);
                    Node expression=children.get(index+1);
                    Node DELIMITER2=new Node(NodeType.DELIMITER,")",0);
                    factor.addChild(DELIMITER1);
                    factor.addChild(expression);
                    factor.addChild(DELIMITER2);



                    add.addChild(term_node);
                    if(children.get(index).getValue().equals("["))
                    {
                        int index_small=sum-1-count;
                        //构建新的语法树，实现乘法的计算
                        while (index_small>0)
                        {
                            term_node.addChild(new Node(NodeType.MULOP,"*",0));
                            Node factor_=new Node(NodeType.FACTOR);
                            Node var_=new Node(NodeType.VAR," ",0);
                            Node iden_=new Node(NodeType.IDENTIFIER,id+"_"+Integer.toString(sum-index_small)+"_index_record" ,0);
                            var_.addChild(iden_);
                            factor_.addChild(var_);
                            term_node.addChild(factor_);
                            index_small--;
                        }

                        add.addChild(new Node(NodeType.ADDLOP,"+",0));
                    }

                    index=index+3;
                    count++;
                }

                String temp;
                //如果是一维数组
                if (sum==1)
                {
                     temp=term(term_node);
                }
                //如果不是一维数组，算出真实的位置
                else
                    {
                         temp=addctiveExpression(add);
                    }

                //赋值四元式
                addCode(new FourCode(FourCode.ASSIGN,value,null,id+"["+temp+"]"));//assign在解释中再执行
            }
        }
        return s;
    }

    //简单表达式
    private String simpleExpression(Node node){
        String temp;
        Node a1=node.getChild().get(0);
        //如果只有一个孩子
        if(node.getChild().size()==1){
           temp=addctiveExpression(a1);
        }
        //不止一个孩子，（2+3）>（1-3）
        else{
            temp=getTempItem().getName();
            Node a2=node.getChild().get(2);
            Node relop=node.getChild().get(1);
            switch (relop.getValue()){
                //逻辑运算符四元式
                case ">":
                    addCode(new FourCode(FourCode.MORE,addctiveExpression(a1),addctiveExpression(a2),temp));
                    break;
                case "<":
                    addCode(new FourCode(FourCode.LESS,addctiveExpression(a1),addctiveExpression(a2),temp));
                    break;
                case ">=":
                    addCode(new FourCode(FourCode.MOREE,addctiveExpression(a1),addctiveExpression(a2),temp));
                    break;
                case "<=":
                    addCode(new FourCode(FourCode.LESSE,addctiveExpression(a1),addctiveExpression(a2),temp));
                    break;
                case "<>":
                    addCode(new FourCode(FourCode.NEQ,addctiveExpression(a1),addctiveExpression(a2),temp));
                    break;
                case "==":
                    addCode(new FourCode(FourCode.EQ,addctiveExpression(a1),addctiveExpression(a2),temp));
                    break;
            }
        }
        return temp;
    }

    //加性表达式
    private String addctiveExpression(Node node){
        String temp;
        String term=term(node.getChild().get(0));//term1
        if(node.getChild().size()!=1){//term ....
            temp=getTempItem().getName();
            for(int i=1;i<node.getChild().size()-1;i=i+2){
                switch (node.getChild().get(i).getValue()) {
                    //使用临时变量
                    case "+":
                        addCode(new FourCode(FourCode.PLUS, term, term(node.getChild().get(i + 1)), temp));
                        term=temp;
                        break;
                    case "-":
                        addCode(new FourCode(FourCode.MINUS, term, term(node.getChild().get(i + 1)), temp));
                        term=temp;
                        break;
                }
            }
        }
        //例如：int a=1;
        else{
            temp=term;
        }
        return temp;
    }

    //term乘法
    private String term(Node node){
        String temp;
        String factor=factor(node.getChild().get(0));//factor1
        //如果是一串乘除
        if(node.getChild().size()!=1){//factor ....
            temp=getTempItem().getName();
            for(int i=1;i<node.getChild().size()-1;i=i+2){
                switch (node.getChild().get(i).getValue()) {
                    case "*":
                        addCode(new FourCode(FourCode.MUL, factor, factor(node.getChild().get(i + 1)), temp));
                        factor=temp;
                        break;
                    case "/":
                        addCode(new FourCode(FourCode.DIV, factor, factor(node.getChild().get(i + 1)), temp));
                        factor=temp;
                        break;
                }
            }
        }
        //如果就一个
        else{
            temp=factor;
        }
        return temp;
    }

    //factor=变量+数字+call
    private String factor(Node node){
        if(node.getChild().size()!=1){//(exp)
            return expression(node.getChild().get(1));
        }else{
            Node n=node.getChild().get(0);
            switch (n.getType()){
                case VAR:
                    //单一变量
                    if(n.getChild().size()==1){//single
                        return n.getChild().get(0).getValue();
                    }
                    //数组，需要计算真实的内存地址
                    else{//array
                        String temp=getTempItem().getName();
                        Node var=n;
                        String id=n.getChild().get(0).getValue();
//                        String index=expression(n.getChild().get(2));


                        //count dimension
                        int index=1;
                        List<Node> children=var.getChild();
                        int sum=0;
                        //根据括号个数算维度
                        while (index<children.size() && children.get(index).getValue().equals("["))
                        {
                            index=index+3;
                            sum++;
                        }



                        //change index position to [
                        Node add=new Node(NodeType.ADDITIVE_EXPRESSION);
                        index=1;
                        int count=0;
                        Node term_node=null;
                        //构造一棵树，为了生成计算内存空间的四元式
                        //加法部门
                        while (index<children.size() && children.get(index).getValue().equals("["))
                        {
                            term_node=new Node(NodeType.TERM);
                            //calculate allocation of memory
                            Node factor=new Node(NodeType.FACTOR);
                            term_node.addChild(factor);

                            Node DELIMITER1=new Node(NodeType.DELIMITER,"(",0);
                            Node expression=children.get(index+1);
                            Node DELIMITER2=new Node(NodeType.DELIMITER,")",0);
                            factor.addChild(DELIMITER1);
                            factor.addChild(expression);
                            factor.addChild(DELIMITER2);



                            add.addChild(term_node);
                            if(children.get(index).getValue().equals("["))
                            {
                                int index_small=sum-1-count;
                                //乘法树
                                while (index_small>0)
                                {
                                    term_node.addChild(new Node(NodeType.MULOP,"*",0));
                                    Node factor_=new Node(NodeType.FACTOR);
                                    Node var_=new Node(NodeType.VAR," ",0);
                                    Node iden_=new Node(NodeType.IDENTIFIER,id+"_"+Integer.toString(sum-index_small)+"_index_record" ,0);
                                    var_.addChild(iden_);
                                    factor_.addChild(var_);
                                    term_node.addChild(factor_);
                                    index_small--;
                                }

                                add.addChild(new Node(NodeType.ADDLOP,"+",0));
                            }

                            index=index+3;
                            count++;
                        }

                        String tempIndex;
                        //维度为1
                        if (sum==1)
                        {
                            tempIndex=term(term_node);
                        }
                        //维度不为1
                        else
                        {
                            //tempIndex数组真实的地址
                            tempIndex=addctiveExpression(add);
                        }


                        addCode(new FourCode(FourCode.ASSIGN,n.getChild().get(0).getValue()+"["+tempIndex+"]",null,temp));
                        return temp;
                    }
                case CALL:
                    return call(n);
                case NUMBER:
                    return n.getValue();
            }
        }
        return null;
    }

    //每一个s的入口
    public void semAnalyze() throws SemException {
        for (Node node : tree.getChild()) {
            generate(node);
        }
    }
}
