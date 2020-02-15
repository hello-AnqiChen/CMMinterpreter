package parser;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private NodeType type;
    private String value;
    private int line;
    private List<Node> childNodes;

    public int getLine() {
        return line;
    }

    public NodeType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    //非终结符
    public Node(NodeType type){
        this.type=type;
        value="";
        childNodes=new ArrayList<>();
    }
    //终结符
    public Node(NodeType type,String value,int line){
        this.type=type;
        this.value=value;
        this.line=line;
        childNodes=new ArrayList<>();
    }
    //增加子节点
    public boolean addChild(Node node){
        boolean isAdd=false;
        if(node!=null){
            isAdd=childNodes.add(node);
        }
        return isAdd;
    }
    public List<Node> getChild(){
        return childNodes;
    }
    public String toString(){
        StringBuffer sb= new StringBuffer();
        switch(type) {
            case PROGRAM:
            case DECLARATION:
            case STATEMENT:
            case EXPRESSION_STMT:
            case COMPOUND_STMT:
            case IF_STMT:
            case WHILE_STMT:
            case READ_STMT:
            case WRITE_STMT:case RETURN_STMT:
            case EXPRESSION:case CONTINUE:case BREAK:
            case CONTINUE_STMT:case BREAK_STMT:
            case SIMPLE_EXPRESSION:
            case ADDITIVE_EXPRESSION:
            case VAR:
            case TERM:
            case FACTOR:
            case CALL:
            case INT:
            case REAL:
            case IF:
            case ELSE:
            case WHILE:
            case READ:
            case WRITE:
            case VOID:
            case RETURN:

            case ADDLOP:

                sb.append(type.toString());
                break;
            case MULOP:
            case RELOP:
            case IDENTIFIER:
            case NUMBER:
            case DELIMITER:
                sb.append(type.toString()).append(":").append(value);
                break;
        }
        return sb.toString();
    }



    public String displayTree(int level){
        String text = "";

        String preStr = "";
        for(int i=0; i < level; i++) {
            preStr += "  ";
        }
        text += preStr + "-" + type + "\n";

        for(int i = 0; i < childNodes.size(); i++) {
            Node t = childNodes.get(i);

            if(!t.childNodes.isEmpty()) {
                text += t.displayTree(level + 1);
            }else{
                text += preStr + "-" + t.type + "\n";
                text += preStr + "(" + t.value + ")\n";
            }
        }
        return text;
    }
}


