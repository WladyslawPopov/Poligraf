package application.liedetector.engine.io

import android.content.Context
import java.io.File

class AndroidFileSystem(private val context: Context) : FileSystem {
    override fun getFilesDir(): String = context.filesDir.absolutePath
    override fun getCacheDir(): String = context.cacheDir.absolutePath

    override fun makeDir(path: String): Boolean {
        return File(path).mkdirs()
    }

    override fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }

    override fun moveFile(source: String, destination: String): Boolean {
        val src = File(source)
        val dst = File(destination)
        if (!src.exists()) return false
        
        // Ensure parent dir exists
        dst.parentFile?.mkdirs()
        
        // On Android, renameTo might fail if destination exists. Delete first for safety.
        if (dst.exists()) {
            dst.delete()
        }
        
        return src.renameTo(dst)
    }

    override fun exists(path: String): Boolean {
        return File(path).exists()
    }

    override fun listFiles(path: String): List<String> {
        return File(path).listFiles()?.map { it.absolutePath } ?: emptyList()
    }

    override fun readFile(path: String): String? {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            null
        }
    }

    override fun writeFile(path: String, content: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }
}
