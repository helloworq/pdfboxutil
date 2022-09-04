import cn.hutool.core.util.StrUtil;
import tool.PdfBuilder;
import tool.PdfUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Demo {
    public static final String COLOR_TAG_START = "<cr>";
    public static final String COLOR_TAG_END = "</cr>";

    static String title = "pdfboxutil is a good util for pdfbox lib";
    static List<List<String>> tableData = Arrays.asList(
            Arrays.asList("", "COMPANY", "EMAIL", "TEL", "CONTACT"),
            Arrays.asList("FIRST", "<cr>AMAZON</cr>", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("SECOND", "MICROSOFT", "123@MAIL", "123", "123"),
            Arrays.asList("THIRD", "APPLE", "123@MAIL", "<cr>123</cr>", "123")
    );

    public static void main(String[] args) throws IOException {
        PdfBuilder.builder()
                .row().moveX(150f).title(title)
                .row().blueText(color("Name:  ")).moveX(50f).line(0f, -5f, 340f, -5f).blueText("Bob")
                .row().powerTable(PdfUtil.listOf(100, 100, 100, 100, 100), tableData, List.of(0), List.of(0))
                .create();
    }

    private static String color(String text) {
        return StrUtil.isNotEmpty(text) ? (COLOR_TAG_START + text + COLOR_TAG_END) : text;
    }
}
