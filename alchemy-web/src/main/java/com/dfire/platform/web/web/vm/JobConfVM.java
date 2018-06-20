package com.dfire.platform.web.web.vm;

/**
 * @author congbai
 * @date 2018/6/8
 */
public class JobConfVM {

    private Long id;

    private Long acJobId;

    private String content;

    private int type;

    private boolean isAudit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAcJobId() {
        return acJobId;
    }

    public void setAcJobId(Long acJobId) {
        this.acJobId = acJobId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isAudit() {
        return isAudit;
    }

    public void setAudit(boolean audit) {
        isAudit = audit;
    }
}
