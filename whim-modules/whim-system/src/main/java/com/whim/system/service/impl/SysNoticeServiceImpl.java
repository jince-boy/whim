package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.file.support.FileUploadSecurity;
import com.whim.file.support.ImageThumbnailGenerator;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.event.NoticeChangedEvent;
import com.whim.system.mapper.SysNoticeMapper;
import com.whim.system.mapper.SysNoticeReadMapper;
import com.whim.system.model.dto.file.SysFileUploadDTO;
import com.whim.system.model.dto.notice.SysNoticeAddDTO;
import com.whim.system.model.dto.notice.SysNoticeImageQueryDTO;
import com.whim.system.model.dto.notice.SysNoticeInboxQueryDTO;
import com.whim.system.model.dto.notice.SysNoticeQueryDTO;
import com.whim.system.model.dto.notice.SysNoticeUpdateDTO;
import com.whim.system.model.entity.SysFile;
import com.whim.system.model.entity.SysNotice;
import com.whim.system.model.entity.SysNoticeRead;
import com.whim.system.model.vo.notice.SysNoticeBoxVO;
import com.whim.system.model.vo.notice.SysNoticeDetailVO;
import com.whim.system.model.vo.notice.SysNoticeInboxItemVO;
import com.whim.system.model.vo.notice.SysNoticeVO;
import com.whim.system.service.ISysFileService;
import com.whim.system.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 维护组织级公告与按账号隔离的已读记录。
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

    private static final int VISIBLE = 0;
    private static final String NOTICE_IMAGE_SOURCE = "noticeImage";
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif");

    private final AuthenticationContext authentication;
    private final ApplicationEventPublisher events;
    private final SysNoticeReadMapper readMapper;
    private final ISysFileService files;

    /**
     * 分页查询管理端公告。
     *
     * @param query 筛选和分页条件
     * @return 公告摘要分页
     */
    @Override
    public PageDataVO<SysNoticeVO> getNoticePage(SysNoticeQueryDTO query) {
        LambdaQueryWrapper<SysNotice> criteria = new LambdaQueryWrapper<>();
        criteria.select(SysNotice::getId, SysNotice::getTitle, SysNotice::getNoticeType, SysNotice::getStatus,
                        SysNotice::getPublishTime, SysNotice::getCreateTime)
                .like(StringUtils.hasText(query.getTitle()), SysNotice::getTitle, query.getTitle())
                .eq(query.getNoticeType() != null, SysNotice::getNoticeType, query.getNoticeType())
                .eq(query.getStatus() != null, SysNotice::getStatus, query.getStatus())
                .orderByDesc(SysNotice::getCreateTime, SysNotice::getId);
        return new PageDataVO<>(page(query.getPage(), criteria).convert(row -> BeanConvertUtils.convert(row, SysNoticeVO.class)));
    }

    /**
     * 分页查询当前用户可以阅读的公告。
     *
     * @param query 分页条件
     * @return 含已读状态的公告分页
     */
    @Override
    public PageDataVO<SysNoticeInboxItemVO> getInboxPage(SysNoticeInboxQueryDTO query) {
        return new PageDataVO<>(baseMapper.getInboxPage(query.getPage(), authentication.getUserId(),
                query.getNoticeType(), LocalDateTime.now()));
    }

    /**
     * 查询管理端公告详情。
     *
     * @param id 公告ID
     * @return 公告详情，不存在时为空
     */
    @Override
    public SysNoticeDetailVO getNoticeDetail(Long id) {
        return BeanConvertUtils.convert(getById(id), SysNoticeDetailVO.class);
    }

    /**
     * 按公告可见性和已保存的图片关联读取安全预览。
     *
     * @param query 公告和图片文件ID
     * @return PNG图片内容，不可访问时为空数组
     */
    @Override
    public byte[] getNoticeImage(SysNoticeImageQueryDTO query) {
        // 已关闭的公告仅在管理详情中可读，文件ID本身不能授予读取权限。
        UserInfo user = authentication.getCurrentUserInfo();
        boolean canViewClosed = user.isSuperAdministrator()
                || user.getPermissionCodeSet().contains("system:notice:detail");
        SysNotice notice = lambdaQuery().select(SysNotice::getContentHtml)
                .eq(SysNotice::getId, query.getNoticeId()).eq(!canViewClosed, SysNotice::getStatus, VISIBLE)
                .le(!canViewClosed, SysNotice::getPublishTime, LocalDateTime.now()).one();
        if (notice == null || !StringUtils.hasText(notice.getContentHtml())
                || !NoticeContentPolicy.getImageIds(Jsoup.parseBodyFragment(notice.getContentHtml()).body()).contains(query.getFileId())) {
            return new byte[0];
        }
        return files.getFileImageByUploadSource(query.getFileId(), NOTICE_IMAGE_SOURCE, 2048);
    }

    /**
     * 查询当前用户未读数与最近公告。
     *
     * @param noticeType 最近消息的类型，为空时查询全部
     * @return 公告消息盒子
     */
    @Override
    @Transactional(readOnly = true)
    public SysNoticeBoxVO getNoticeBox(Integer noticeType) {
        Long userId = authentication.getUserId();
        LocalDateTime now = LocalDateTime.now();
        Page<SysNoticeInboxItemVO> recent = baseMapper.getInboxPage(new Page<>(1, 5, false), userId, noticeType, now);
        // 下次发布可能属于另一分类，铃铛的总未读数也要在该时间刷新。
        Page<SysNotice> upcoming = lambdaQuery().select(SysNotice::getPublishTime).eq(SysNotice::getStatus, VISIBLE)
                .gt(SysNotice::getPublishTime, now).orderByAsc(SysNotice::getPublishTime).page(new Page<>(1, 1, false));
        Long refreshAfterMillis = upcoming.getRecords().isEmpty() ? null
                : Math.max(1L, Duration.between(now, upcoming.getRecords().getFirst().getPublishTime()).toMillis());
        return new SysNoticeBoxVO(baseMapper.getUnreadCount(userId, now), recent.getRecords(), refreshAfterMillis);
    }

    /**
     * 新增系统公告。
     *
     * @param parameter 标题、类型、正文与状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addNotice(SysNoticeAddDTO parameter) {
        SysNotice notice = BeanConvertUtils.convert(parameter, SysNotice.class);
        validateNoticeContent(notice, null);
        if (notice.getStatus() == VISIBLE && notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
        }
        if (!save(notice)) {
            throw new ServiceException("新增公告失败");
        }
        if (notice.getStatus() == VISIBLE) {
            events.publishEvent(new NoticeChangedEvent(null));
        }
    }

    /**
     * 校验图片实际内容并通过系统默认存储创建文件。
     *
     * @param file 本地图片
     * @return 文件ID
     */
    @Override
    public Long addNoticeImage(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > ImageThumbnailGenerator.MAXIMUM_SOURCE_BYTES
                || !IMAGE_TYPES.contains(Objects.requireNonNullElse(file.getContentType(), ""))) {
            throw new IllegalArgumentException("请上传不超过20 MiB的JPG、PNG或GIF图片");
        }
        try (InputStream header = file.getInputStream()) {
            if (!IMAGE_TYPES.contains(FileUploadSecurity.getContentType(header))) {
                throw new IllegalArgumentException("公告图片仅支持JPG、PNG或GIF格式");
            }
            if (ImageThumbnailGenerator.generateThumbnail(file.getInputStream(), 96, 96).length == 0) {
                throw new IllegalArgumentException("图片无法解码或像素尺寸过大");
            }
        } catch (IOException exception) {
            throw new ServiceException("读取公告图片失败", exception);
        }
        SysFileUploadDTO upload = new SysFileUploadDTO();
        upload.setUploadSource(NOTICE_IMAGE_SOURCE);
        upload.setFile(file);
        return files.addFile(upload).getId();
    }

    /**
     * 按编辑版本更新系统公告。
     *
     * @param parameter 公告ID、原版本及新内容
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNotice(SysNoticeUpdateDTO parameter) {
        SysNotice original = lambdaQuery()
                .select(SysNotice::getVersion, SysNotice::getStatus, SysNotice::getPublishTime, SysNotice::getContentHtml)
                .eq(SysNotice::getId, parameter.getId()).one();
        if (original == null || !Objects.equals(original.getVersion(), parameter.getVersion())) {
            return false;
        }
        SysNotice notice = BeanConvertUtils.convert(parameter, SysNotice.class);
        validateNoticeContent(notice, original);
        LocalDateTime now = LocalDateTime.now();
        if (!parameter.isPublishTimeSpecified()) {
            // 旧客户端不传时间时保留原计划；重新启用已过期的关闭公告仍按保存时间发布。
            LocalDateTime previous = original.getPublishTime();
            boolean reopening = original.getStatus() != VISIBLE && notice.getStatus() == VISIBLE;
            notice.setPublishTime(reopening && previous != null && !previous.isAfter(now) ? now : previous);
        } else if (original.getPublishTime() != null && original.getPublishTime().withNano(0).equals(notice.getPublishTime())) {
            // 日期选择器精确到秒，普通内容编辑应保留原有毫秒及收件箱排序。
            notice.setPublishTime(original.getPublishTime());
        }
        if (notice.getStatus() == VISIBLE && notice.getPublishTime() == null) {
            notice.setPublishTime(now);
        }
        if (!updateById(notice)) {
            return false;
        }
        if (original.getStatus() == VISIBLE || notice.getStatus() == VISIBLE) {
            events.publishEvent(new NoticeChangedEvent(null));
        }
        return true;
    }

    /**
     * 逻辑删除系统公告。
     *
     * @param id 公告ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotice(Long id) {
        if (!removeById(id)) {
            return false;
        }
        // 广播只携带刷新信号，关闭公告的标题、正文和标识不会泄露。
        events.publishEvent(new NoticeChangedEvent(null));
        return true;
    }

    /**
     * 读取可见公告并记录当前用户首次阅读。
     *
     * @param id 公告ID
     * @return 公告详情，已关闭或不存在时为空
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysNoticeDetailVO readNotice(Long id) {
        // 直接打开详情也要校验发布时间和状态，避免通过旧链接提前阅读或写入已读记录。
        SysNotice notice = lambdaQuery().eq(SysNotice::getId, id).eq(SysNotice::getStatus, VISIBLE)
                .le(SysNotice::getPublishTime, LocalDateTime.now()).one();
        if (notice == null) {
            return null;
        }
        Long userId = authentication.getUserId();
        SysNoticeRead receipt = new SysNoticeRead();
        receipt.setNoticeId(id);
        receipt.setUserId(userId);
        readMapper.addFirstRead(receipt);
        events.publishEvent(new NoticeChangedEvent(userId));
        return BeanConvertUtils.convert(notice, SysNoticeDetailVO.class);
    }

    /**
     * 规范正文并批量校验图片来源，允许编辑者保留原文已引用的其他作者图片。
     *
     * @param notice 待写入的公告
     * @param original 修改前的公告，新增时为空
     */
    private void validateNoticeContent(SysNotice notice, SysNotice original) {
        Set<Long> ids = NoticeContentPolicy.normalizeContent(notice);
        if (ids.isEmpty()) {
            return;
        }
        Set<Long> retained = original != null && StringUtils.hasText(original.getContentHtml())
                ? NoticeContentPolicy.getImageIds(Jsoup.parseBodyFragment(original.getContentHtml()).body()) : Set.of();
        // 新引用必须是本人上传的公告图片，不能借用其他草稿或文件管理中的普通附件。
        Long userId = authentication.getUserId();
        long count = files.lambdaQuery().in(SysFile::getId, ids).eq(SysFile::getUploadSource, NOTICE_IMAGE_SOURCE)
                .in(SysFile::getContentType, IMAGE_TYPES).le(SysFile::getFileSize, ImageThumbnailGenerator.MAXIMUM_SOURCE_BYTES)
                .and(filter -> filter.eq(SysFile::getCreateBy, userId)
                        .or(!retained.isEmpty()).in(!retained.isEmpty(), SysFile::getId, retained)).count();
        if (count != ids.size()) {
            throw new IllegalArgumentException("正文包含不可使用的图片，请重新上传");
        }
    }
}
