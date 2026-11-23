package com.xhzb.nursing.service;

import java.util.List;
import com.xhzb.nursing.domain.NursingLevel;
import com.xhzb.nursing.domain.NursingPlan;
import com.xhzb.nursing.domain.NursingProject;

/** 护理计划及护理等级Service接口。 */
public interface INursingManageService
{
    /** 查询护理计划列表。 */
    public List<NursingPlan> plans(NursingPlan nursingPlan);

    /** 查询护理计划详情。 */
    public NursingPlan plan(Long id);

    /** 新增或修改护理计划。 */
    public int savePlan(NursingPlan nursingPlan);

    /** 批量删除护理计划。 */
    public int deletePlans(Long[] ids);

    /** 查询可关联护理项目。 */
    public List<NursingProject> projects();

    /** 查询护理等级列表。 */
    public List<NursingLevel> levels(NursingLevel nursingLevel);

    /** 查询护理等级详情。 */
    public NursingLevel level(Long id);

    /** 新增或修改护理等级。 */
    public int saveLevel(NursingLevel nursingLevel);

    /** 批量删除护理等级。 */
    public int deleteLevels(Long[] ids);
}
