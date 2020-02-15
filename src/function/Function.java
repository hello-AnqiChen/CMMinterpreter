package function;

import interpreter.InterpreterSymbolTable;
import semantics.FourCode;
import semantics.SemanticSymbolTable;

import java.util.ArrayList;
import java.util.List;


public class Function {

    public static final String INT = "int";//return type
    public static final String REAL = "real";
    public static final String VOID = "void";

    private String type;//return type
    private String name;
    private int pc = 0;

    private List<String> params = new ArrayList<String>();
    private List<FourCode> codes = new ArrayList<FourCode>();

    private SemanticSymbolTable SemanticSymbolTable;
    private InterpreterSymbolTable InterpreterSymbolTable;

    private int returnValue;
    private int returnMark;
    private String returnType;

    public Function(){}

    public Function(String type,String name,List<String> params,List<FourCode> codes){
        this.type = type;
        this.name = name;
        this.params = params;
        this.codes = codes;
        this.InterpreterSymbolTable = new InterpreterSymbolTable();
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public void setReturnValue(int returnValue) {
        this.returnValue = returnValue;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<String> getParams() {
        return params;
    }

    public void setCodes(List<FourCode> codes) {
        this.codes = codes;
    }

    public List<FourCode> getCodes() {
        return codes;
    }

    public int getReturnValue() {
        return returnValue;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public void pcPlus()
    {
        this.pc++;
    }

    public int getReturnMark() {
        return returnMark;
    }

    public void setReturnMark(int returnMark) {
        this.returnMark = returnMark;
    }

    public SemanticSymbolTable getSemanticSymbolTable() {
        return SemanticSymbolTable;
    }

    public void setSemanticSymbolTable(SemanticSymbolTable SemanticSymbolTable) {
        this.SemanticSymbolTable = SemanticSymbolTable;
    }

    public InterpreterSymbolTable getInterpreterSymbolTable() {
        return InterpreterSymbolTable;
    }

    public void setInterpreterSymbolTable(InterpreterSymbolTable InterpreterSymbolTable) {
        this.InterpreterSymbolTable = InterpreterSymbolTable;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String toString(){
        StringBuffer str = new StringBuffer();
        for (int i = 0;i<this.codes.size();i++){
            str.append(this.codes.get(i)).append("\n");
        }
        StringBuffer para = new StringBuffer();
        for(int i= 0;i<this.params.size();i++){
            para.append(this.params.get(i)).append("\n");
        }
        return
                "函数 :" + "\n" + this.name + "\n" +
                        "返回类型 :" +"\n"+ this.type + "\n"+
                        "参数表：" +"\n"+ new String(para) +"\n" +
                        "四元式组:" + "\n" +new String(str) +"\n" ;
    }
}
