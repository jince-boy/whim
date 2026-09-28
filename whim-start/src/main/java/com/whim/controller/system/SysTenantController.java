package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.tenant.MemberStatusDTO;
import com.whim.system.model.dto.tenant.TenantPackagePermissionAssignDTO;
import com.whim.system.model.dto.tenant.TenantPackageStatusDTO;
import com.whim.system.model.dto.tenant.TenantPackageUpdateDTO;
import com.whim.system.model.dto.tenant.TenantStatusDTO;
import com.whim.system.model.vo.tenant.MemberVO;
import com.whim.system.service.ISysTenantPackagePermissionService;
import com.whim.system.service.ISysTenantPackageService;
import com.whim.system.service.ISysTenantService;
import com.whim.system.service.ISysUserTenantService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统租户表控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/tenant")
public class SysTenantController {

    /**
     * 系统租户表服务对象
     */
    private final ISysTenantService sysTenantService;
    private final ISysUserTenantService sysUserTenantService;
    private final ISysTenantPackageService sysTenantPackageService;
    private final ISysTenantPackagePermissionService sysTenantPackagePermissionService;

    /** 查询当前租户已有成员。 */
    @GetMapping("/current/members")
    @SystemCheckPermission("system:member:list")
    public Result<List<MemberVO>> listMembers() {
        return Result.success("成员查询成功", sysUserTenantService.listMembers());
    }

    /** 修改当前租户成员状态。 */
    @PutMapping("/current/members/{userId}/status")
    @SystemCheckPermission("system:member:status")
    public Result<Void> setMemberStatus(@PathVariable Long userId, @RequestBody @Valid MemberStatusDTO request) {
        sysUserTenantService.setMemberStatus(userId, request.getStatus());
        return Result.success("成员状态修改成功");
    }

    /** 修改租户状态。 */
    @PutMapping("/{tenantId}/status")
    @SystemCheckPermission("system:platform:tenant:status")
    public Result<Void> setTenantStatus(@PathVariable Long tenantId, @RequestBody @Valid TenantStatusDTO request) {
        sysTenantService.setTenantStatus(tenantId, request.getStatus());
        return Result.success("租户状态修改成功");
    }

    /** 修改租户使用的套餐。 */
    @PutMapping("/{tenantId}/package")
    @SystemCheckPermission("system:platform:tenant:package")
    public Result<Void> setTenantPackage(@PathVariable Long tenantId,
                                         @RequestBody @Valid TenantPackageUpdateDTO request) {
        sysTenantService.setTenantPackage(tenantId, request.getPackageId());
        return Result.success("租户套餐修改成功");
    }

    /** 修改租户套餐状态。 */
    @PutMapping("/packages/{packageId}/status")
    @SystemCheckPermission("system:platform:package:status")
    public Result<Void> setPackageStatus(@PathVariable Long packageId,
                                         @RequestBody @Valid TenantPackageStatusDTO request) {
        sysTenantPackageService.setPackageStatus(packageId, request.getStatus());
        return Result.success("套餐状态修改成功");
    }

    /** 覆盖指定租户套餐的权限。 */
    @PutMapping("/packages/{packageId}/permissions")
    @SystemCheckPermission("system:platform:package:assign")
    public Result<Void> replacePackagePermissions(@PathVariable Long packageId,
                                                  @RequestBody @Valid TenantPackagePermissionAssignDTO request) {
        sysTenantPackagePermissionService.replacePackagePermissions(packageId, request.getPermissionIds());
        return Result.success("套餐权限分配成功");
    }
}

