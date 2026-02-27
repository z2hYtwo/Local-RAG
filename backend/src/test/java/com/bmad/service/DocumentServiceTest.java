package com.bmad.service;

import com.bmad.model.DocumentSegment;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentServiceTest {

    @Test
    void parsePdf_extractsPageNumbers() throws Exception {
        byte[] pdfBytes;
        try (PDDocument doc = new PDDocument()) {
            PDPage page1 = new PDPage();
            PDPage page2 = new PDPage();
            doc.addPage(page1);
            doc.addPage(page2);
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                cs.beginText();
                cs.setFont(font, 12);
                cs.newLineAtOffset(50, 750);
                cs.showText("PDF Page One");
                cs.endText();
            }

            try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
                cs.beginText();
                cs.setFont(font, 12);
                cs.newLineAtOffset(50, 750);
                cs.showText("PDF Page Two");
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            pdfBytes = out.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile("files", "test.pdf", "application/pdf", pdfBytes);
        DocumentService service = new DocumentService();
        List<DocumentSegment> segments = service.parseDocument(file);

        assertEquals(2, segments.size());
        assertEquals(1, segments.get(0).getMetadata().get("page_number"));
        assertEquals(2, segments.get(1).getMetadata().get("page_number"));
    }

    @Test
    void parsePptx_extractsSlideNumbers() throws Exception {
        byte[] pptxBytes;
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextBox box = slide.createTextBox();
            box.setText("Slide One");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ppt.write(out);
            pptxBytes = out.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile("files", "deck.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxBytes);
        DocumentService service = new DocumentService();
        List<DocumentSegment> segments = service.parseDocument(file);

        assertEquals(1, segments.size());
        assertEquals(1, segments.get(0).getMetadata().get("slide_number"));
    }

    @Test
    void parseDocx_extractsParagraphIndexAndSectionHeader() throws Exception {
        byte[] docxBytes;
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph heading = doc.createParagraph();
            heading.setStyle("Heading1");
            heading.createRun().setText("Section A");

            XWPFParagraph p1 = doc.createParagraph();
            p1.createRun().setText("First paragraph text");

            XWPFParagraph p2 = doc.createParagraph();
            p2.createRun().setText("Second paragraph text");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            docxBytes = out.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile("files", "doc.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes);
        DocumentService service = new DocumentService();
        List<DocumentSegment> segments = service.parseDocument(file);

        assertEquals(3, segments.size());
        assertEquals(1, segments.get(0).getMetadata().get("paragraph_index"));
        assertEquals("Section A", segments.get(0).getMetadata().get("section_header"));
        assertEquals(2, segments.get(1).getMetadata().get("paragraph_index"));
        assertEquals("Section A", segments.get(1).getMetadata().get("section_header"));
        assertEquals(3, segments.get(2).getMetadata().get("paragraph_index"));
        assertEquals("Section A", segments.get(2).getMetadata().get("section_header"));
    }

    @Test
    void parseTxt_setsSourceType() throws Exception {
        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("files", "a.txt", "text/plain", bytes);
        DocumentService service = new DocumentService();
        List<DocumentSegment> segments = service.parseDocument(file);

        assertEquals(1, segments.size());
        assertEquals("txt", segments.get(0).getMetadata().get("source_type"));
    }
}
