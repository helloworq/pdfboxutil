package tool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import util.Strings;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PdfUtil {
    private static final String CHARS = "qwertyuioplkjhgfdsazxcvbnm1234567890";
    public static final String COLOR_TAG_START = "<cr>";
    public static final String COLOR_TAG_END = "</cr>";
    public static final Float STROKING_WIDTH = 1.0f;
    public static final Float TABLE_TEXT_PADDING = 5.0f;
    public static final String WATER_MARK_TEXT = "SAMPLE";
    public static final Float GLOBAL_FONT_SIZE = 16f;
    public static final Float GLOBAL_PADDING = 20f;
    public static final Integer DEFAULT_CELL_HEIGHT = 20;
    public static final Color NORMAL_TEXT_COLOR = new Color(85, 85, 85);

    /**
     * 自定义table
     *
     * @param contentStream
     * @param font
     * @param xSplit        x上即纵向切分数量
     * @param ySplit        y上即横向切分数量
     * @param colorRows     定义着色行
     * @param colorColumns  定义着色列
     * @param rowData       行数据
     * @throws IOException
     */
    public static float customTable(PDPageContentStream contentStream, PDFont font
            , List<Integer> xSplit, List<Integer> ySplit
            , List<Integer> colorRows, List<Integer> colorColumns
            , List<List<String>> rowData
    ) throws IOException {
        Integer yHeightMin = ySplit.get(0);
        Integer yHeightMax = ySplit.stream().reduce(Integer::sum).get();

        Integer xWidthMin = xSplit.get(0);
        Integer xWidthMax = xSplit.stream().reduce(Integer::sum).get();

        if (!CollectionUtils.isEmpty(colorRows)) {
            for (int row : colorRows) {
                contentStream.setNonStrokingColor(new Color(244, 244, 244));//gray
                contentStream.addRect(
                        xWidthMin + STROKING_WIDTH
                        , ySplit.stream().limit(row).reduce(Integer::sum).get() + STROKING_WIDTH
                        , xWidthMax - xWidthMin - STROKING_WIDTH
                        , ySplit.get(row) - STROKING_WIDTH);
                contentStream.fill();
            }
        }

        if (!CollectionUtils.isEmpty(colorColumns)) {
            for (int column : colorColumns) {
                contentStream.setNonStrokingColor(new Color(244, 244, 244));//gray
                contentStream.addRect(
                        xSplit.stream().limit(column).reduce(Integer::sum).get() + STROKING_WIDTH
                        , yHeightMin + STROKING_WIDTH
                        , xSplit.get(column) - STROKING_WIDTH
                        , yHeightMax - yHeightMin - STROKING_WIDTH);
                contentStream.fill();
            }
        }

        //drawGrid
        xSplit.stream().reduce((sum, cur) -> {
            darwLine(contentStream, sum, yHeightMin, sum, yHeightMax);
            darwLine(contentStream, sum + cur, yHeightMin, sum + cur, yHeightMax);
            return sum + cur;
        });

        ySplit.stream().reduce((sum, cur) -> {
            darwLine(contentStream, xWidthMin, sum, xWidthMax, sum);
            darwLine(contentStream, xWidthMin, sum + cur, xWidthMax, sum + cur);
            return sum + cur;
        });

        //drawText
        for (int i = 1; i < ySplit.size(); i++) {
            for (int j = 1; j < xSplit.size(); j++) {
                if (!CollectionUtils.isEmpty(rowData)) {
                    int sum = 0;
                    int newLineHeight = 1;
                    String data = rowData.get(i - 1).get(j - 1);

                    List<String> colorStringList = new LinkedList<>();
                    int circleTimes = (int) Math.ceil(getStringWidth(data, font, GLOBAL_FONT_SIZE) / (xSplit.get(j).floatValue() - GLOBAL_PADDING));
                    for (int k = 0; k < circleTimes; k++) {
                        int subStringEndPosition = k == (circleTimes - 1)
                                ? data.length()
                                : getSubStringNextPositionByWidth(sum, (xSplit.get(j).floatValue() - GLOBAL_PADDING)
                                , data, font, GLOBAL_FONT_SIZE);
                        String temp = data.substring(sum, subStringEndPosition);
                        //位置坐标转置，以将符合直觉的数据写入表格
                        colorStringList.add(temp);
                        sum = subStringEndPosition;
                    }
                    //补全颜色tag
                    List<String> res = fillColorTag(colorStringList);
                    for (String re : res) {
                        drawColorText(contentStream, re, xSplit.stream().limit(j).reduce(Integer::sum).get() + TABLE_TEXT_PADDING
                                , ySplit.stream().limit(i).reduce(Integer::sum).get() - 2 * TABLE_TEXT_PADDING - newLineHeight * 20f
                                , font, GLOBAL_FONT_SIZE, new Color(142, 142, 142));
                        newLineHeight++;
                    }
                }
            }
        }
        return yHeightMax.floatValue() - yHeightMin.floatValue();
    }

    //根据已切分的带颜色标签的字符补全tag
    public static List<String> fillColorTag(List<String> colorList) {
        if (!CollectionUtils.isEmpty(colorList) && colorList.size() != 1) {
            for (int i = 0; i < colorList.size() - 1; i++) {
                String current = colorList.get(i);
                if (!current.endsWith(COLOR_TAG_END)) {
                    //倒序搜索最近一个颜色标签,确保start标签是最后一个
                    int startTagIndex = current.lastIndexOf(COLOR_TAG_START);
                    int endTagIndex = current.lastIndexOf(COLOR_TAG_END);
                    if ((startTagIndex != -1) && (startTagIndex > endTagIndex)) {
                        colorList.set(i, current + COLOR_TAG_END);
                        if (!colorList.get(i + 1).startsWith(COLOR_TAG_START) && !colorList.get(i + 1).startsWith(COLOR_TAG_END)) {
                            colorList.set(i + 1, COLOR_TAG_START + colorList.get(i + 1));
                        }
                    }
                }
            }
        }
        return colorList;
    }

    public static void drawBlueText(PDPageContentStream contentStream, String text, float x, float y, PDFont font, float fontSize) throws IOException {
        drawColorText(contentStream, text, x, y, font, fontSize, new Color(0, 128, 255));
    }

    public static void drawGrayText(PDPageContentStream contentStream, String text, float x, float y, PDFont font, float fontSize) throws IOException {
        drawColorText(contentStream, text, x, y, font, fontSize, new Color(142, 142, 142));
    }

    public static void drawColorText(PDPageContentStream contentStream, String text, float x, float y, PDFont font, float fontSize, Color color) throws IOException {
        int start = 0;
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x + TABLE_TEXT_PADDING, y + TABLE_TEXT_PADDING);
        if (text.indexOf(COLOR_TAG_START, start) != -1) {
            while (text.indexOf(COLOR_TAG_START, start) != -1) {
                int findPosition = text.indexOf(COLOR_TAG_START, start);
                contentStream.setStrokingColor(NORMAL_TEXT_COLOR);
                contentStream.showText(text.substring(start, findPosition));

                start = findPosition + COLOR_TAG_START.length();
                int end = text.indexOf(COLOR_TAG_END, start);
                contentStream.setStrokingColor(color);
                contentStream.showText(text.substring(start, end).replace(COLOR_TAG_START, Strings.EMPTY).replace(COLOR_TAG_END, Strings.EMPTY));
                start = end + COLOR_TAG_END.length();

                contentStream.setStrokingColor(NORMAL_TEXT_COLOR);
                if (text.indexOf(COLOR_TAG_START, start) == -1) {
                    //contentStream.setCharacterSpacing(0.7f);
                    contentStream.showText(text.substring(start).replace(COLOR_TAG_START, Strings.EMPTY).replace(COLOR_TAG_END, Strings.EMPTY));
                }
            }
        } else {
            contentStream.setStrokingColor(NORMAL_TEXT_COLOR);
            contentStream.showText(text);
        }

        contentStream.endText();
    }

    //从字符串指定位置开始获取限制长度的字符串
    public static int getSubStringNextPositionByWidth(int start, float limitWidth, String text, PDFont font, float fontSize) {
        AtomicInteger result = new AtomicInteger(start);
        AtomicReference<String> total = new AtomicReference<>(Strings.EMPTY);
        AtomicInteger count = new AtomicInteger(0);
        Arrays.stream(text.split(Strings.EMPTY)).skip(start).forEach(curChar -> {
            if (count.get() > 0) {
                count.decrementAndGet();
            } else {
                if (curChar.equals("<")) {
                    if (text.startsWith(COLOR_TAG_START, result.get())) {
                        count.set(COLOR_TAG_START.length() - 1);
                        total.set(total.get() + COLOR_TAG_START);
                        result.set(result.get() + COLOR_TAG_START.length());
                    } else if (text.startsWith(COLOR_TAG_END, result.get())) {
                        count.set(COLOR_TAG_END.length() - 1);
                        total.set(total.get() + COLOR_TAG_END);
                        result.set(result.get() + COLOR_TAG_END.length());
                    }
                } else {
                    total.set(total.get() + curChar);
                    if ((getStringWidth(total.get(), font, fontSize)) < limitWidth) {
                        result.getAndIncrement();
                    }
                }
            }
        });
        return result.get();
    }

    public static List<Integer> calculateRowHeight(int startY, List<List<String>> rowData, PDFont font, List<Integer> split) {
        AtomicInteger order = new AtomicInteger(1);
        //计算结果基于已给定的cell宽度和字符尺寸
        List<Integer> res = new LinkedList<>();
        res.add(startY);
        for (int i = 0; i < rowData.size(); i++) {
            int height = rowData.get(i).stream()
                    .map(data -> (int) Math.ceil(PdfUtil.getStringWidth(data, font, GLOBAL_FONT_SIZE)
                            / (split.get(order.getAndIncrement()) - GLOBAL_PADDING)))
                    .max(Comparator.comparing(Integer::intValue))
                    .map(data -> (DEFAULT_CELL_HEIGHT * (data - 1)) + (int) (GLOBAL_PADDING * 2))
                    .get();
            res.add(-height);
            order.set(1);
        }
        return res;
    }

    public static float getStringWidth(String text, PDFont font, float fontSize) {
        try {
            text = text.replace(COLOR_TAG_START, Strings.EMPTY)
                    .replace(COLOR_TAG_END, Strings.EMPTY);
            return font.getStringWidth(text) / 1000 * fontSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void darwLine(PDPageContentStream contentStream, float xStart, float yStart, float xEnd, float yEnd) {
        try {
            contentStream.moveTo(xStart, yStart);
            contentStream.setStrokingColor(new Color(221, 221, 221));
            contentStream.lineTo(xEnd, yEnd);
            contentStream.stroke();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawText(PDPageContentStream contentStream, String text, float x, float y, PDFont font, float fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setStrokingColor(new Color(85, 85, 85));
        contentStream.setFont(font, fontSize);
        //contentStream.setRenderingMode(RenderingMode.FILL);
        contentStream.setCharacterSpacing(0.6f);
        contentStream.newLineAtOffset(x + TABLE_TEXT_PADDING, y + TABLE_TEXT_PADDING);
        contentStream.showText(text);
        contentStream.endText();
    }

    public static void drawText(PDPageContentStream contentStream, String text, float x, float y, PDFont font, float fontSize, boolean bold) throws IOException {
        contentStream.beginText();
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setFont(font, fontSize);
        contentStream.setRenderingMode(RenderingMode.FILL_STROKE);
        contentStream.newLineAtOffset(x + TABLE_TEXT_PADDING, y + TABLE_TEXT_PADDING);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.setRenderingMode(RenderingMode.STROKE);
    }


    public static <T> List<T> listOf(T... t) {
        return new ArrayList<>(Arrays.asList(t));
    }
}
