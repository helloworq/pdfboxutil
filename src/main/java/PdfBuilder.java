import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * create pdf with builder model
 */
public class PdfBuilder {
    public static void main(String[] args) throws IOException {
        List<List<String>> question = Arrays.asList(
                Arrays.asList("Question", "Who Asked", "Date"),
                Arrays.asList("How can i get stars", "lei.zhou", "2022-08-16"),
                Arrays.asList("Why can not i get even one star", "lei.zhou", "16-08-2022")
        );

        PdfBuilder.builder()
                .row().moveX(200f).title("Google")
                .row().moveY(-70f).grayText(color("Search :")).moveX(60f).line(0f, -5f, 340f, -5f).moveX(340f).grayText(color("input text"))
                .row().moveY(-30f).boldText("There are some other question asked by others ")
                .row().table(List.of(GLOBAL_X.intValue(), 300, 120, 120), question, List.of(1), List.of())
                .create();
    }

    private PDDocument document;
    private PDPageContentStream contentStream;
    public static final PDRectangle PAPER = PDRectangle.A3;
    public static PDFont FONT = PDType1Font.TIMES_ROMAN;
    public static final Float GLOBAL_FONT_SIZE = 16f;
    public static final Float INIT_X = 50.0f;
    public static final Float INIT_Y = PAPER.getHeight() - 70.0f;//pdfbox默认第一象限
    public static final Float BOTTOM_HEIGHT = 80f;
    public static Float GLOBAL_X = INIT_X;
    public static Float GLOBAL_Y = INIT_Y;
    public static final String COLOR_TAG_START = "<cr>";
    public static final String COLOR_TAG_END = "</cr>";
    public static final String PDF_BASE_PATH = "C:\\Users\\lei.zhou\\Desktop\\pdf\\";
    public static final String INIT_PDF = PDF_BASE_PATH + RandomStringUtils.random(5) + ".pdf";

    public PdfBuilder() throws IOException {
        this.document = new PDDocument();
        PDPage firstPage = new PDPage(PAPER);
        this.document.addPage(firstPage);
        this.contentStream = new PDPageContentStream(document, firstPage);
        FONT = PDType1Font.HELVETICA;
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
            GLOBAL_Y = INIT_Y;
            return this;
        } else {
            GLOBAL_X = INIT_X;
            GLOBAL_Y = GLOBAL_Y - 25f;
            return this;
        }
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
        //contentStream.setStrokingColor(Color.BLACK);
        contentStream.setRenderingMode(RenderingMode.FILL);
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
        float height = PdfUtil.customTable(contentStream, FONT,
                widths,
                PdfUtil.calculateRowHeight(GLOBAL_Y.intValue(), rowData, FONT, widths),
                colorRows,
                colorColumns, rowData,
                Color.GRAY);
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
        this.document.save(INIT_PDF);
        this.document.close();
    }

    private static String color(String text) {
        return StringUtils.isNotEmpty(text) ? (COLOR_TAG_START + text + COLOR_TAG_END) : text;
    }
}
