package com.dfire.platform.web.cluster.request;

/**
 * @author congbai
 * @date 04/06/2018
 */
public class JarSubmitRequest extends AbstractSubmitRequest {

    private String[] programArgs;

    private String entryClass;

    public String[] getProgramArgs() {
        return programArgs;
    }

    public void setProgramArgs(String[] programArgs) {
        this.programArgs = programArgs;
    }

    public String getEntryClass() {
        return entryClass;
    }

    public void setEntryClass(String entryClass) {
        this.entryClass = entryClass;
    }

}
