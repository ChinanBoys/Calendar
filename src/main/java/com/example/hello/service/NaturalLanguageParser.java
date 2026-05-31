package com.example.hello.service;

import com.example.hello.dto.VoiceParseResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则式自然语言解析器（把中文口语解析为结构化字段）。
 *
 * <p>接口文档描述「后端调用大模型解析」，此处用确定性的规则引擎实现等价能力，
 * 既无需外部依赖、结果可复现，又便于后续替换为真正的 LLM 调用
 * （只需另写一个实现并替换 {@link VoiceParseService} 中的解析步骤）。</p>
 *
 * <p>支持：意图识别（create/query/update/delete/unknown）、相对日期（今天/明天/后天/周X）、
 * 时间与时间段（下午三点到五点 / 15:00）、提醒提前量、地点、重复规则，并给出各字段置信度。</p>
 */
@Component
public class NaturalLanguageParser {

    private static final String INTENT_CREATE = "create";
    private static final String INTENT_QUERY = "query";
    private static final String INTENT_UPDATE = "update";
    private static final String INTENT_DELETE = "delete";
    private static final String INTENT_UNKNOWN = "unknown";

    /** 删除类关键词 */
    private static final List<String> DELETE_WORDS =
            Arrays.asList("删除", "删掉", "删去", "去掉", "取消", "撤销");
    /** 修改类关键词 */
    private static final List<String> UPDATE_WORDS =
            Arrays.asList("改到", "改成", "改为", "修改", "更新", "推迟", "提前到", "挪到", "换到", "调整到", "顺延");
    /** 查询类关键词 */
    private static final List<String> QUERY_WORDS =
            Arrays.asList("有什么", "有啥", "有哪些", "查一下", "查查", "查询", "查看", "看看", "看一下",
                    "安排", "日程", "什么安排", "有没有", "几点");
    /** 用于截取标题的常见事件类型词 */
    private static final List<String> EVENT_TYPE_WORDS =
            Arrays.asList("会议", "例会", "周会", "晨会", "面试", "培训", "约会", "聚餐", "聚会", "生日",
                    "课程", "上课", "考试", "体检", "看病", "出差", "旅行", "电话", "约见", "拜访", "活动", "演出", "比赛");

    /** 中文数字 -> 阿拉伯数字 */
    private static final Map<Character, Integer> CN_NUM = new LinkedHashMap<>();

    static {
        CN_NUM.put('零', 0);
        CN_NUM.put('〇', 0);
        CN_NUM.put('一', 1);
        CN_NUM.put('两', 2);
        CN_NUM.put('二', 2);
        CN_NUM.put('三', 3);
        CN_NUM.put('四', 4);
        CN_NUM.put('五', 5);
        CN_NUM.put('六', 6);
        CN_NUM.put('七', 7);
        CN_NUM.put('八', 8);
        CN_NUM.put('九', 9);
        CN_NUM.put('十', 10);
    }

    /**
     * 解析入口。
     *
     * @param text    自然语言原文
     * @param baseNow 解析相对时间的基准「现在」
     * @return 结构化结果（含置信度）
     */
    public VoiceParseResult parse(String text, LocalDateTime baseNow) {
        VoiceParseResult result = new VoiceParseResult();
        Map<String, Double> confidence = new LinkedHashMap<>();
        result.setRawText(text);
        result.setConfidence(confidence);

        String normalized = text == null ? "" : text.trim();
        String intent = detectIntent(normalized);
        result.setIntent(intent);
        confidence.put("intent", intentConfidence(normalized, intent));

        switch (intent) {
            case INTENT_CREATE:
                fillCreate(normalized, baseNow, result, confidence);
                break;
            case INTENT_QUERY:
                fillQuery(normalized, baseNow, result, confidence);
                break;
            case INTENT_UPDATE:
            case INTENT_DELETE:
                fillUpdateOrDelete(normalized, baseNow, result, confidence);
                break;
            default:
                // unknown：仅返回 intent 与 rawText
                break;
        }
        return result;
    }

    // ---------------------------------------------------------------------
    // 意图识别
    // ---------------------------------------------------------------------

    private String detectIntent(String text) {
        if (containsAny(text, DELETE_WORDS)) {
            return INTENT_DELETE;
        }
        if (containsAny(text, UPDATE_WORDS)) {
            return INTENT_UPDATE;
        }
        // 疑问/查询语气优先于创建（避免「明天有什么安排」被当成创建）
        if (containsAny(text, QUERY_WORDS) || text.contains("吗") || text.contains("？") || text.contains("?")) {
            return INTENT_QUERY;
        }
        // 含时间或创建类动词，判为创建
        if (hasTimeSignal(text) || containsAny(text, EVENT_TYPE_WORDS)
                || text.contains("提醒") || text.contains("安排") || text.contains("有个") || text.contains("约")) {
            return INTENT_CREATE;
        }
        return INTENT_UNKNOWN;
    }

    private double intentConfidence(String text, String intent) {
        if (INTENT_UNKNOWN.equals(intent)) {
            return 0.3;
        }
        // 有明确动词关键词时给高置信度
        if (INTENT_DELETE.equals(intent) || INTENT_UPDATE.equals(intent)) {
            return 0.95;
        }
        if (INTENT_QUERY.equals(intent)) {
            return 0.9;
        }
        return hasTimeSignal(text) ? 0.92 : 0.75;
    }

    // ---------------------------------------------------------------------
    // create
    // ---------------------------------------------------------------------

    private void fillCreate(String text, LocalDateTime baseNow,
                            VoiceParseResult result, Map<String, Double> confidence) {
        LocalDate date = parseDate(text, baseNow.toLocalDate());
        boolean dateExplicit = hasDateSignal(text);

        boolean allDay = text.contains("全天") || text.contains("一整天") || text.contains("整天");
        int[] hourRange = parseTimeRange(text);

        if (allDay || hourRange == null) {
            result.setAllDay(Boolean.TRUE);
            result.setStartTime(date.atTime(LocalTime.of(0, 0, 0)));
            result.setEndTime(date.atTime(LocalTime.of(23, 59, 59)));
            confidence.put("startTime", dateExplicit ? 0.85 : 0.5);
        } else {
            result.setAllDay(Boolean.FALSE);
            LocalTime start = LocalTime.of(hourRange[0], hourRange[1], 0);
            result.setStartTime(date.atTime(start));
            if (hourRange[2] >= 0) {
                LocalTime end = LocalTime.of(hourRange[2], hourRange[3], 0);
                LocalDateTime endDt = date.atTime(end);
                // 结束早于开始视为跨日
                if (endDt.isBefore(result.getStartTime())) {
                    endDt = endDt.plusDays(1);
                }
                result.setEndTime(endDt);
            } else {
                // 仅给出开始时间，默认时长 1 小时
                result.setEndTime(result.getStartTime().plusHours(1));
            }
            confidence.put("startTime", dateExplicit ? 0.95 : 0.8);
        }

        String title = extractTitle(text);
        result.setTitle(title);
        confidence.put("title", title != null && !title.isEmpty() ? 0.9 : 0.4);

        String location = extractLocation(text);
        result.setLocation(location);
        confidence.put("location", location != null ? 0.85 : 0.4);

        List<Integer> offsets = parseReminderOffsets(text);
        if (!offsets.isEmpty()) {
            result.setReminderOffsets(offsets);
        }

        result.setRecurrence(parseRecurrence(text));
    }

    // ---------------------------------------------------------------------
    // query
    // ---------------------------------------------------------------------

    private void fillQuery(String text, LocalDateTime baseNow,
                           VoiceParseResult result, Map<String, Double> confidence) {
        LocalDate today = baseNow.toLocalDate();

        // 周/月范围优先
        if (text.contains("这周") || text.contains("本周") || text.contains("这星期") || text.contains("本星期")) {
            LocalDate monday = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
            result.setQueryStart(monday.atTime(LocalTime.MIN));
            result.setQueryEnd(monday.plusDays(6).atTime(LocalTime.of(23, 59, 59)));
        } else if (text.contains("这个月") || text.contains("本月")) {
            LocalDate first = today.withDayOfMonth(1);
            result.setQueryStart(first.atTime(LocalTime.MIN));
            result.setQueryEnd(first.plusMonths(1).minusDays(1).atTime(LocalTime.of(23, 59, 59)));
        } else {
            LocalDate date = parseDate(text, today);
            result.setQueryStart(date.atTime(LocalTime.MIN));
            result.setQueryEnd(date.atTime(LocalTime.of(23, 59, 59)));
        }
    }

    // ---------------------------------------------------------------------
    // update / delete
    // ---------------------------------------------------------------------

    private void fillUpdateOrDelete(String text, LocalDateTime baseNow,
                                    VoiceParseResult result, Map<String, Double> confidence) {
        String keyword = extractMatchKeyword(text);
        result.setMatchKeyword(keyword);
        if (keyword != null && !keyword.isEmpty()) {
            confidence.put("matchKeyword", 0.85);
        }

        // 修改时若包含新时间，则解析为 startTime（前端据此生成 patch）
        if (INTENT_UPDATE.equals(result.getIntent())) {
            int[] hourRange = parseTimeRange(text);
            if (hourRange != null) {
                LocalDate date = parseDate(text, baseNow.toLocalDate());
                LocalTime start = LocalTime.of(hourRange[0], hourRange[1], 0);
                result.setStartTime(date.atTime(start));
                confidence.put("startTime", 0.85);
                if (hourRange[2] >= 0) {
                    result.setEndTime(date.atTime(LocalTime.of(hourRange[2], hourRange[3], 0)));
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // 日期解析
    // ---------------------------------------------------------------------

    private boolean hasDateSignal(String text) {
        return Pattern.compile("今天|今日|明天|明日|后天|大后天|昨天|前天|"
                        + "周[一二三四五六日天]|星期[一二三四五六日天]|礼拜[一二三四五六日天]|"
                        + "下周|下星期|这周|本周|\\d{1,2}月\\d{1,2}[日号]")
                .matcher(text).find();
    }

    private boolean hasTimeSignal(String text) {
        return hasDateSignal(text) || Pattern.compile(
                        "上午|下午|中午|早上|晚上|凌晨|傍晚|\\d{1,2}[点:：]|[零一两二三四五六七八九十]+点")
                .matcher(text).find();
    }

    private LocalDate parseDate(String text, LocalDate today) {
        // 绝对日期 X月Y日
        Matcher md = Pattern.compile("(\\d{1,2})月(\\d{1,2})[日号]").matcher(text);
        if (md.find()) {
            int month = Integer.parseInt(md.group(1));
            int day = Integer.parseInt(md.group(2));
            LocalDate d = LocalDate.of(today.getYear(), month, day);
            if (d.isBefore(today)) {
                d = d.plusYears(1);
            }
            return d;
        }
        if (text.contains("大后天")) {
            return today.plusDays(3);
        }
        if (text.contains("后天")) {
            return today.plusDays(2);
        }
        if (text.contains("明天") || text.contains("明日")) {
            return today.plusDays(1);
        }
        if (text.contains("前天")) {
            return today.minusDays(2);
        }
        if (text.contains("昨天")) {
            return today.minusDays(1);
        }
        // 周X / 星期X / 礼拜X
        Matcher wd = Pattern.compile("(下{0,1})(周|星期|礼拜)([一二三四五六日天])").matcher(text);
        if (wd.find()) {
            boolean next = !wd.group(1).isEmpty();
            int target = weekdayValue(wd.group(3).charAt(0));
            int current = today.getDayOfWeek().getValue();
            int diff = target - current;
            if (diff < 0 || (diff == 0 && next)) {
                diff += 7;
            }
            if (next) {
                diff += 7;
            }
            return today.plusDays(diff);
        }
        // 今天/今日 或 缺省
        return today;
    }

    private int weekdayValue(char c) {
        switch (c) {
            case '一':
                return 1;
            case '二':
                return 2;
            case '三':
                return 3;
            case '四':
                return 4;
            case '五':
                return 5;
            case '六':
                return 6;
            case '日':
            case '天':
                return 7;
            default:
                return 1;
        }
    }

    // ---------------------------------------------------------------------
    // 时间解析
    // ---------------------------------------------------------------------

    /**
     * 解析时间段。
     *
     * @return [startHour, startMinute, endHour, endMinute]，无结束时间时 endHour=-1；无任何时间返回 null
     */
    private int[] parseTimeRange(String text) {
        // 上午/下午等修饰会影响小时换算，按位置就近处理
        List<int[]> times = new ArrayList<>();

        // 1) 数字 + 点/: 形式：3点 / 3点半 / 15:30 / 3点20分
        Matcher m = Pattern.compile(
                        "(上午|下午|中午|早上|晚上|凌晨|傍晚)?\\s*(\\d{1,2})\\s*[点:：]\\s*(半|[0-5]?\\d)?\\s*分?")
                .matcher(text);
        while (m.find()) {
            int hour = Integer.parseInt(m.group(2));
            int minute = parseMinute(m.group(3));
            hour = applyMeridiem(m.group(1), hour, text, m.start());
            times.add(new int[]{hour, minute});
        }

        // 2) 中文数字 + 点 形式：三点 / 三点半
        Matcher cm = Pattern.compile(
                        "(上午|下午|中午|早上|晚上|凌晨|傍晚)?\\s*([零一两二三四五六七八九十]+)\\s*点\\s*(半|[零一两二三四五六七八九十]+)?\\s*分?")
                .matcher(text);
        while (cm.find()) {
            int hour = cnToInt(cm.group(2));
            int minute = parseChineseMinute(cm.group(3));
            hour = applyMeridiem(cm.group(1), hour, text, cm.start());
            times.add(new int[]{hour, minute});
        }

        if (times.isEmpty()) {
            return null;
        }
        int[] start = times.get(0);
        int[] end = times.size() > 1 ? times.get(1) : null;
        return new int[]{start[0], start[1], end == null ? -1 : end[0], end == null ? -1 : end[1]};
    }

    private int parseMinute(String g) {
        if (g == null || g.isEmpty()) {
            return 0;
        }
        if ("半".equals(g)) {
            return 30;
        }
        return Integer.parseInt(g);
    }

    private int parseChineseMinute(String g) {
        if (g == null || g.isEmpty()) {
            return 0;
        }
        if ("半".equals(g)) {
            return 30;
        }
        return cnToInt(g);
    }

    /**
     * 根据上午/下午等修饰词换算 24 小时制。若无显式修饰词，则尝试就近向前查找一个修饰词。
     */
    private int applyMeridiem(String meridiem, int hour, String text, int matchStart) {
        String mer = meridiem;
        if (mer == null || mer.isEmpty()) {
            mer = nearestMeridiemBefore(text, matchStart);
        }
        if (mer == null) {
            return hour;
        }
        switch (mer) {
            case "下午":
            case "晚上":
            case "傍晚":
                return hour < 12 ? hour + 12 : hour;
            case "中午":
                return hour == 12 ? 12 : (hour < 12 ? hour + 12 : hour);
            case "凌晨":
            case "早上":
            case "上午":
            default:
                return hour == 24 ? 0 : hour;
        }
    }

    private String nearestMeridiemBefore(String text, int idx) {
        String prefix = text.substring(0, Math.min(idx, text.length()));
        String[] words = {"凌晨", "早上", "上午", "中午", "下午", "傍晚", "晚上"};
        String found = null;
        int foundPos = -1;
        for (String w : words) {
            int p = prefix.lastIndexOf(w);
            if (p > foundPos) {
                foundPos = p;
                found = w;
            }
        }
        return found;
    }

    private int cnToInt(String cn) {
        if (cn == null || cn.isEmpty()) {
            return 0;
        }
        // 处理 十/十X/X十/X十Y
        if (cn.contains("十")) {
            int idx = cn.indexOf('十');
            int tens = idx == 0 ? 1 : CN_NUM.getOrDefault(cn.charAt(idx - 1), 0);
            int ones = idx == cn.length() - 1 ? 0 : CN_NUM.getOrDefault(cn.charAt(cn.length() - 1), 0);
            return tens * 10 + ones;
        }
        int value = 0;
        for (char c : cn.toCharArray()) {
            Integer d = CN_NUM.get(c);
            if (d != null) {
                value = value * 10 + d;
            }
        }
        return value;
    }

    // ---------------------------------------------------------------------
    // 提醒提前量
    // ---------------------------------------------------------------------

    private List<Integer> parseReminderOffsets(String text) {
        List<Integer> offsets = new ArrayList<>();
        // 提前X分钟/小时/天（X 可为阿拉伯或中文数字，支持「半小时」）
        Matcher m = Pattern.compile("提前\\s*(半|\\d+|[零一两二三四五六七八九十]+)\\s*(分钟|分|小时|个小时|钟头|天|日)")
                .matcher(text);
        while (m.find()) {
            String numStr = m.group(1);
            String unit = m.group(2);
            int num;
            if ("半".equals(numStr)) {
                num = 1; // 半小时 -> 0.5 小时，下方单独处理
            } else if (numStr.matches("\\d+")) {
                num = Integer.parseInt(numStr);
            } else {
                num = cnToInt(numStr);
            }
            int minutes;
            if (unit.contains("小时") || unit.contains("钟头")) {
                minutes = "半".equals(numStr) ? 30 : num * 60;
            } else if (unit.contains("天") || unit.contains("日")) {
                minutes = num * 24 * 60;
            } else {
                minutes = "半".equals(numStr) ? 30 : num;
            }
            if (!offsets.contains(minutes)) {
                offsets.add(minutes);
            }
        }
        return offsets;
    }

    // ---------------------------------------------------------------------
    // 地点
    // ---------------------------------------------------------------------

    private String extractLocation(String text) {
        // 地点在X / 位置在X / 在X会议室|室|楼|号楼|公司|办公室|餐厅
        Matcher m1 = Pattern.compile("(?:地点|位置|地址)\\s*(?:在|是|为|:|：)?\\s*([\\u4e00-\\u9fa5A-Za-z0-9]+(?:室|厅|楼|馆|店|区|号楼|会议室|办公室|公司|餐厅)?)")
                .matcher(text);
        if (m1.find()) {
            return trimLocation(m1.group(1));
        }
        Matcher m2 = Pattern.compile("在\\s*([\\u4e00-\\u9fa5A-Za-z0-9]{1,12}?(?:会议室|办公室|餐厅|教室|实验室|号楼|大厦|广场|公园|酒店|宾馆|公司|楼|馆|店))")
                .matcher(text);
        if (m2.find()) {
            return trimLocation(m2.group(1));
        }
        return null;
    }

    private String trimLocation(String loc) {
        if (loc == null) {
            return null;
        }
        String trimmed = loc.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // ---------------------------------------------------------------------
    // 重复规则
    // ---------------------------------------------------------------------

    private String parseRecurrence(String text) {
        if (text.contains("每天") || text.contains("每日")) {
            return "daily";
        }
        if (text.contains("每周") || text.contains("每星期") || text.contains("每个星期")) {
            return "weekly";
        }
        if (text.contains("每月") || text.contains("每个月")) {
            return "monthly";
        }
        return "none";
    }

    // ---------------------------------------------------------------------
    // 标题 / 关键词
    // ---------------------------------------------------------------------

    private String extractTitle(String text) {
        // 优先匹配「有个X」「有一个X」「有场X」结构
        Matcher m = Pattern.compile("有(?:一)?(?:个|场|节|堂)([\\u4e00-\\u9fa5A-Za-z0-9]{1,10})").matcher(text);
        if (m.find()) {
            String t = stripTrailingVerb(m.group(1));
            if (!t.isEmpty()) {
                return t;
            }
        }
        // 其次匹配已知事件类型词
        for (String w : EVENT_TYPE_WORDS) {
            if (text.contains(w)) {
                return w;
            }
        }
        // 「提醒我X」结构
        Matcher r = Pattern.compile("提醒我\\s*([\\u4e00-\\u9fa5A-Za-z0-9]{1,12})").matcher(text);
        if (r.find()) {
            return stripTrailingVerb(r.group(1));
        }
        return null;
    }

    private String stripTrailingVerb(String s) {
        // 去掉常见尾随动词，避免把「会议地点」之类多余字带入
        String t = s.replaceAll("(地点|时间|位置)$", "");
        return t.trim();
    }

    private String extractMatchKeyword(String text) {
        // 已知事件词优先
        for (String w : EVENT_TYPE_WORDS) {
            if (text.contains(w)) {
                return w;
            }
        }
        // 「把X改到/删除」结构
        Matcher m = Pattern.compile("把\\s*([\\u4e00-\\u9fa5A-Za-z0-9]{1,10}?)(?:改|删|取消|挪|换|推迟|顺延|调整)")
                .matcher(text);
        if (m.find()) {
            String kw = cleanKeyword(m.group(1));
            if (!kw.isEmpty()) {
                return kw;
            }
        }
        // 「删除X」「取消X」结构
        Matcher d = Pattern.compile("(?:删除|删掉|取消|撤销)\\s*([\\u4e00-\\u9fa5A-Za-z0-9]{1,10})")
                .matcher(text);
        if (d.find()) {
            String kw = cleanKeyword(d.group(1));
            if (!kw.isEmpty()) {
                return kw;
            }
        }
        return null;
    }

    private String cleanKeyword(String kw) {
        if (kw == null) {
            return "";
        }
        // 去掉时间相关词，保留事件名核心
        return kw.replaceAll("今天|明天|后天|昨天|的|那个|这个", "").trim();
    }

    private boolean containsAny(String text, List<String> words) {
        for (String w : words) {
            if (text.contains(w)) {
                return true;
            }
        }
        return false;
    }
}
