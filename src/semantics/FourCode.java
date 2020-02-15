package semantics;

public class FourCode {

    /*JMP指令如果是有条件跳转, 那么是条件为假的时候跳转到指定目标.
      IN指令代表进入代码块
      OUT指令代表出代码块
      INT和REAL指令代表声明变量
    * */
    public static final String JMP="jmp";
    public static final String ASSIGN="assign";
    public static final String READ="read";
    public static final String WRITE="write";
    public static final String CALL ="call";
    public static final String PARAMETERS="parameters";
    public static final String RETURN="return";
    public static final String INT="int";
    public static final String REAL="real";
    public static final String IN="in";
    public static final String OUT="out";
    public static final String PLUS="+";
    public static final String MINUS="-";
    public static final String MUL="*";
    public static final String DIV="/";
    public static final String LESS="<";
    public static final String MORE=">";
    public static final String LESSE="<=";
    public static final String MOREE=">=";
    public static final String EQ="==";
    public static final String NEQ="<>";

    public String op;
    public String arg1;
    public String arg2;
    public String result;
    public int lineNo;

    //构造四元式
    public FourCode(String op,String arg1,String arg2,String result){
        this.op=op;
        this.arg1=arg1;
        this.arg2=arg2;
        this.result=result;
    }

    //op,arg1,arg2,result的get,set函数
    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    //四元式toString
    public String toString(){
        return String.format("(%s, %s, %s, %s)", op, arg1, arg2, result);
    }

}
