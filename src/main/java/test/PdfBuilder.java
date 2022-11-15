package test;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * create pdf with builder model
 */
public class PdfBuilder {
    private final PDDocument document;
    private PDPageContentStream contentStream;
    public static final PDRectangle PAPER = PDRectangle.A3;
    public static PDFont FONT = PDType1Font.TIMES_ROMAN;
    public static final Float GLOBAL_FONT_SIZE = 16f;
    public static final Float INIT_X = 50.0f;
    public static final Float INIT_Y = PAPER.getHeight() - 70.0f;//pdfbox默认第一象限
    public static final Float BOTTOM_HEIGHT = 80f;
    public Float GLOBAL_X = INIT_X;
    public Float GLOBAL_Y = INIT_Y;
    public static final String COLOR_TAG_START = "<cr>";
    public static final String COLOR_TAG_END = "</cr>";
    public static final String BASE_PATH = System.getProperty("user.dir") + File.separator;
    public static final String SAVE_PATH = BASE_PATH + +System.currentTimeMillis() + ".pdf";
    public static final Float GLOBAL_PADDING = 20f;
    public static final Float TABLE_TEXT_PADDING = 5.0f;
    public static final Float STROKING_WIDTH = 1.0f;

    public PdfBuilder() throws IOException {
        this.document = new PDDocument();
        PDPage firstPage = new PDPage(PAPER);
        this.document.addPage(firstPage);
        this.contentStream = new PDPageContentStream(document, firstPage);
        try (InputStream fontStream = Files.newInputStream(Paths.get(BASE_PATH + "font/SourceHanSansCN-VF.ttf"))) {
            FONT = PDType0Font.load(document, fontStream);
        }
    }

    public static PdfBuilder builder() throws IOException {
        return new PdfBuilder();
    }

    public PdfBuilder line(float xStart, float yStart, float xEnd, float yEnd) {
        PdfUtil.darwLine(contentStream, GLOBAL_X + xStart, GLOBAL_Y + yStart, GLOBAL_X + xEnd, GLOBAL_Y + yEnd);
        return this;
    }

    public PdfBuilder row() throws IOException {
        //判断当前高度是否超过最大高度，超过则需要新建一页
        if (GLOBAL_Y <= BOTTOM_HEIGHT) {
            PDPage nextPage = new PDPage(PAPER);
            this.document.addPage(nextPage);
            this.contentStream.close();
            this.contentStream = new PDPageContentStream(document, nextPage);
            contentStream.setRenderingMode(RenderingMode.FILL_STROKE);
            GLOBAL_Y = INIT_Y;
        } else {
            GLOBAL_X = INIT_X;
            GLOBAL_Y = GLOBAL_Y - 30f;
        }
        return this;
    }

    public PdfBuilder grayText(String text) throws IOException {
        PdfUtil.drawGrayText(contentStream, text, GLOBAL_X, GLOBAL_Y, FONT, GLOBAL_FONT_SIZE);
        return this;
    }

    public PdfBuilder blueText(String text) throws IOException {
        PdfUtil.drawBlueText(contentStream, text, GLOBAL_X, GLOBAL_Y, FONT, GLOBAL_FONT_SIZE);
        return this;
    }

    public PdfBuilder text(String text) throws IOException {
        PdfUtil.drawText(contentStream, text, GLOBAL_X, GLOBAL_Y, FONT, GLOBAL_FONT_SIZE);
        return this;
    }

    public PdfBuilder boldText(String text) throws IOException {
        PdfUtil.drawText(contentStream, text, GLOBAL_X, GLOBAL_Y, FONT, GLOBAL_FONT_SIZE, true);
        return this;
    }

    public PdfBuilder title(String text) throws IOException {
        contentStream.beginText();
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setRenderingMode(RenderingMode.FILL_STROKE);
        contentStream.setFont(FONT, 22f);
        contentStream.newLineAtOffset(GLOBAL_X, GLOBAL_Y);//手动居中
        contentStream.showText(text);
        contentStream.endText();
        return this;
    }

    public PdfBuilder subTitle() {
        return this;
    }

    public PdfBuilder table(List<Integer> widths, List<List<String>> rowData, List<Integer> colorRows, List<Integer> colorColumns) throws IOException {
        widths.add(0, GLOBAL_X.intValue());
        float height = PdfUtil.customTable(contentStream, FONT,
                widths,
                PdfUtil.calculateRowHeight(GLOBAL_Y.intValue(), rowData, FONT, widths),
                colorRows,
                colorColumns, rowData);
        GLOBAL_Y = GLOBAL_Y + height;
        return this;
    }

    public PdfBuilder moveX(Float offset) {
        GLOBAL_X = GLOBAL_X + offset;
        return this;
    }

    public PdfBuilder moveY(Float offset) {
        GLOBAL_Y = GLOBAL_Y + offset;
        return this;
    }

    public void create() throws IOException {
        this.contentStream.close();
        this.document.save(SAVE_PATH);
        this.document.close();
    }

    private static String color(String text) {
        return StringUtils.isNotEmpty(text) ? (COLOR_TAG_START + text + COLOR_TAG_END) : text;
    }

    /**
     * 更强的table
     *
     * @param widths       指定table宽度，每一列都需要指定，行高不需要指定，会自适应
     * @param data         需要渲染的数据
     * @param colorRows    需要着色的行
     * @param colorColumns 需要着色的列
     * @return
     * @throws IOException
     */
    public PdfBuilder powerTable(
            List<Integer> widths
            , List<List<String>> data
            , List<Integer> colorRows, List<Integer> colorColumns
    ) throws IOException {

        widths.add(0, GLOBAL_X.intValue());
        List<Integer> heights = PdfUtil.calculateRowHeight(GLOBAL_Y.intValue(), data, FONT, widths);
        Integer xWidthMin = widths.get(0);
        Integer xWidthMax = widths.stream().reduce(Integer::sum).get();

        int heightPrev = 0;
        int height = 0;
        for (int a = 0; a < heights.size() - 1; a++) {
            heightPrev += heights.get(a);
            //判断是否要新开一页
            if (heightPrev <= BOTTOM_HEIGHT) {
                PDPage nextPage = new PDPage(PAPER);
                document.addPage(nextPage);
                contentStream.close();
                contentStream = new PDPageContentStream(document, nextPage);
                heightPrev = INIT_Y.intValue();
                contentStream.setRenderingMode(RenderingMode.FILL_STROKE);
            }
            height = heightPrev + heights.get(a + 1);
            PdfUtil.darwLine(contentStream, xWidthMin, heightPrev, xWidthMax, heightPrev);//画横线
            PdfUtil.darwLine(contentStream, xWidthMin, height, xWidthMax, height);
            int width = 0;
            for (int i = 0; i < widths.size(); i++) {
                //PdfUtil.darwLine(contentStream, width, heightPrev, width, height);//画竖线
                width += widths.get(i);
                PdfUtil.darwLine(contentStream, width, heightPrev, width, height);
                //准备写入数据
                if (i < widths.size() - 1) {
                    if (colorRows.contains(a) || colorColumns.contains(i)) {
                        contentStream.setNonStrokingColor(new Color(244, 244, 244));//gray
                        contentStream.addRect(
                                width + STROKING_WIDTH
                                , heightPrev - STROKING_WIDTH
                                , widths.get(i + 1) - STROKING_WIDTH
                                , height - heightPrev + 2 * STROKING_WIDTH);
                        contentStream.fill();
                    }
                    contentStream.setNonStrokingColor(Color.BLACK);

                    int sum = 0;
                    int newLineHeight = 1;
                    String input = data.get(a).get(i);
                    List<String> colorStringList = new LinkedList<>();

                    int circleTimes = (int) Math.ceil(PdfUtil.getStringWidth(input, FONT, GLOBAL_FONT_SIZE) / (widths.get(i + 1) - GLOBAL_PADDING.intValue()));
                    for (int k = 0; k < circleTimes; k++) {
                        int subStringEndPosition = k == (circleTimes - 1)
                                ? input.length()
                                : PdfUtil.getSubStringNextPositionByWidth(sum, (widths.get(i + 1) - GLOBAL_PADDING)
                                , input, FONT, GLOBAL_FONT_SIZE);
                        String temp = input.substring(sum, subStringEndPosition);
                        //位置坐标转置，以将符合直觉的数据写入表格
                        colorStringList.add(temp);
                        sum = subStringEndPosition;
                    }
                    //补全颜色tag
                    List<String> res = PdfUtil.fillColorTag(colorStringList);
                    for (String re : res) {
                        PdfUtil.drawColorText(contentStream, re, width + TABLE_TEXT_PADDING
                                , heightPrev - 2 * TABLE_TEXT_PADDING - newLineHeight * 20f
                                , FONT, GLOBAL_FONT_SIZE, new Color(142, 142, 142));
                        newLineHeight++;
                    }
                }
            }
        }

        GLOBAL_Y = (float) height;
        return this;
    }
}
