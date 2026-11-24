package com.whim.controller.system;

import cn.dev33.satoken.annotation.SaMode;
import com.whim.file.support.FileUploadSecurity;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.system.model.dto.notice.SysNoticeInboxQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.notice.SysNoticeAddDTO;
import com.whim.system.model.dto.notice.SysNoticeImageQueryDTO;
import com.whim.system.model.dto.notice.SysNoticeQueryDTO;
import com.whim.system.model.dto.notice.SysNoticeUpdateDTO;
import com.whim.system.model.vo.notice.SysNoticeBoxVO;
import com.whim.system.model.vo.notice.SysNoticeDetailVO;
import com.whim.system.model.vo.notice.SysNoticeInboxItemVO;
import com.whim.system.model.vo.notice.SysNoticeVO;
import com.whim.system.service.ISysNoticeService;
import com.whim.web.annotation.XssIgnore;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 系统公告管理及登录用户阅读接口。
 */
@RestController
@SystemCheckLogin
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class SysNoticeController {
    private final ISysNoticeService notices;

    /**
     * 上传公告正文图片。
     *
     * @param file 本地图片
     * @return 可写入正文的文件ID
     */
    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SystemCheckPermission(value = {"system:notice:add", "system:notice:update"}, mode = SaMode.OR)
    @Log(title = "上传公告图片", type = LogType.INSERT, excludeFields = "file")
    public Result<Long> addNoticeImage(@RequestParam MultipartFile file) {
        return Result.success(notices.addNoticeImage(file));
    }

    /**
     * 读取指定公告中的图片预览。
     *
     * @param query 公告和图片文件ID
     * @return PNG图片或统一错误响应
     */
    @GetMapping("/image")
    public ResponseEntity<?> getNoticeImage(@Valid SysNoticeImageQueryDTO query) {
        byte[] image = notices.getNoticeImage(query);
        if (image.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).cacheControl(CacheControl.noStore())
                    .contentType(MediaType.APPLICATION_JSON).body(Result.error(HttpStatus.NOT_FOUND, "公告图片不可用"));
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", FileUploadSecurity.CONTENT_SECURITY_POLICY)
                .contentLength(image.length).body(image);
    }

    /**
     * 分页查询管理端公告。
     *
     * @param query 筛选和分页条件
     * @return 公告摘要分页
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:notice:page")
    public Result<PageDataVO<SysNoticeVO>> getNoticePage(@Valid SysNoticeQueryDTO query) {
        return Result.success(notices.getNoticePage(query));
    }

    /**
     * 分页查询已发布的公共消息和当前账号收到的系统消息。
     *
     * @param query 分页条件
     * @return 含本人已读状态的消息分页
     */
    @GetMapping("/inbox")
    public Result<PageDataVO<SysNoticeInboxItemVO>> getInboxPage(@Valid SysNoticeInboxQueryDTO query) {
        return Result.success(notices.getInboxPage(query));
    }

    /**
     * 只读检查当前账号可见的消息版本。
     *
     * @param id 消息ID
     * @return 消息当前版本
     */
    @GetMapping("/inboxVersion")
    public Result<Integer> getInboxNoticeVersion(@RequestParam Long id) {
        Integer version = notices.getInboxNoticeVersion(id);
        return version == null ? Result.error(HttpStatus.NOT_FOUND, "消息当前不可见或不存在", null) : Result.success(version);
    }

    /**
     * 查询管理端公告详情。
     *
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/detail")
    @SystemCheckPermission("system:notice:detail")
    public Result<SysNoticeDetailVO> getNoticeDetail(@RequestParam Long id) {
        SysNoticeDetailVO detail = notices.getNoticeDetail(id);
        return detail == null ? Result.error(HttpStatus.NOT_FOUND, "公告不存在", null) : Result.success(detail);
    }

    /**
     * 查询当前账号各分类的未读数、未读总数与所选分类的最近消息。
     *
     * @param noticeType 最近消息的类型，为空时查询全部
     * @return 消息中心摘要
     */
    @GetMapping("/box")
    public Result<SysNoticeBoxVO> getNoticeBox(@RequestParam(required = false)
                                               @Min(value = 1, message = "消息类型无效")
                                               @Max(value = 3, message = "消息类型无效") Integer noticeType) {
        return Result.success(notices.getNoticeBox(noticeType));
    }

    /**
     * 新增系统公告。
     *
     * @param parameter 标题、类型、正文与状态
     * @return 操作结果
     */
    @PostMapping("/add")
    @SystemCheckPermission("system:notice:add")
    @XssIgnore(bodyFields = "contentHtml")
    @Log(title = "通知公告", type = LogType.INSERT, excludeFields = {"content", "contentHtml"})
    public Result<Void> addNotice(@Valid @RequestBody SysNoticeAddDTO parameter) {
        notices.addNotice(parameter);
        return Result.success("公告保存成功");
    }

    /**
     * 按编辑版本更新系统公告。
     *
     * @param parameter 公告ID、原版本及新内容
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @SystemCheckPermission("system:notice:update")
    @XssIgnore(bodyFields = "contentHtml")
    @Log(title = "通知公告", type = LogType.UPDATE, excludeFields = {"content", "contentHtml"})
    public Result<Void> updateNotice(@Valid @RequestBody SysNoticeUpdateDTO parameter) {
        return notices.updateNotice(parameter) ? Result.success("公告更新成功")
                : Result.error(HttpStatus.CONFLICT, "公告已被修改或删除，请刷新后重试");
    }

    /**
     * 逻辑删除系统公告。
     *
     * @param id 公告ID
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:notice:delete")
    @Log(title = "通知公告", type = LogType.DELETE)
    public Result<Void> deleteNotice(@RequestParam Long id) {
        return notices.deleteNotice(id) ? Result.success("公告删除成功") : Result.error(HttpStatus.NOT_FOUND, "公告不存在");
    }

    /**
     * 清空当前登录账号截至指定消息的系统消息。
     *
     * @param throughId 确认清空时的最新系统消息ID
     * @return 清空结果
     */
    @DeleteMapping("/clearSystemMessages")
    @Log(title = "个人系统消息", type = LogType.CLEAN)
    public Result<Void> clearSystemMessages(@RequestParam Long throughId) {
        notices.clearSystemMessages(throughId);
        return Result.success("系统消息已清空");
    }

    /**
     * 读取本人可见的消息并记录本次返回的版本。
     *
     * @param id 消息ID
     * @return 可见消息详情
     */
    @PostMapping("/read")
    public Result<SysNoticeDetailVO> readNotice(@RequestParam Long id) {
        SysNoticeDetailVO detail = notices.readNotice(id);
        return detail == null ? Result.error(HttpStatus.NOT_FOUND, "消息当前不可见或不存在", null) : Result.success(detail);
    }

    /**
     * 将当前登录账号所有分类中的可见未读消息标为已读。
     *
     * @return 标记结果
     */
    @PostMapping("/markAllRead")
    public Result<Void> markAllNoticesRead() {
        notices.markAllNoticesRead();
        return Result.success("消息已全部标为已读");
    }
}
