package com.xhzb.nursing.domain;

import java.util.Date;

/** 护理计划与护理项目关联对象 nursing_project_plan。 */
public class NursingProjectPlan
{
    private Long id;
    private Long planId;
    private Long projectId;
    private String projectName;
    private String executeTime;
    private Long executeCycle;
    private Long executeFrequency;
    private Date createTime;
    private Date updateTime;
    private Long createBy;
    private Long updateBy;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getExecuteTime() { return executeTime; }
    public void setExecuteTime(String executeTime) { this.executeTime = executeTime; }
    public Long getExecuteCycle() { return executeCycle; }
    public void setExecuteCycle(Long executeCycle) { this.executeCycle = executeCycle; }
    public Long getExecuteFrequency() { return executeFrequency; }
    public void setExecuteFrequency(Long executeFrequency) { this.executeFrequency = executeFrequency; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
