package com.pdfeditor.pdfeditor.model;

public class MergePdfRequest {
    private String pdf1FileName;
    private String pdf2FileName;

    public String getPdf1FileName() {
        return pdf1FileName;
    }

    public void setPdf1FileName(String pdf1FileName) {
        this.pdf1FileName = pdf1FileName;
    }

    public String getPdf2FileName() {
        return pdf2FileName;
    }

    public void setPdf2FileName(String pdf2FileName) {
        this.pdf2FileName = pdf2FileName;
    }
}

