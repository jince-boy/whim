package com.whim.system.service.impl;

import com.whim.json.utils.JsonUtils;
import com.whim.system.model.entity.SysNotice;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 规范公告可发布的排版，并为旧客户端生成纯文本正文。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class NoticeContentPolicy {

    private static final int MAX_TEXT_LENGTH = 10000;
    private static final int MAX_HTML_BYTES = 256 * 1024;
    private static final Cleaner CLEANER = new Cleaner(Safelist.none()
            .addTags("p", "br", "h1", "h2", "h3", "h4", "h5", "h6", "strong", "b", "em", "i", "u", "s", "a", "ul", "ol", "li", "img",
                    "table", "thead", "tbody", "tr", "th", "td")
            .addAttributes("a", "href").addProtocols("a", "href", "https", "http")
            .addAttributes("img", "data-file-id", "alt")
            .addAttributes("ol", "start")
            .addAttributes("th", "colspan", "rowspan").addAttributes("td", "colspan", "rowspan"));

    /**
     * 在写入前统一校验正文，保留可展示的标记并同步纯文本。
     *
     * @param notice 待保存的公告实体
     * @return 需要校验文件归属的图片ID
     */
    static Set<Long> normalizeContent(SysNotice notice) {
        Set<Long> imageIds = Set.of();
        String html = notice.getContentHtml();
        if (html != null) {
            if (JsonUtils.toBytes(html).length > MAX_HTML_BYTES) {
                throw new IllegalArgumentException("公告排版内容过大，请简化格式");
            }
            Document document = CLEANER.clean(Jsoup.parseBodyFragment(html));
            document.outputSettings().prettyPrint(false);
            Element body = document.body();
            imageIds = getImageIds(body);
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
     * 从正文中的图片节点提取文件ID，拒绝外链、临时地址和无效标识。
     *
     * @param body 清洗后的公告正文
     * @return 不重复的公告图片文件ID
     */
    static Set<Long> getImageIds(Element body) {
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
