package application.poligraf.engine.io

interface FileSystem {
    fun getFilesDir(): String
    fun getCacheDir(): String
    fun makeDir(path: String): Boolean
    fun deleteFile(path: String): Boolean
    fun moveFile(source: String, destination: String): Boolean
    fun exists(path: String): Boolean
    fun listFiles(path: String): List<String>
    fun readFile(path: String): String?
    fun writeFile(path: String, content: String): Boolean
}
