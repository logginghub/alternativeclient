package com.logginghub.utils.soffixtures;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;


public class SimpleIntObject implements SerialisableObject{

    private int value;
    
    public SimpleIntObject() {}
    
    public SimpleIntObject(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public void read(SofReader reader) throws SofException {
        this.value = reader.readInt(1);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
    }
}
