package util;

//Java ints are primitive yet the Integer box class doesn't support manipulation???
//
//And of java has no overloaded operators! No implicit assignment operators! Ugh!

public class BoxedInteger
{
    private int _value;

    public BoxedInteger(int value) {
        _value = value;
    }

    public int increment() { _value++; return _value; }

    public int decrement() { _value--; return _value; }

    public int add(int value) { _value += value; return _value; }

    public int subtract(int value) { _value -= value; return _value; }

    public int multiply(int value) { _value *= value; return _value; }

    public int value() { return _value; }
}