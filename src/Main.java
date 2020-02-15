import interpretException.InterpretException;
import interpreter.Interpreter;
import lexer.Lexer;
import parser.Parser;
import semantics.SemException;
import semantics.Semantic;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args)
    {

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
        Lexer lexer = new Lexer(textS);
        Parser parser=new Parser(textS);
        parser.parse();

        if(parser.errors.isEmpty()){

            Semantic semantic = new Semantic(parser.getTree());
            try {
                semantic.semAnalyze();


                Interpreter interpreter = new Interpreter();

                String output=interpreter.interpret(semantic);

                //run result
                System.out.println("****LEX*****");
                System.out.println(lexer.getResult());
                System.out.println("*************");

                System.out.println("****TREE*****");
                System.out.println(parser.printTree());
                System.out.println("*************");

                System.out.println("****CODE*****");
                System.out.println(semantic.getCodes());
                System.out.println("*************");

                System.out.println("*****RUN*****");
                System.out.println(output.replace("null",""));
                System.out.println("*************");
            } catch (SemException | InterpretException e) {
                System.out.println(e.getMessage());
            }
        }else { for(Error e: parser.errors){
                    System.out.println(e+"");
                }
        }

    }
}
