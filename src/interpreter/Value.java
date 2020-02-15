package interpreter;


import interpretException.InterpretException;

public class Value {

    private int mType;

    private int mInt;
    private double mReal;
    private int[] mArrayInt;
    private double[] mArrayReal;

    public Value(int type)
    {
        this.mType = type;
    }

    //类型转换
    public Value(boolean bool)
    {
        if (bool)
            this.mType = Symbol.TRUE;
        else
            this.mType = Symbol.FALSE;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public int getInt() {
        return mInt;
    }

    public void setInt(int mInt) {
        this.mInt = mInt;
    }

    public double getReal() {
        return mReal;
    }

    public void setReal(double mReal) {
        this.mReal = mReal;
    }

    public int[] getArrayInt() {
        return mArrayInt;
    }

    public void setArrayInt(int[] mArrayInt) {
        this.mArrayInt = mArrayInt;
    }

    public double[] getArrayReal() {
        return mArrayReal;
    }

    public void setArrayReal(double[] mArrayReal) {
        this.mArrayReal = mArrayReal;
    }

    public void initArray(int dimension)
    {
        if (mType == Symbol.ARRAY_INT)
            mArrayInt = new int[dimension];
        else
            mArrayReal = new double[dimension];
    }


    public Value plus(Value value) throws InterpretException
    {
        if (this.mType == Symbol.SINGLE_REAL)
        {
            Value returnValue = new Value(Symbol.SINGLE_REAL);
            if (returnValue.mType == Symbol.SINGLE_INT)
            {
                returnValue.setReal(this.mReal + value.mInt);
                return returnValue;
            }else if (value.mType == Symbol.SINGLE_REAL)
            {
                returnValue.setReal(this.mReal + value.mReal);
                return returnValue;
            }
        }else if (this.mType == Symbol.SINGLE_INT)
        {
            if (value.mType == Symbol.SINGLE_INT)
            {
                Value returnValue = new Value(Symbol.SINGLE_INT);
                returnValue.setInt(this.mInt + value.mInt);
                return returnValue;
            }else if (value.mType == Symbol.SINGLE_REAL)
            {
                Value returnValue = new Value(Symbol.SINGLE_REAL);
                returnValue.setReal(this.mInt + value.mReal);
                return returnValue;
            }
        }
        throw new InterpretException("算数运算非法");
    }

    public Value minus(Value value) throws InterpretException {
        if (this.mType == Symbol.SINGLE_REAL) {
            Value rv = new Value(Symbol.SINGLE_REAL);
            if (value.mType == Symbol.SINGLE_INT) {
                rv.setReal(this.mReal - value.mInt);
                return rv;
            } else if (value.mType == Symbol.SINGLE_REAL) {
                rv.setReal(this.mReal - value.mReal);
                return rv;
            }
        } else if (this.mType == Symbol.SINGLE_INT) {
            if (value.mType == Symbol.SINGLE_INT) {
                Value rv = new Value(Symbol.SINGLE_INT);
                rv.setInt(this.mInt - value.mInt);
                return rv;
            } else if (value.mType == Symbol.SINGLE_REAL) {
                Value rv = new Value(Symbol.SINGLE_REAL);
                rv.setReal(this.mInt - value.mReal);
                return rv;
            }
        }
        throw new InterpretException("算数运算非法");
    }

    public Value multiply(Value value) throws InterpretException {
        if (this.mType == Symbol.SINGLE_REAL) {
            Value rv = new Value(Symbol.SINGLE_REAL);
            if (value.mType == Symbol.SINGLE_INT) {
                rv.setReal(this.mReal * value.mInt);
                return rv;
            } else if (value.mType == Symbol.SINGLE_REAL) {
                rv.setReal(this.mReal * value.mReal);
                return rv;
            }
        } else if (this.mType == Symbol.SINGLE_INT) {
            if (value.mType == Symbol.SINGLE_INT) {
                Value rv = new Value(Symbol.SINGLE_INT);
                rv.setInt(this.mInt * value.mInt);
                return rv;
            } else if (value.mType == Symbol.SINGLE_REAL) {
                Value rv = new Value(Symbol.SINGLE_REAL);
                rv.setReal(this.mInt * value.mReal);
                return rv;
            }
        }
        throw new InterpretException("算数运算非法");
    }

    public Value divide(Value value) throws InterpretException {
        if (this.mType == Symbol.SINGLE_REAL) {
            Value rv = new Value(Symbol.SINGLE_REAL);
            if (value.mType == Symbol.SINGLE_INT) {
                if (value.getInt() == 0) {
                    throw new InterpretException("不能除0");
                }
                rv.setReal(this.mReal / value.mInt);
                return rv;
            } else if (value.mType == Symbol.SINGLE_REAL) {
                if (value.getReal() == 0) {
                    throw new InterpretException("不能除0");
                }
                rv.setReal(this.mReal / value.mReal);
                return rv;
            }
        } else if (this.mType == Symbol.SINGLE_INT) {
            if (value.mType == Symbol.SINGLE_INT) {
                if (value.getInt() == 0) {
                    throw new InterpretException("不能除0");
                }
                Value rv = new Value(Symbol.SINGLE_INT);
                rv.setInt(this.mInt / value.mInt);
                return rv;
            } else if (value.mType == Symbol.SINGLE_REAL) {
                if (value.getReal() == 0) {
                    throw new InterpretException("不能除0");
                }
                Value rv = new Value(Symbol.SINGLE_REAL);
                rv.setReal(this.mInt / value.mReal);
                return rv;
            }
        }
        throw new InterpretException("算数运算非法");
    }

    public Value greaterThan(Value value) throws InterpretException {
        if (this.mType == Symbol.SINGLE_INT) {
            if (value.mType == Symbol.SINGLE_INT) {
                return new Value(this.mInt > value.mInt);
            } else if (value.mType == Symbol.SINGLE_REAL) {
                return new Value(this.mInt > value.mReal);
            }
        } else if (this.mType == Symbol.SINGLE_REAL) {
            if (value.mType == Symbol.SINGLE_INT) {
                return new Value(this.mReal > value.mInt);
            } else if (value.mType == Symbol.SINGLE_REAL) {
                return new Value(this.mReal > value.mReal);
            }
        }
        throw new InterpretException("逻辑比较非法");
    }

    public Value equalTo(Value value) throws InterpretException {
        if (this.mType == Symbol.SINGLE_INT) {
            if (value.mType == Symbol.SINGLE_INT) {
                return new Value(this.mInt == value.mInt);
            } else if (value.mType == Symbol.SINGLE_REAL) {
                return new Value(this.mInt == value.mReal);
            }
        } else if (this.mType == Symbol.SINGLE_REAL) {
            if (value.mType == Symbol.SINGLE_INT) {
                return new Value(this.mReal == value.mInt);
            } else if (value.mType == Symbol.SINGLE_REAL) {
                return new Value(this.mReal == value.mReal);
            }
        }
        throw new InterpretException("逻辑比较非法");
    }

    public Value or(Value value) {
        if (this.mType == Symbol.TRUE || value.mType == Symbol.TRUE) {
            return new Value(Symbol.TRUE);
        } else {
            return new Value(Symbol.FALSE);
        }
    }

    public Value greaterOrEqualTo(Value value) throws InterpretException {
        return this.greaterThan(value).or(this.equalTo(value));
    }

    public Value lessThan(Value value) throws InterpretException {
        return not(this.greaterOrEqualTo(value));
    }

    public Value lessOrEqualTo(Value value) throws InterpretException {
        return not(this.greaterThan(value));
    }

    public Value notEqualTo(Value value) throws InterpretException {
        return not(this.equalTo(value));
    }

    public static Value not(Value value) throws InterpretException {
        if (value.mType == Symbol.TRUE) {
            return new Value(Symbol.FALSE);
        } else if (value.mType == Symbol.FALSE) {
            return new Value(Symbol.TRUE);
        } else if (value.mType == Symbol.SINGLE_INT) {
            Value rv = new Value(Symbol.SINGLE_INT);
            rv.setInt(value.mInt * -1);
            return rv;
        } else if (value.mType == Symbol.SINGLE_REAL) {
            Value rv = new Value(Symbol.SINGLE_REAL);
            rv.setReal(value.mReal * -1);
            return rv;
        }
        throw new InterpretException("负号使用非法");
    }

    @Override
    public String toString() {
        switch (mType) {
            case Symbol.SINGLE_INT:
                return mInt + "";
            case Symbol.SINGLE_REAL:
                return mReal + "";
            case Symbol.TRUE:
                return "true";
            case Symbol.FALSE:
                return "false";
            default:
                return "array can't be write";
        }
    }

    /**
     * 获取Value对应的real值
     * @return
     */
    public Value toReal() {
        if (mType == Symbol.SINGLE_REAL) {
            return this;
        } else {
            mType = Symbol.SINGLE_REAL;
            mReal = (double) mInt;
            mInt = 0;
            return this;
        }
    }
}
