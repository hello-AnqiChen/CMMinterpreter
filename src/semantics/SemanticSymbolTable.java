package semantics;

import java.util.ArrayList;

//符号表
public class SemanticSymbolTable {
    //符号表tabel为一个数组
    private ArrayList<Symbol> table;

    //构造符号表
    public SemanticSymbolTable() {
        table = new ArrayList<Symbol>();
    }

    //table的get函数
    public ArrayList<Symbol> getTable() {
        return table;
    }

    //当一个Symbol要加入SymbolTable时
    public boolean enter(Symbol symbol) {
        for (int i = 0; i < table.size(); i++) {
            //取第i个symbol
            Symbol i_symbol = table.get(i);
            //case1：将即将加入的symbol与第i个symbol比较，如果存在相同name的symbol
            if (i_symbol.getName().equals(symbol.getName())) {
                //case1.1:如果即将加入的symbol的优先级比第i个symbol的优先级高
                if (i_symbol.getLevel() < symbol.getLevel()) {
                    //则设置第i个symbol为即将加入的symbol的nextSymbol
                    symbol.setNextSymbol(i_symbol);
                    //将即将加入的这个symbol插入符号表的第i位
                    table.set(i, symbol);
                    return true;
                } else {
                    //case1.2:error
                    return false;
                }
            }
        }
        //case2：如果没有相同名称的symbol，则增加该symbol到符号表
        table.add(symbol);
        return true;
    }

    //销毁符号
    public void endScope(int level) {
        //遍历符号表，找到想要销毁的scope对应的优先级level
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).getLevel() == level) {
                //找到对应level的第i个元素的下一个元素NextSymbol
                Symbol next = table.get(i).getNextSymbol();
                //如果NextSymbol不为空，则添加到符号表中
                if (next != null) {
                    table.set(i, next);
                }
                //如果NextSymbol为空，则将第i个symbol移出符号表
                else {
                    table.remove(i);
                }
            }
        }
    }

    //遍历table，返回对应name的symbol的i，不存在则返回null
    public Symbol getItem(String name) {
        for(Symbol i:table){
            if(i.getName().equals(name)){
                return i;
            }
        }
        return null;//if null error occur, the variable is not exit
    }
    //返回对应name的symbol的type
    public int getItemType(String name){
        return getItem(name).getType();
    }
}
