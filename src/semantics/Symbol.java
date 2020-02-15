package semantics;


import java.util.List;
//符号
public class Symbol {
    //static无需对象即可访问 final不可修改
    public static final int INT=1;
    public static final int REAL=2;
    public static final int ARRAY_INT=3;
    public static final int ARRAY_REAL=4;
    public static final int TEMP=5;

    //symbol的name，type，level，下一个symbol
    private String name;
    private int type;
    private int level;
    private Symbol nextSymbol;


    //symbol的name，type，level，下一个symbol的get，set函数
    public void setNextSymbol(Symbol nextSymbol) {
        this.nextSymbol = nextSymbol;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public Symbol getNextSymbol() {
        return nextSymbol;
    }

    //symbol的各种构造函数
    public Symbol(String name, int type, int level){
        this.name=name;
        this.type=type;
        this.level=level;
        this.nextSymbol =null;
    }
    public Symbol(String name, int type){
        this.name=name;
        this.type=type;
        this.level=-1;
        this.nextSymbol =null;
    }

    public Symbol(String name){
        this.name=name;
        this.level=-1;
        this.nextSymbol =null;
    }
}
