package com.logginghub.sof;

public class SofHeader {
    public int version;
    public int flags;
    public int type;
    public int fieldCount;
    public int headerLength;
    public int length;
    
    @Override public String toString() {
        return "SofHeader [version=" +
               version +
               ", flags=" +
               flags +
               ", type=" +
               type +
               ", fieldCount=" +
               fieldCount +
               ", headerLength=" +
               headerLength +
               ", length=" +
               length +
               "]";
    }
    
    

}
