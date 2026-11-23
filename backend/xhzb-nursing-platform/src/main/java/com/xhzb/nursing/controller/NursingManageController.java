package com.xhzb.nursing.controller;

import com.xhzb.common.annotation.Log;
import com.xhzb.common.core.controller.BaseController;
import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.common.core.page.TableDataInfo;
import com.xhzb.common.enums.BusinessType;
import com.xhzb.nursing.domain.NursingLevel;
import com.xhzb.nursing.domain.NursingPlan;
import com.xhzb.nursing.service.INursingManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NursingManageController extends BaseController {
    @Autowired
    private INursingManageService service;

    @PreAuthorize("@ss.hasPermi('nursing:plan:list')")
    @GetMapping("/nursing/plan/list")
    public TableDataInfo plans(NursingPlan p) {
        startPage();
        List<NursingPlan> rows = service.plans(p);
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('nursing:plan:query')")
    @GetMapping("/nursing/plan/{id}")
    public AjaxResult plan(@PathVariable Long id) {
        return success(service.plan(id));
    }

    @PreAuthorize("@ss.hasPermi('nursing:plan:add')")
    @Log(title = "护理计划", businessType = BusinessType.INSERT)
    @PostMapping("/nursing/plan")
    public AjaxResult addPlan(@RequestBody NursingPlan p) {
        return toAjax(service.savePlan(p));
    }

    @PreAuthorize("@ss.hasPermi('nursing:plan:edit')")
    @Log(title = "护理计划", businessType = BusinessType.UPDATE)
    @PutMapping("/nursing/plan")
    public AjaxResult editPlan(@RequestBody NursingPlan p) {
        return toAjax(service.savePlan(p));
    }

    @PreAuthorize("@ss.hasPermi('nursing:plan:remove')")
    @Log(title = "护理计划", businessType = BusinessType.DELETE)
    @DeleteMapping("/nursing/plan/{ids}")
    public AjaxResult deletePlans(@PathVariable Long[] ids) {
        return toAjax(service.deletePlans(ids));
    }

    @PreAuthorize("@ss.hasPermi('nursing:plan:list')")
    @GetMapping("/nursing/plan/projects")
    public AjaxResult projects() {
        return success(service.projects());
    }

    @PreAuthorize("@ss.hasPermi('nursing:level:list')")
    @GetMapping("/nursing/level/list")
    public TableDataInfo levels(NursingLevel p) {
        startPage();
        List<NursingLevel> rows = service.levels(p);
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('nursing:level:query')")
    @GetMapping("/nursing/level/{id}")
    public AjaxResult level(@PathVariable Long id) {
        return success(service.level(id));
    }

    @PreAuthorize("@ss.hasPermi('nursing:level:add')")
    @Log(title = "护理等级", businessType = BusinessType.INSERT)
    @PostMapping("/nursing/level")
    public AjaxResult addLevel(@RequestBody NursingLevel p) {
        return toAjax(service.saveLevel(p));
    }

    @PreAuthorize("@ss.hasPermi('nursing:level:edit')")
    @Log(title = "护理等级", businessType = BusinessType.UPDATE)
    @PutMapping("/nursing/level")
    public AjaxResult editLevel(@RequestBody NursingLevel p) {
        return toAjax(service.saveLevel(p));
    }

    @PreAuthorize("@ss.hasPermi('nursing:level:remove')")
    @Log(title = "护理等级", businessType = BusinessType.DELETE)
    @DeleteMapping("/nursing/level/{ids}")
    public AjaxResult deleteLevels(@PathVariable Long[] ids) {
        return toAjax(service.deleteLevels(ids));
    }

    @PreAuthorize("@ss.hasPermi('nursing:level:list')")
    @GetMapping("/nursing/level/enabledPlans")
    public AjaxResult enabledPlans() {
        NursingPlan p = new NursingPlan();
        p.setStatus(1L);
        return success(service.plans(p));
    }
}
