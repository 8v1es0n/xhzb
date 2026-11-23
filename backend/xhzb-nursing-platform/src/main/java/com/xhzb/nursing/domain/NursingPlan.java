package com.xhzb.nursing.domain;

import com.xhzb.common.core.domain.BaseEntity;
import java.util.List;

/** 护理计划对象 nursing_plan。 */
public class NursingPlan extends BaseEntity
{
    private Long id;
    private Long sortNo;
    private String planName;
    private Long status;
    private List<NursingProjectPlan> projects;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getSortNo()
    {
        return sortNo;
    }

    public void setSortNo(Long sortNo)
    {
        this.sortNo = sortNo;
    }

    public String getPlanName()
    {
        return planName;
    }

    public void setPlanName(String planName)
    {
        this.planName = planName;
    }

    public Long getStatus()
    {
        return status;
    }

    public void setStatus(Long status)
    {
        this.status = status;
    }

    public List<NursingProjectPlan> getProjects()
    {
        return projects;
    }

    public void setProjects(List<NursingProjectPlan> projects)
    {
        this.projects = projects;
    }
}
