package com.logginghub.utils.soffixtures;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;


public class SimpleStringObject implements SerialisableObject{

    private String value;
    
    public SimpleStringObject() {}
    
    public SimpleStringObject(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public void read(SofReader reader) throws SofException {
        this.value = reader.readString(1);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
    }
}
