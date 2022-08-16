import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Demo {
    public static final PDRectangle PAPER = PDRectangle.A3;
    public static final PDFont FONT = PDType1Font.TIMES_ROMAN;
    public static final Float GLOBAL_FONT_SIZE = 16f;
    public static Float GLOBAL_X = 50.0f;
    public static Float GLOBAL_Y = PAPER.getHeight() - 70.0f;//pdfbox默认第一象限
    public static final String PDF_BASE_PATH = "C:\\Users\\zhoudashuai\\Desktop\\pdf\\";
    public static final String INIT_PDF = PDF_BASE_PATH + RandomStringUtils.random(5) + ".pdf";

    public static void main(String[] args) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage my_page = new PDPage(PAPER);
            document.addPage(my_page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, my_page)) {
                //draw color text with color tag (<cr> any text </cr>)  通过颜色标签绘制字符
                PdfUtil.drawBlueText(contentStream,
                        "pdfboxutil is a <cr>good</cr> util for <cr>pdfbox</cr> lib",
                        GLOBAL_X,
                        GLOBAL_Y,
                        FONT,
                        GLOBAL_FONT_SIZE);

                GLOBAL_Y = GLOBAL_Y - 50f;


                //create a table and you can set the color at any row or cloumn 创建一个表格，可以指定着色行，列
                List<List<String>> tableData = Arrays.asList(
                        Arrays.asList("", "COMPANY", "EMAIL", "TEL", "CONTACT"),
                        Arrays.asList("FIRST", "<cr>AMAZON</cr>", "123@MAIL", "123", "123"),
                        Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
                        Arrays.asList("THIRD", "APPLE", "123@MAIL", "<cr>123</cr>", "123")
                );
                //set table width and size  设置表格的宽度和数量
                List<Integer> tableWidthList = List.of(GLOBAL_X.intValue(), 150, 115, 115, 115, 115);
                PdfUtil.customTable(contentStream, FONT
                        , tableWidthList
                        //calculate table height per row 计算每一行的行高
                        //the method is origin you can replace it ， 方法比较原始，你可以替换掉它
                        , PdfUtil.calculateRowHeight(GLOBAL_Y.intValue(), tableData, FONT, tableWidthList)
                        , List.of(1)
                        , List.of(1)
                        , tableData
                        , Color.RED);

            }
            document.save(INIT_PDF);
        }
    }
}
