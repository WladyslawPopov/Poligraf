package application.poligraf.security

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream

object FirebaseAdmin {
    fun init() {
        val serviceAccountPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS") 
            ?: throw IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS path missing!")
        
        val serviceAccount = FileInputStream(serviceAccountPath)

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
        println("FIREBASE: Admin SDK initialized successfully.")
    }
}
