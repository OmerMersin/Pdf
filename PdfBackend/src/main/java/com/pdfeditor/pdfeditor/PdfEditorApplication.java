package com.pdfeditor.pdfeditor;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@SpringBootApplication
@EnableScheduling
public class PdfEditorApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(PdfEditorApplication.class, args);

		FileInputStream serviceAccount =
				new FileInputStream("src/main/resources/serviceAccountKey.json");

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.setDatabaseUrl("https://pdf-editor-4b574-default-rtdb.firebaseio.com")
				.setStorageBucket("pdf-editor-4b574.appspot.com")
				.build();

		FirebaseApp.initializeApp(options);

		System.out.println("Hello World");
	}

}
