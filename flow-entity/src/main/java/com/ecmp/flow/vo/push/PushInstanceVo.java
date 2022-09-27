package com.ecmp.flow.vo.push;


import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

public class PushInstanceVo implements Serializable {

    protected String id;
    /**
     * 创建者
     */
    protected String creatorId;

    protected String creatorAccount;

    protected String creatorName;

    /**
     * 创建时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    protected Date createdDate;

    /**
     * 最后修改者
     */
    protected String lastEditorId;

    protected String lastEditorAccount;

    protected String lastEditorName;

    /**
     * 最后修改时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    protected Date lastEditedDate;


    /**
     * 乐观锁-版本
     */
    private Integer version = 0;

    /**
     * 所属流程定义版本
     */
    private PushFlowDefVersionVo flowDefVersion;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 业务单号
     */
    private String businessCode;

    /**
     * 业务单据名称
     */
    private String businessName;


    /**
     * 业务摘要(工作说明)
     */
    private String businessModelRemark;


    /**
     * 业务单据启动时传的组织机构ID
     */
    private String businessOrgId;



    /**
     * 开始时间
     */
    private Date startDate;

    /**
     * 结束时间
     */
    private Date endDate;

    /**
     * 关联的实际流程引擎实例ID
     */
    private String actInstanceId;


    /**
     * 所属流程定义版本
     */
    private PushInstanceVo parent;

    /**
     * 实例调用路径，针对作为子流程被调用时
     */
    private String callActivityPath;


    /**
     * 是否挂起
     */
    private Boolean suspended=false;

    /**
     * 是否结束
     */
    private Boolean ended=false;


    /**
     * 是否是手动结束（发起人手动终止任务的情况）
     */
    private Boolean manuallyEnd=false;


    /**
     * web基地址
     */
    private String webBaseAddress;


    /**
     * web基地址绝对路径
     */
    private String webBaseAddressAbsolute;

    /**
     * api基地址
     */
    private String apiBaseAddress;

    /**
     * api基地址
     */
    private String apiBaseAddressAbsolute;



    /**
     * 租户代码
     */
    private String tenantCode;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorAccount() {
        return creatorAccount;
    }

    public void setCreatorAccount(String creatorAccount) {
        this.creatorAccount = creatorAccount;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastEditorId() {
        return lastEditorId;
    }

    public void setLastEditorId(String lastEditorId) {
        this.lastEditorId = lastEditorId;
    }

    public String getLastEditorAccount() {
        return lastEditorAccount;
    }

    public void setLastEditorAccount(String lastEditorAccount) {
        this.lastEditorAccount = lastEditorAccount;
    }

    public String getLastEditorName() {
        return lastEditorName;
    }

    public void setLastEditorName(String lastEditorName) {
        this.lastEditorName = lastEditorName;
    }

    public Date getLastEditedDate() {
        return lastEditedDate;
    }

    public void setLastEditedDate(Date lastEditedDate) {
        this.lastEditedDate = lastEditedDate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public PushFlowDefVersionVo getFlowDefVersion() {
        return flowDefVersion;
    }

    public void setFlowDefVersion(PushFlowDefVersionVo flowDefVersion) {
        this.flowDefVersion = flowDefVersion;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessModelRemark() {
        return businessModelRemark;
    }

    public void setBusinessModelRemark(String businessModelRemark) {
        this.businessModelRemark = businessModelRemark;
    }

    public String getBusinessOrgId() {
        return businessOrgId;
    }

    public void setBusinessOrgId(String businessOrgId) {
        this.businessOrgId = businessOrgId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getActInstanceId() {
        return actInstanceId;
    }

    public void setActInstanceId(String actInstanceId) {
        this.actInstanceId = actInstanceId;
    }

    public PushInstanceVo getParent() {
        return parent;
    }

    public void setParent(PushInstanceVo parent) {
        this.parent = parent;
    }

    public String getCallActivityPath() {
        return callActivityPath;
    }

    public void setCallActivityPath(String callActivityPath) {
        this.callActivityPath = callActivityPath;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public Boolean getEnded() {
        return ended;
    }

    public void setEnded(Boolean ended) {
        this.ended = ended;
    }

    public Boolean getManuallyEnd() {
        return manuallyEnd;
    }

    public void setManuallyEnd(Boolean manuallyEnd) {
        this.manuallyEnd = manuallyEnd;
    }


    public String getWebBaseAddress() {
        return webBaseAddress;
    }

    public void setWebBaseAddress(String webBaseAddress) {
        this.webBaseAddress = webBaseAddress;
    }

    public String getWebBaseAddressAbsolute() {
        return webBaseAddressAbsolute;
    }

    public void setWebBaseAddressAbsolute(String webBaseAddressAbsolute) {
        this.webBaseAddressAbsolute = webBaseAddressAbsolute;
    }

    public String getApiBaseAddress() {
        return apiBaseAddress;
    }

    public void setApiBaseAddress(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
    }

    public String getApiBaseAddressAbsolute() {
        return apiBaseAddressAbsolute;
    }

    public void setApiBaseAddressAbsolute(String apiBaseAddressAbsolute) {
        this.apiBaseAddressAbsolute = apiBaseAddressAbsolute;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}
