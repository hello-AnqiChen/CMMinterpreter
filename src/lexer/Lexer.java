package lexer;

import java.util.ArrayList;
import java.util.List;


public class Lexer {
    public CharReader charReader;
    public Lexer(String text){
        charReader=new CharReader(text);
    }
    public String getResult(){
        StringBuffer result=new StringBuffer();
        Token t=nextToken();
        while(true){
            result.append("    ");
//                result.append(charReader.line);
//                result.append(":");
            result.append(t.toString());
            result.append('\n');
            if(t.getType()==TokenType.EOF||t.getType()==TokenType.ERROR) break;
            t=nextToken();
        }
        return result.toString();
    }
    public List<Token> getTokens(){
        List<Token> tokens=new ArrayList<>();
        Token t=nextToken();
        while(true){
            tokens.add(t);
            if(t.getType()==TokenType.EOF) break;
            t=nextToken();
        }
        return tokens;
    }

    public Token nextToken(){

//        try {
            char ch;
            for (; ; ) {
                //check EOF
                if (!charReader.hasMore()) {
                    return new Token(TokenType.EOF, "EOF",charReader.line);
                }
                ch = charReader.peek();
                //skip
                if (ch == '/') {
                    charReader.forward();
                    char a=charReader.peek();
                    if (charReader.peek() == '*') {//多行注释
                        charReader.forward();
                        boolean hasChar=true;
                        while (hasChar) {
                            if (charReader.peek() == '*') {
                                hasChar=charReader.forward();
                                if (charReader.peek() == '/') {
                                    charReader.forward();
                                    break;
                                }
                            }
                            else {
                                hasChar=charReader.forward();
                            }
                        }
                    }
                    else if (charReader.peek() == '/') {//单行注释
                        boolean hasChar=true;
                        while (charReader.peek() != '\n' && charReader.peek() != '\r' && hasChar) {
                            hasChar=charReader.forward();
                        }
                        if(!hasChar){
                            return new Token(TokenType.EOF,"EOF",charReader.line);
                        }
                    }
                    else {//除号
                        return new Token(TokenType.SIMPLE_SYMBOL, "/",charReader.line);
                    }
                    ch=charReader.peek();
                }

                if (ch=='/')
                {
                    continue;
                }

                if (ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r' ) {
                    break;
                }
                charReader.forward();
            }

            //@todo + - number
        if(ch=='+' || ch=='-')
        {   char symbol=ch;
            char last=charReader.last();
            if( last=='('  || last=='=' ||  last=='<' ||  last=='>' ||  last=='|'||  last=='&')
            {
                char next=charReader.next();
                if (next >= '0' && next <= '9') {
                    charReader.forward();
                    StringBuffer val = new StringBuffer();
                    boolean isReal = false;
                    boolean isError=false;
                    while ((charReader.peek() >= '0' && charReader.peek() <= '9') || charReader.peek() == '.') {
                        if (charReader.peek() == '.') {
                            if (isReal) isError=true;
                            else isReal = true;
                        }
                        val.append(charReader.peek());
                        charReader.forward();
                    }
                    if (isError)
                    {
                        return new Token(TokenType.ERROR, symbol+val.toString(),charReader.line);
                    }

                    return new Token(TokenType.NUM, symbol+val.toString(),charReader.line);

                }
            }

        }


            switch (ch) {
                case ',':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, ",",charReader.line);
                case ';':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, ";",charReader.line);
                case '+':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, "+",charReader.line);
                case '-':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, "-",charReader.line);
//                case '*':
//                    charReader.forward();
//                    return new Token(TokenType.SIMPLE_SYMBOL, "*",charReader.line);
//                case '=':
//                    charReader.forward();
//                    return new Token(TokenType.SIMPLE_SYMBOL,"=",charReader.line);
                case '(':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, "(",charReader.line);
                case ')':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, ")",charReader.line);
                case '[':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, "[",charReader.line);
                case ']':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, "]",charReader.line);
                case '{':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, "{",charReader.line);
                case '}':
                    charReader.forward();
                    return new Token(TokenType.SIMPLE_SYMBOL, "}",charReader.line);
            }

        //复杂符号
        if(ch=='*'){
            charReader.forward();
            if(charReader.peek()=='*'){
                charReader.forward();
                return new Token(TokenType.COMPOSITE_SYMBOL,"**",charReader.line);
            }else{
                return new Token(TokenType.SIMPLE_SYMBOL,"*",charReader.line);
            }
        }


            //复杂符号
            if(ch=='='){
                charReader.forward();
                if(charReader.peek()=='='){
                    charReader.forward();
                    return new Token(TokenType.COMPOSITE_SYMBOL,"==",charReader.line);
                }else{
                    return new Token(TokenType.SIMPLE_SYMBOL,"=",charReader.line);
                }
            }
            if (ch == '<') {
                charReader.forward();
                if (charReader.peek() == '=') {
                    charReader.forward();
                    return new Token(TokenType.COMPOSITE_SYMBOL, "<=",charReader.line);
                } else if (charReader.peek() == '>') {
                    charReader.forward();
                    return new Token(TokenType.COMPOSITE_SYMBOL, "<>",charReader.line);
                } else {
                    return new Token(TokenType.SIMPLE_SYMBOL, "<",charReader.line);
                }
            } else if (ch == '>') {
                charReader.forward();
                if (charReader.peek() == '=') {
                    charReader.forward();
                    return new Token(TokenType.COMPOSITE_SYMBOL, ">=",charReader.line);
                } else {
                    return new Token(TokenType.SIMPLE_SYMBOL, ">",charReader.line);
                }
            }else if (ch == '|') {
                charReader.forward();
                if (charReader.peek() == '|') {
                    charReader.forward();
                    return new Token(TokenType.COMPOSITE_SYMBOL, "||",charReader.line);
                } else {
                    return new Token(TokenType.SIMPLE_SYMBOL, "|",charReader.line);
                }
            }else if (ch == '&') {
                charReader.forward();
                if (charReader.peek() == '&') {
                    charReader.forward();
                    return new Token(TokenType.COMPOSITE_SYMBOL, "&&",charReader.line);
                } else {
                    return new Token(TokenType.SIMPLE_SYMBOL, "&",charReader.line);
                }
            }

            if ((Character.isLetter(ch)) || ch == '_') {
                StringBuffer val = new StringBuffer();
                while (Character.isLetterOrDigit(charReader.peek()) || charReader.peek() == '_') {
                    val.append(charReader.peek());
                    charReader.forward();
                }
                String value = val.toString();
                switch (value) {
                    case "if":
                    case "else":
                    case "while":
                    case "read":
                    case "write":
                    case "int":
                    case "real":
                    case "void":
                    case "return":
                    case "continue":
                    case "break":
                        return new Token(TokenType.RESERVED_WORD, value,charReader.line);
                    default:
                        return new Token(TokenType.ID, value,charReader.line);
                }
            }
            //数字
        if (ch >= '0' && ch <= '9') {
            StringBuffer val = new StringBuffer();
            boolean isReal = false;
            boolean isError=false;
            while ((charReader.peek() >= '0' && charReader.peek() <= '9') || charReader.peek() == '.') {
                if (charReader.peek() == '.') {
                    if (isReal) isError=true;
                    else isReal = true;
                }
                val.append(charReader.peek());
                charReader.forward();
            }
            if (isError)
            {
                return new Token(TokenType.ERROR, val.toString(),charReader.line);
            }

            return new Token(TokenType.NUM, val.toString(),charReader.line);

        }
        charReader.forward();
        return new Token(TokenType.ERROR, ch+"",charReader.line);
    }
    public static void main(String[] args){
        Lexer lexer=new Lexer("int i;\n int j;\n void fun(int i){a=a+b}");
        System.out.print(lexer.getResult());
        lexer.getTokens();
    }
}
