package com.pdfeditor.pdfeditor.controller;

import com.pdfeditor.pdfeditor.model.MergePdfRequest;
import com.pdfeditor.pdfeditor.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class PdfController {

    public PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/uploadPdf")
    public String uploadPdf(
            @RequestParam("fileName") String fileName,
            @RequestPart("file") MultipartFile file) throws IOException, ExecutionException, InterruptedException {
        return pdfService.uploadPdf(fileName, file.getBytes());
    }

// Uploaded during the implementation of 100MB limitation
    @PostMapping("/uploadPdfUser/{user}")
    public ResponseEntity<String> uploadPdfUser(
            @RequestParam("fileName") String fileName,
            @RequestPart("file") MultipartFile file,
            @PathVariable("user") String user) throws IOException, ExecutionException, InterruptedException {

        // Check user's folder size
        long userFolderSize = pdfService.getUserFolderSize(user);
        long fileSize = file.getSize();
        long totalSize = userFolderSize + fileSize;

        // Set the maximum allowed size to 100 MB
        long maxAllowedSize = 100 * 1024 * 1024; // 100 MB in bytes

        if (totalSize > maxAllowedSize) {
            return ResponseEntity.badRequest().body("User's folder size exceeds the limit of 100 MB. File not uploaded.");
        }

        // Continue with the upload if the size is within the limit
        String result = pdfService.uploadPdfUser(fileName, file.getBytes(), user);

        return ResponseEntity.ok(result);
    }


    @GetMapping("/downloadPdf/{fileName}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("fileName") String fileName) throws IOException {
        byte[] pdfContent = pdfService.downloadPdf(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @GetMapping("/downloadPdfPage/{fileName}/{pageNumber}")
    public ResponseEntity<byte[]> downloadPdfPage(
            @PathVariable("fileName") String fileName,
            @PathVariable("pageNumber") int pageNumber) throws IOException {
        byte[] pdfPageContent = pdfService.downloadPdfPage(fileName, pageNumber);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfPageContent);
    }

    @CrossOrigin(origins = "http://127.0.0.1:5501")
    @PostMapping("/downloadSortedPdf/{fileName}")
    public ResponseEntity<byte[]> downloadSortedPdf(
            @PathVariable("fileName") String fileName,
            @RequestBody List<Integer> pageOrder) {
        try {
            byte[] sortedPdf = pdfService.downloadSortedPdf(fileName, pageOrder);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName + "-sorted.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(sortedPdf);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately based on your application's error handling strategy
            return ResponseEntity.status(500).build(); // Internal Server Error
        }
    }

    @PostMapping("/mergePdf/{fileName1}/{fileName2}")
    @CrossOrigin(origins = "http://127.0.0.1:5501")
    public ResponseEntity<byte[]> mergePdf(@PathVariable("fileName1") String fileName1, @PathVariable("fileName2") String fileName2) {
        try {
            // Call the service method to merge and upload the PDF
            byte[] mergedPdfContent = pdfService.mergePdfAndUpload(fileName1, fileName2);

            // Return the merged PDF content as a response
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(mergedPdfContent);
        } catch (IOException e) {
            // Handle the exception appropriately based on your application's error handling strategy
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/mergeAndReturnPdf/{fileName1}/{fileName2}")
    @CrossOrigin(origins = "http://127.0.0.1:5501")
    public ResponseEntity<byte[]> mergeAndReturnPdf(
            @PathVariable("fileName1") String fileName1,
            @PathVariable("fileName2") String fileName2) {
        try {
            byte[] mergedPdfContent = pdfService.mergePdfAndReturn(fileName1, fileName2);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(mergedPdfContent);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately based on your application's error handling strategy
            return ResponseEntity.status(500).build(); // Internal Server Error
        }
    }


    @PostMapping("/createFolder/{folderName}")
    @CrossOrigin(origins = "http://127.0.0.1:5501")
    public String createFolder(@PathVariable("folderName") String folderName) {
        return pdfService.createFolder(folderName);
    }


    @GetMapping("/getUserFolderSize/{user}")
    public long getUserFolderSize(@PathVariable("user") String user) {
        try {
            return pdfService.getUserFolderSize(user);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately based on your application's error handling strategy
            return -1; // Return a sentinel value or throw an exception
        }
    }

    @GetMapping(value = "/download/{fileName}/{userId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdfUser(@PathVariable String fileName, @PathVariable String userId) {
        try {
            byte[] pdfContent = pdfService.downloadPdfUser(fileName, userId);
            return ResponseEntity.ok().body(pdfContent);
        } catch (IOException e) {
            e.printStackTrace();  // Log the exception or handle it appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/performOCR")
    public ResponseEntity<String> performOCR(@RequestParam("file") MultipartFile file) {
        try {
            String ocrResult = PdfService.performOCR(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(ocrResult);
        } catch (Exception e) {
            // Handle the exception appropriately based on your application's error handling strategy
            return ResponseEntity.status(500).body("Error performing OCR on the image: " + e.getMessage());
        }
    }
}
