package interpreter;

public class Symbol
{
    private String name;
    private int type;
    private Value value;
    private int level;
    private Symbol next;

    public static final int TEMP = -1;
    public static final int SINGLE_INT = 0;
    public static final int SINGLE_REAL = 1;
    public static final int ARRAY_INT = 2;
    public static final int ARRAY_REAL = 3;

    public static final int TRUE = 4;
    public static final int FALSE = 5;

    /**
     * type是ARRAY_*的时候务必要调用value的initArray方法来初始化数组
     * @param name
     * @param type
     * @param level
     */
    public Symbol(String name, int type, int level) {
        this.name = name;
        this.type = type;
        this.level = level;
        this.value = new Value(type);
    }

    /**
     * 定要是SINGLE_INT
     * @param name
     * @param type
     * @param level
     * @param value
     */
    public Symbol(String name, int type, int level, int value)
    {
        this(name, type, level);
        this.value.setInt(value);
    }

    /**
     * 定要是SINGLE_REAL
     * @param name
     * @param type
     * @param level
     * @param value
     */
    public Symbol(String name, int type, int level, double value) {
        this(name, type, level);
        this.value.setReal(value);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setValue(Value value)
    {
        this.value = value;
    }

    public String getName()
    {
        return this.name;
    }

    public Value getValue()
    {
        return value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }


    public Symbol getNext() {
        return next;
    }

    public void setNext(Symbol next) {
        this.next = next;
    }

}
