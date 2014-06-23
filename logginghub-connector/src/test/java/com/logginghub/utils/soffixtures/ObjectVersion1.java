package com.logginghub.utils.soffixtures;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;


public class ObjectVersion1 implements SerialisableObject{

    private int value;
    private String string;
    
    public ObjectVersion1() {}
    
    public ObjectVersion1(int value, String string) {
        this.value = value;
        this.string = string;
    }
    
    public String getString() {
        return string;
    }
    
    public void setString(String string) {
        this.string = string;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public void read(SofReader reader) throws SofException {
        this.value = reader.readInt(1);
        this.string = reader.readString(2);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
        writer.write(2, string);
    }
}
