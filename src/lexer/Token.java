package lexer;

public class Token {

    private TokenType type;
    private String value;
    public int line;

    public Token(TokenType type, String value,int line){
        this.type=type;
        this.value=value;
        this.line=line;
    }

    public TokenType getType(){
        return type;
    }
    public String getValue(){
        return value;
    }
    public void setType(TokenType type){
        this.type=type;
    }
    public void setValue(String value){
        this.value=value;
    }
    public int getLine() {
        return line;
    }
    public void setLine(int line) {
        this.line = line;
    }

    public String toString(){
        StringBuffer sb= new StringBuffer();
        sb.append(line);
        switch(type) {
            case RESERVED_WORD:
                sb.append(": reserved word: ").append(value);
                break;
            case ID:
                sb.append(": ID: name = ").append(value);
                break;
            case NUM:
                sb.append(": NUM: val = ").append(value);
                break;
            case EOF:
                sb.append(": EOF");
                break;
            case ERROR:
                sb.append(": ERROR");
                break;
            case SIMPLE_SYMBOL:
            case COMPOSITE_SYMBOL:
                sb.append(": special symbol: ").append(value);
                break;
        }
        return sb.toString();
    }
}
