package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.constant.CacheKeys;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.file.support.FileUploadSecurity;
import com.whim.file.support.ImageThumbnailGenerator;
import com.whim.json.utils.JsonUtils;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.redis.utils.CacheUtils;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
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

    private static final int SYSTEM_MESSAGE = 3;
    private static final int READ_RECEIPT_BATCH_SIZE = 500;
    private static final int VISIBLE = 0;
    private static final String NOTICE_IMAGE_SOURCE = "noticeImage";
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif");
    private static final int MAX_TEXT_LENGTH = 10000;
    private static final int MAX_HTML_BYTES = 256 * 1024;
    private static final Cleaner NOTICE_HTML_CLEANER = new Cleaner(Safelist.none()
            .addTags("p", "br", "h1", "h2", "h3", "h4", "h5", "h6", "strong", "b", "em", "i", "u", "s", "a", "ul", "ol", "li", "img",
                    "table", "thead", "tbody", "tr", "th", "td")
            .addAttributes("a", "href").addProtocols("a", "href", "https", "http")
            .addAttributes("img", "data-file-id", "alt")
            .addAttributes("ol", "start")
            .addAttributes("th", "colspan", "rowspan").addAttributes("td", "colspan", "rowspan"));

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
                .isNull(SysNotice::getRecipientUserId)
                .like(StringUtils.hasText(query.getTitle()), SysNotice::getTitle, query.getTitle())
                .eq(query.getNoticeType() != null, SysNotice::getNoticeType, query.getNoticeType())
                .eq(query.getStatus() != null, SysNotice::getStatus, query.getStatus())
                .orderByDesc(SysNotice::getCreateTime, SysNotice::getId);
        return new PageDataVO<>(page(query.getPage(), criteria).convert(row -> BeanConvertUtils.convert(row, SysNoticeVO.class)));
    }

    /**
     * 分页查询已发布的公共消息和当前账号收到的系统消息。
     *
     * @param query 分页条件
     * @return 含本人已读状态的消息分页
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
        return BeanConvertUtils.convert(lambdaQuery().isNull(SysNotice::getRecipientUserId)
                .eq(SysNotice::getId, id).one(), SysNoticeDetailVO.class);
    }

    /**
     * 查询本人收件箱中消息的当前版本，供已打开的详情检查变更。
     *
     * @param id 消息ID
     * @return 当前可见的版本，不可见时为空
     */
    @Override
    public Integer getInboxNoticeVersion(Long id) {
        return getOneOpt(getVisibleNoticeCriteria(id, authentication.getUserId()).select(SysNotice::getVersion))
                .map(SysNotice::getVersion).orElse(null);
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
        SysNotice notice = lambdaQuery().select(SysNotice::getContentHtml).isNull(SysNotice::getRecipientUserId)
                .eq(SysNotice::getId, query.getNoticeId()).eq(!canViewClosed, SysNotice::getStatus, VISIBLE)
                .le(!canViewClosed, SysNotice::getPublishTime, LocalDateTime.now()).one();
        if (notice == null || !StringUtils.hasText(notice.getContentHtml())
                || !getNoticeImageIds(Jsoup.parseBodyFragment(notice.getContentHtml()).body()).contains(query.getFileId())) {
            return new byte[0];
        }
        return files.getFileImageByUploadSource(query.getFileId(), NOTICE_IMAGE_SOURCE, 2048);
    }

    /**
     * 查询当前账号各分类的未读数、未读总数与所选分类的最近消息。
     *
     * @param noticeType 最近消息的类型，为空时查询全部
     * @return 消息中心摘要
     */
    @Override
    @Transactional(readOnly = true)
    public SysNoticeBoxVO getNoticeBox(Integer noticeType) {
        Long userId = authentication.getUserId();
        LocalDateTime now = LocalDateTime.now();
        Page<SysNoticeInboxItemVO> recent = baseMapper.getInboxPage(new Page<>(1, 5, false), userId, noticeType, now);
        // 下次发布可能属于另一分类，铃铛的总未读数也要在该时间刷新。
        Page<SysNotice> upcoming = lambdaQuery().select(SysNotice::getPublishTime).eq(SysNotice::getStatus, VISIBLE)
                .isNull(SysNotice::getRecipientUserId).gt(SysNotice::getPublishTime, now).orderByAsc(SysNotice::getPublishTime).page(new Page<>(1, 1, false));
        Long refreshAfterMillis = upcoming.getRecords().isEmpty() ? null
                : Math.max(1L, Duration.between(now, upcoming.getRecords().getFirst().getPublishTime()).toMillis());
        return new SysNoticeBoxVO(baseMapper.getUnreadCountsByNoticeType(userId, now), recent.getRecords(), refreshAfterMillis);
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
            events.publishEvent(new NoticeChangedEvent(null, NoticeChangedEvent.NOTICE_CHANGED));
        }
    }

    /**
     * 批量保存同一业务事件的个人消息，并在事务提交后通知对应收件人。
     *
     * @param recipientIds 本次实际受影响的账号ID
     * @param title 消息标题
     * @param content 纯文本正文
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSystemMessages(Set<Long> recipientIds, String title, String content) {
        if (recipientIds.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<SysNotice> messages = recipientIds.stream().map(userId -> {
            SysNotice message = new SysNotice();
            message.setRecipientUserId(userId);
            message.setNoticeType(SYSTEM_MESSAGE);
            message.setTitle(title);
            message.setContent(content);
            message.setStatus(VISIBLE);
            message.setPublishTime(now);
            return message;
        }).toList();
        boolean saved = messages.size() == 1 ? save(messages.getFirst()) : saveBatch(messages);
        if (!saved) {
            throw new ServiceException("保存系统消息失败");
        }
        // 同一事务回滚时消息一并撤销，已离线的用户下次登录时从收件箱读取。
        events.publishEvent(new NoticeChangedEvent(Set.copyOf(recipientIds), NoticeChangedEvent.SYSTEM_MESSAGE));
    }

    /**
     * 清除迁移前的消息分类缓存，使已部署系统及时加载新增分类。
     */
    @EventListener(ApplicationStartedEvent.class)
    public void refreshNoticeTypeCache() {
        CacheUtils.evict(CacheKeys.SYS_DICT, "sys_notice_type");
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
                .select(SysNotice::getVersion, SysNotice::getStatus, SysNotice::getPublishTime, SysNotice::getTitle,
                        SysNotice::getNoticeType, SysNotice::getContent, SysNotice::getContentHtml)
                .isNull(SysNotice::getRecipientUserId).eq(SysNotice::getId, parameter.getId()).one();
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
        // 对比清洗后的正文和最终发布时间，原样保存时保留编辑版本和所有读者的已读状态。
        if (Objects.equals(original.getTitle(), notice.getTitle())
                && Objects.equals(original.getContent(), notice.getContent())
                && Objects.equals(original.getContentHtml(), notice.getContentHtml())
                && Objects.equals(original.getNoticeType(), notice.getNoticeType())
                && Objects.equals(original.getStatus(), notice.getStatus())
                && Objects.equals(original.getPublishTime(), notice.getPublishTime())) {
            return true;
        }
        if (!updateById(notice)) {
            return false;
        }
        if (original.getStatus() == VISIBLE || notice.getStatus() == VISIBLE) {
            events.publishEvent(new NoticeChangedEvent(null, NoticeChangedEvent.NOTICE_CHANGED));
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
        // 收件人不可变，确认是公开公告后按主键逻辑删除，由框架照常填充删除审计字段。
        if (!lambdaQuery().eq(SysNotice::getId, id).isNull(SysNotice::getRecipientUserId).exists() || !removeById(id)) {
            return false;
        }
        // 广播只携带刷新信号，关闭公告的标题、正文和标识不会泄露。
        events.publishEvent(new NoticeChangedEvent(null, NoticeChangedEvent.NOTICE_CHANGED));
        return true;
    }

    /**
     * 清空当前账号截至指定消息的全部系统消息，并在事务提交后刷新本人收件箱。
     *
     * @param throughId 打开确认框时看到的最新系统消息ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearSystemMessages(Long throughId) {
        Long userId = authentication.getUserId();
        // 边界也必须是本人的可见系统消息，重复确认和跨账号传入的编号均不会扩大清空范围。
        if (baseMapper.clearSystemMessages(userId, throughId, LocalDateTime.now()) > 0) {
            events.publishEvent(new NoticeChangedEvent(Set.of(userId), NoticeChangedEvent.SYSTEM_MESSAGES_CLEARED));
        }
    }

    /**
     * 读取本人可见的消息并记录本次返回的版本。
     *
     * @param id 公告ID
     * @return 消息详情，不可见或不存在时为空
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysNoticeDetailVO readNotice(Long id) {
        Long userId = authentication.getUserId();
        SysNotice notice = getOne(getVisibleNoticeCriteria(id, userId));
        if (notice == null) {
            return null;
        }
        SysNoticeRead receipt = new SysNoticeRead();
        receipt.setNoticeId(id);
        receipt.setUserId(userId);
        // 并发编辑时只确认本次查询实际返回的正文版本。
        receipt.setReadVersion(notice.getVersion());
        readMapper.updateReadReceipts(List.of(receipt));
        events.publishEvent(new NoticeChangedEvent(Set.of(userId), NoticeChangedEvent.NOTICE_READ));
        return BeanConvertUtils.convert(notice, SysNoticeDetailVO.class);
    }

    /**
     * 将当前账号各分类中已发布且可见的未读消息全部标为已读。
     */
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void markAllNoticesRead() {
        Long userId = authentication.getUserId();
        LocalDateTime now = LocalDateTime.now();
        long afterNoticeId = 0;
        // 固定发布时间和事务快照，分批写入期间新收到的消息仍保留未读。
        List<SysNoticeRead> receipts;
        do {
            receipts = baseMapper.getPendingReadReceipts(userId, now, afterNoticeId, READ_RECEIPT_BATCH_SIZE);
            if (receipts.isEmpty()) {
                break;
            }
            readMapper.updateReadReceipts(receipts);
            afterNoticeId = receipts.getLast().getNoticeId();
        } while (receipts.size() == READ_RECEIPT_BATCH_SIZE);

        // 只在整个事务提交后通知本人在线会话刷新一次。
        if (afterNoticeId > 0) {
            events.publishEvent(new NoticeChangedEvent(Set.of(userId), NoticeChangedEvent.NOTICE_READ));
        }
    }

    /**
     * 构建本人收件箱中指定消息的可见条件。
     *
     * @param id 消息ID
     * @param userId 当前账号ID
     * @return 详情阅读与版本检查共用的查询条件
     */
    private LambdaQueryWrapper<SysNotice> getVisibleNoticeCriteria(Long id, Long userId) {
        // 版本检查同样受发布时间和收件人限制，不能暴露未发布消息或他人的系统消息。
        return new LambdaQueryWrapper<SysNotice>().eq(SysNotice::getId, id).eq(SysNotice::getStatus, VISIBLE)
                .and(scope -> scope.isNull(SysNotice::getRecipientUserId).or().eq(SysNotice::getRecipientUserId, userId))
                .le(SysNotice::getPublishTime, LocalDateTime.now());
    }

    /**
     * 规范正文并批量校验图片来源，允许编辑者保留原文已引用的其他作者图片。
     *
     * @param notice 待写入的公告
     * @param original 修改前的公告，新增时为空
     */
    private void validateNoticeContent(SysNotice notice, SysNotice original) {
        Set<Long> ids = normalizeNoticeContent(notice);
        if (ids.isEmpty()) {
            return;
        }
        Set<Long> retained = original != null && StringUtils.hasText(original.getContentHtml())
                ? getNoticeImageIds(Jsoup.parseBodyFragment(original.getContentHtml()).body()) : Set.of();
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

    /**
     * 在写入前统一校验正文，保留可展示的标记并同步纯文本。
     *
     * @param notice 待保存的公告实体
     * @return 需要校验文件归属的图片ID
     */
    private Set<Long> normalizeNoticeContent(SysNotice notice) {
        Set<Long> imageIds = Set.of();
        String html = notice.getContentHtml();
        if (html != null) {
            if (JsonUtils.toBytes(html).length > MAX_HTML_BYTES) {
                throw new IllegalArgumentException("公告排版内容过大，请简化格式");
            }
            Document document = NOTICE_HTML_CLEANER.clean(Jsoup.parseBodyFragment(html));
            document.outputSettings().prettyPrint(false);
            Element body = document.body();
            imageIds = getNoticeImageIds(body);
            // 限制粘贴表格的跨度，避免极大的跨度使客户端扩展出大量空单元格。
            for (Element cell : body.select("td[colspan],td[rowspan],th[colspan],th[rowspan]")) {
                for (String attribute : new String[]{"colspan", "rowspan"}) {
                    if (cell.hasAttr(attribute) && !cell.attr(attribute).matches("[1-9]|[1-5][0-9]|6[0-4]")) {
                        cell.removeAttr(attribute);
                    }
                }
            }
            String safeHtml = body.html();
            if (JsonUtils.toBytes(safeHtml).length > MAX_HTML_BYTES) {
                throw new IllegalArgumentException("公告排版内容过大，请简化格式");
            }
            notice.setContentHtml(safeHtml);
            // 每个块贡献一个段落分隔符。已有纯文本客户端也能分行读取列表和表格。
            body.select("p,h1,h2,h3,h4,h5,h6,li:not(:has(p,li)),th:not(:has(p)),td:not(:has(p))")
                    .forEach(block -> block.appendText("\n"));
            String text = body.wholeText();
            notice.setContent(text.endsWith("\n") ? text.substring(0, text.length() - 1) : text);
        }
        String text = notice.getContent();
        if (text != null && text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("公告正文不能超过10000个字符");
        }
        if (text == null || text.replace('\u00a0', ' ').isBlank()) {
            if (imageIds.isEmpty()) {
                throw new IllegalArgumentException("请输入公告正文或上传图片");
            }
            // 纯图片公告仍为只识别纯文本的客户端提供可读摘要。
            notice.setContent("[图片]");
        }
        return imageIds;
    }

    /**
     * 从已解析的正文中提取图片文件ID，拒绝缺失或无效的标识。
     *
     * @param body 已解析的公告正文
     * @return 不重复的公告图片文件ID
     */
    private Set<Long> getNoticeImageIds(Element body) {
        Set<Long> ids = new LinkedHashSet<>();
        for (Element image : body.select("img")) {
            String value = image.attr("data-file-id");
            if (!value.matches("[1-9][0-9]{0,18}")) {
                throw new IllegalArgumentException("公告图片须通过系统图片上传后插入");
            }
            try {
                ids.add(Long.valueOf(value));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("公告图片文件ID无效");
            }
        }
        return ids;
    }
}
