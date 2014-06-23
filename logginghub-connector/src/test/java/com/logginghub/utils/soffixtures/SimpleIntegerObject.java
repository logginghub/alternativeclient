package com.logginghub.utils.soffixtures;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;


public class SimpleIntegerObject implements SerialisableObject{

    private Integer intType;
    
    public SimpleIntegerObject() {}
    
    public SimpleIntegerObject(int value) {
        this.intType = value;
    }

    public Integer getIntType() {
        return intType;
    }
    
    public void setIntType(Integer intType) {
        this.intType = intType;
    }
    
    public void read(SofReader reader) throws SofException {
        this.intType = reader.readIntObject(4);
    }

    public void write(SofWriter writer) throws SofException {

        writer.write(4, intType);
        
    }
}
