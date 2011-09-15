package de.jiac.micro.test;

/**
 *
 * @author Marcel Patzlaff
 */
public class DummyClass {
    private int _number= -1;
    private String _string= null;
    
    public int getNumber() {
        return _number;
    }
    
    public void setNumber(int value) {
        _number= value;
    }
    
    public String getString() {
        return _string;
    }
    
    public void setString(String value) {
        _string= value;
    }
    
    protected void invisibleMethod() {}
    
    public byte overloadedMethod(Object val) {
        System.out.println("Object: aufgerufen mit " + val);
        return 1;
    }
    
    public byte overloadedMethod(Long val) {
        System.out.println("Long: aufgerufen mit " + val);
        return 2;
    }
    
    public byte overloadedMethod(short[] val) {
        System.out.println("short[]: aufgerufen mit " + val);
        return 3;
    }
    
    public byte overloadedMethod(float val) {
        System.out.println("float: aufgerufen mit " + val);
        return 4;
    }
}
