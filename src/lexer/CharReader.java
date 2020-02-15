package lexer;

public class CharReader{
    public int offset;
    public int line;
    public int col;
    public String text;

    public CharReader(String text) {
        this.text=text;
        this.offset=0;
        this.line=1;
        this.col=0;
    }
    /*
    offset指向下一字符
     */
    public boolean forward(){
        if(offset==text.length()){
            return false;
        }
        if(text.charAt(offset)=='\n'){
            line++;
            col=0;
            offset++;
        }else{
            col++;
            offset++;
        }
        return true;
    }
    public char peek()  {
        if(offset==text.length()){
            return ' ';
        }
        return text.charAt(offset);
    }
    public boolean hasMore(){
        return offset<text.length();
    }


    public char last()
    {
        if(offset-1==text.length()){
            return ' ';
        }
        return text.charAt(offset-1);
    }

    public char next()
    {
        if(offset+1==text.length()){
            return ' ';
        }
        return text.charAt(offset+1);
    }


    public void toEndOfLine()
    {
        char currentChar=text.charAt(offset);
        while (currentChar!='\n')
        {
            offset=offset+1;
            currentChar=text.charAt(offset);
        }
        offset=offset+1;
    }


}

