package com.pdfeditor.pdfeditor.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;

@Service
public class PdfService {

    public String createPdf(String id) {
        return "Successfully created " + id;
    }

    public String uploadPdf(String fileName, byte[] content) throws IOException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                .build()
                .getService();

        // Read PDF file
//        Path path = Paths.get("/Users/omermersin/Developer/final/pdf-js/pdf/sa.pdf");
//        byte[] fileContent = Files.readAllBytes(path);

        // Upload PDF to Firebase Storage
        BlobId blobId = BlobId.of("pdf-editor-5f9cc.appspot.com", "any" + "/" + fileName + ".pdf");
        Blob blob = storage.create(BlobInfo.newBuilder(blobId).build(), content);

        System.out.println("File uploaded to: " + blob.getMediaLink());
        return "Successfully uploaded " + fileName;
    }

    public static byte[] downloadPdf(String fileName) throws IOException {

//        String filePath = "/Users/omermersin/Developer/final/pdf-js/pdf/sample.pdf";
//
//        Path path = Paths.get(filePath);
//
//        return Files.readAllBytes(path);

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                .build()
                .getService();

        // Download PDF from Firebase Storage
        BlobId blobId = BlobId.of("pdf-editor-5f9cc.appspot.com", "undefined" + "/" + fileName + ".pdf");
        Blob blob = storage.get(blobId);

        return blob.getContent();
    }

    public byte[] downloadPdfPage(String fileName, int pageNumber) throws IOException {
        try (PDDocument pdfDocument = Loader.loadPDF(new ByteArrayInputStream(downloadPdf(fileName)))) {
            // Validate the page number
            int totalPages = pdfDocument.getNumberOfPages();
            if (pageNumber < 1 || pageNumber > totalPages) {
                throw new IllegalArgumentException("Invalid page number");
            }

            // Create a new document and add the specific page
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PDDocument newDocument = new PDDocument();
                newDocument.addPage(new PDPage(pdfDocument.getPage(pageNumber - 1).getCOSObject()));

                // Save the modified document to the output stream
                newDocument.save(outputStream);
                newDocument.close();

                return outputStream.toByteArray();
            }
        }
    }

    public byte[] downloadSortedPdf(String fileName, List<Integer> pageOrder) throws IOException {
        // Download the original PDF content
        byte[] originalPdfContent = downloadPdf(fileName);

        // Create a new document based on the sorted page order
        try (PDDocument originalDocument = Loader.loadPDF(new ByteArrayInputStream(originalPdfContent))) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PDDocument newDocument = new PDDocument();

                for (int pageNumber : pageOrder) {
                    // Validate the page number
                    int totalPages = originalDocument.getNumberOfPages();
                    if (pageNumber < 1 || pageNumber > totalPages) {
                        throw new IllegalArgumentException("Invalid page number");
                    }

                    // Add the specific page to the new document
                    newDocument.addPage(new PDPage(originalDocument.getPage(pageNumber - 1).getCOSObject()));
                }

                // Save the modified document to the output stream
                newDocument.save(outputStream);
                newDocument.close();

                return outputStream.toByteArray();
            }
        }
    }

    public byte[] mergePdf(byte[] pdfContent1, byte[] pdfContent2) throws IOException {
        // Load the existing PDFs
        try (PDDocument pdf1Doc = Loader.loadPDF(new ByteArrayInputStream(pdfContent1));
             PDDocument pdf2Doc = Loader.loadPDF(new ByteArrayInputStream(pdfContent2))) {

            // Iterate through the pages of the second PDF and add each page to the first PDF
            for (PDPage page : pdf2Doc.getPages()) {
                pdf1Doc.addPage(page);
            }

            // Save the merged document to the output stream
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                pdf1Doc.save(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    public byte[] mergePdfAndUpload(String fileName1, String fileName2) throws IOException {
        try {
            // Download individual PDFs
            byte[] pdfContent1 = downloadPdf(fileName1);
            byte[] pdfContent2 = downloadPdf(fileName2);

            // Merge PDFs
            byte[] mergedPdfContent = mergePdf(pdfContent1, pdfContent2);

            // Upload the merged PDF back to Firebase Storage
            String mergedFileName = "merged-" + fileName1 + "-" + fileName2;
            uploadPdf(mergedFileName, mergedPdfContent);

            return mergedPdfContent;
        } catch (IOException e) {
            throw new RuntimeException("Error merging and uploading PDFs", e);
        }
    }

    public byte[] mergePdfAndReturn(String fileName1, String fileName2) throws IOException {
        try {
            // Download individual PDFs
            byte[] pdfContent1 = downloadPdf(fileName1);
            byte[] pdfContent2 = downloadPdf(fileName2);

            // Merge PDFs
            byte[] mergedPdfContent = mergePdf(pdfContent1, pdfContent2);

            return mergedPdfContent;
        } catch (IOException e) {
            throw new RuntimeException("Error merging PDFs", e);
        }
    }



    @Scheduled(fixedRate = 3600000) // Run every hour
    public void deleteOldFiles() {
        try {
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                    .build()
                    .getService();

            // Specify the folder or bucket where the files are stored
            String folder = "pdf-editor-5f9cc.appspot.com";
            String prefix = "any/"; // Adjust this based on your file structure

            // List files in the specified folder
            Page<Blob> blobs = storage.list(folder, Storage.BlobListOption.prefix(prefix));
            for (Blob blob : blobs.iterateAll()) {
                // Check the creation time of each file
                long createTimeMillis = blob.getCreateTime();
                Instant createTime = Instant.ofEpochMilli(createTimeMillis);
                Instant twoHoursAgo = Instant.now().minusSeconds(2 * 3600); // 2 hours ago

                // Delete the file if it's older than 2 hours
                if (createTime.isBefore(twoHoursAgo)) {
                    storage.delete(blob.getBlobId());
                    System.out.println("File deleted: " + blob.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately based on your application's error handling strategy
        }
    }

    public String createFolder(String folderName) {
        try {
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                    .build()
                    .getService();

            // Specify the bucket name
            String bucketName = "pdf-editor-5f9cc.appspot.com";

            // Specify the folder name (object name)
            String folderObjectName = folderName + "/"; // Ending with a forward slash to represent a folder

            // Create a new blob (folder) in the specified bucket
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, folderObjectName).build();
            storage.create(blobInfo);

            return "Folder created successfully: " + folderName;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error creating folder: " + e.getMessage();
        }
    }

    public String uploadPdfUser(String fileName, byte[] content, String User) throws IOException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                .build()
                .getService();

        // Read PDF file
//        Path path = Paths.get("/Users/omermersin/Developer/final/pdf-js/pdf/sa.pdf");
//        byte[] fileContent = Files.readAllBytes(path);

        // Upload PDF to Firebase Storage
        BlobId blobId = BlobId.of("pdf-editor-5f9cc.appspot.com", User + "/" + fileName + ".pdf");
        Blob blob = storage.create(BlobInfo.newBuilder(blobId).build(), content);

        System.out.println("File uploaded to: " + blob.getMediaLink());
        return "Successfully uploaded " + fileName;
    }


// Uploaded during the implementation of 100MB limitation
    public long getUserFolderSize(String user) throws IOException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                .build()
                .getService();

        // Specify the bucket name
        String bucketName = "pdf-editor-5f9cc.appspot.com";

        // Specify the user's folder name
        String userFolder = user + "/";

        // List files in the user's folder
        Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(userFolder)).iterateAll();

        long folderSize = 0;

        // Calculate the total size of files in the folder
        for (Blob blob : blobs) {
            folderSize += blob.getSize();
        }

        return folderSize;
    }


    public static byte[] downloadPdfUser(String fileName, String userId) throws IOException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                .build()
                .getService();

        // Download PDF from Firebase Storage using the specified user's folder
        BlobId blobId = BlobId.of("pdf-editor-5f9cc.appspot.com", userId + "/" + fileName + ".pdf");
        Blob blob = storage.get(blobId);

        return blob.getContent();
    }


    public static String performOCR(MultipartFile file) {
        try {
            // Convert MultipartFile to BufferedImage
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

            // Convert BufferedImage to Tesseract compatible format
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("/opt/homebrew/share/tessdata");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);

            // Perform OCR on the provided image
            String result = tesseract.doOCR(bufferedImage);

            System.out.println(result);
            return result;
        } catch (Exception e) {
            // Handle the exception appropriately based on your application's error handling strategy
            return e.getMessage();
        }
    }


}
