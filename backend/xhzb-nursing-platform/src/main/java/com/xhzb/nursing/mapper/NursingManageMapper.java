package com.xhzb.nursing.mapper;

import com.xhzb.nursing.domain.NursingLevel;
import com.xhzb.nursing.domain.NursingPlan;
import com.xhzb.nursing.domain.NursingProject;
import com.xhzb.nursing.domain.NursingProjectPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NursingManageMapper {
    List<NursingPlan> selectPlanList(NursingPlan plan);

    NursingPlan selectPlanById(Long id);

    List<NursingProjectPlan> selectPlanProjects(Long id);

    int insertPlan(NursingPlan plan);

    int updatePlan(NursingPlan plan);

    int deletePlanProjects(Long id);

    int insertPlanProject(NursingProjectPlan row);

    int countPlanReferences(Long id);

    int deletePlans(Long[] ids);

    List<NursingProject> selectAvailableProjects();

    List<NursingLevel> selectLevelList(NursingLevel level);

    NursingLevel selectLevelById(Long id);

    int insertLevel(NursingLevel level);

    int updateLevel(NursingLevel level);

    int deleteLevels(Long[] ids);

    int countLevelName(@Param("name") String name, @Param("id") Long id);

    int countEnabledPlan(Long id);
}
