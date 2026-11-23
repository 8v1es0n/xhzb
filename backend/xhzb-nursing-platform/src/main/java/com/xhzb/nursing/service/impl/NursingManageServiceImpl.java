package com.xhzb.nursing.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xhzb.common.exception.ServiceException;
import com.xhzb.common.utils.DateUtils;
import com.xhzb.nursing.domain.NursingLevel;
import com.xhzb.nursing.domain.NursingPlan;
import com.xhzb.nursing.domain.NursingProject;
import com.xhzb.nursing.domain.NursingProjectPlan;
import com.xhzb.nursing.mapper.NursingManageMapper;
import com.xhzb.nursing.service.INursingManageService;

/** 护理计划及护理等级Service业务层处理。 */
@Service
public class NursingManageServiceImpl implements INursingManageService
{
    @Autowired
    private NursingManageMapper nursingManageMapper;

    /** 查询护理计划列表。 */
    @Override
    public List<NursingPlan> plans(NursingPlan nursingPlan)
    {
        return nursingManageMapper.selectPlanList(nursingPlan);
    }

    /** 查询护理计划及其关联项目。 */
    @Override
    public NursingPlan plan(Long id)
    {
        NursingPlan nursingPlan = nursingManageMapper.selectPlanById(id);
        if (nursingPlan != null)
        {
            nursingPlan.setProjects(nursingManageMapper.selectPlanProjects(id));
        }
        return nursingPlan;
    }

    /** 新增或修改护理计划，并同步维护关联项目。 */
    @Override
    @Transactional
    public int savePlan(NursingPlan nursingPlan)
    {
        if (nursingPlan.getId() == null)
        {
            nursingPlan.setCreateTime(DateUtils.getNowDate());
            nursingManageMapper.insertPlan(nursingPlan);
        }
        else
        {
            nursingPlan.setUpdateTime(DateUtils.getNowDate());
            nursingManageMapper.updatePlan(nursingPlan);
        }

        nursingManageMapper.deletePlanProjects(nursingPlan.getId());
        List<NursingProjectPlan> projects = nursingPlan.getProjects();
        if (projects != null)
        {
            Set<Long> projectIds = new HashSet<>();
            for (NursingProjectPlan project : projects)
            {
                if (project.getProjectId() == null || !projectIds.add(project.getProjectId()))
                {
                    throw new ServiceException("同一护理计划内的护理项目不能重复");
                }
                project.setPlanId(nursingPlan.getId());
                project.setCreateTime(DateUtils.getNowDate());
                nursingManageMapper.insertPlanProject(project);
            }
        }
        return 1;
    }

    /** 批量删除护理计划；存在项目或等级引用时拒绝删除。 */
    @Override
    public int deletePlans(Long[] ids)
    {
        for (Long id : ids)
        {
            if (nursingManageMapper.countPlanReferences(id) > 0)
            {
                throw new ServiceException("该护理计划已被护理项目或护理等级引用，无法删除");
            }
        }
        return nursingManageMapper.deletePlans(ids);
    }

    /** 查询可用于计划配置的护理项目。 */
    @Override
    public List<NursingProject> projects()
    {
        return nursingManageMapper.selectAvailableProjects();
    }

    /** 查询护理等级列表。 */
    @Override
    public List<NursingLevel> levels(NursingLevel nursingLevel)
    {
        return nursingManageMapper.selectLevelList(nursingLevel);
    }

    /** 查询护理等级详情。 */
    @Override
    public NursingLevel level(Long id)
    {
        return nursingManageMapper.selectLevelById(id);
    }

    /** 新增或修改护理等级。 */
    @Override
    public int saveLevel(NursingLevel nursingLevel)
    {
        if (nursingManageMapper.countLevelName(nursingLevel.getName(), nursingLevel.getId()) > 0)
        {
            throw new ServiceException("护理等级名称已存在");
        }
        if (nursingManageMapper.countEnabledPlan(nursingLevel.getLplanId()) == 0)
        {
            throw new ServiceException("请选择启用状态的护理计划");
        }

        if (nursingLevel.getId() == null)
        {
            nursingLevel.setCreateTime(DateUtils.getNowDate());
            return nursingManageMapper.insertLevel(nursingLevel);
        }
        nursingLevel.setUpdateTime(DateUtils.getNowDate());
        return nursingManageMapper.updateLevel(nursingLevel);
    }

    /** 批量删除护理等级。 */
    @Override
    public int deleteLevels(Long[] ids)
    {
        return nursingManageMapper.deleteLevels(ids);
    }
}
