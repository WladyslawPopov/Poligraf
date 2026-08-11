package application.liedetector.engine.io

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
class IosFileSystem : FileSystem {
    private val fileManager = NSFileManager.defaultManager

    override fun getFilesDir(): String {
        return NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String
    }

    override fun getCacheDir(): String {
        return NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).first() as String
    }

    override fun makeDir(path: String): Boolean {
        return fileManager.createDirectoryAtPath(path, true, null, null)
    }

    override fun deleteFile(path: String): Boolean {
        return fileManager.removeItemAtPath(path, null)
    }

    override fun moveFile(source: String, destination: String): Boolean {
        // Use NSString methods for path manipulation
        val nsDestination = destination as NSString
        val parent = nsDestination.stringByDeletingLastPathComponent
        makeDir(parent)
        
        return fileManager.moveItemAtPath(source, destination, null)
    }

    override fun exists(path: String): Boolean {
        return fileManager.fileExistsAtPath(path)
    }

    override fun listFiles(path: String): List<String> {
        val contents = fileManager.contentsOfDirectoryAtPath(path, null) ?: return emptyList()
        return (contents as List<String>).map { "$path/$it" }
    }

    override fun readFile(path: String): String? {
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
    }

    override fun writeFile(path: String, content: String): Boolean {
        val nsPath = path as NSString
        val parent = nsPath.stringByDeletingLastPathComponent
        makeDir(parent)
        return (content as NSString).writeToFile(path, true, NSUTF8StringEncoding, null)
    }
}
