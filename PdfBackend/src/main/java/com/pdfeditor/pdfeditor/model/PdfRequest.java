package com.pdfeditor.pdfeditor.model;

import java.util.List;

public class PdfRequest {
    private String pdfBuffer;
    private List<Integer> pageOrder;


    public String getPdfBuffer() {
        return pdfBuffer;
    }

    public void setPdfBuffer(String pdfBuffer) {
        this.pdfBuffer = pdfBuffer;
    }

    public List<Integer> getPageOrder() {
        return pageOrder;
    }

    public void setPageOrder(List<Integer> pageOrder) {
        this.pageOrder = pageOrder;
    }
}

