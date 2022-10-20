package com.linkiing.fdsmeshlibdemo.utils

import android.content.Context
import java.io.*

object FileUtils {

    /**
     * 配网数据保存目录
     */
    fun getMeshJsonDir(ctx: Context): String {
        val file = ctx.getFileStreamPath("ShearJson")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }

    /**
     * 保存或覆盖Json
     */
    fun saveOrReplaceJson(ctx: Context, jsonStr: String, jsonName: String) {
        saveStrToFile(jsonStr, getMeshJsonDir(ctx), jsonName)
    }

    /**
     *  读取Json文件
     */
    fun getJsonSelect(path: String): String {
        return readFile2String(path)
    }

    /**
     * 删除目录下的所有文件
     */
    fun deleteAllFiles(file: File) {
        val files = file.listFiles()
        if (files != null) {
            for (f in files) {
                if (f.isDirectory) { // 判断是否为文件夹
                    deleteAllFiles(f)
                    try {
                        f.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f)
                        try {
                            f.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除文件
     */
    fun deleteFile(file: File) {
        if (file.exists()) {
            try {
                file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 重命名
     */
    fun reNameFile(path: String, newName: String): Boolean {
        val file = File(path)
        return reNameFile(file, newName)
    }

    /**
     * 重命名
     */
    fun reNameFile(file: File, newName: String): Boolean {
        if (file.exists()) {
            if (file.isFile) {
                try {
                    val dir = file.parent ?: return false
                    val newFile = File(dir, newName)
                    if (newFile.exists()) {
                        return false
                    }
                    file.renameTo(newFile)
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }

    /**
     * 保存String到本地文件
     */
    private fun saveStrToFile(content: String, filePath: String, fileName: String) {
        var fWriter: FileWriter? = null
        try {
            val f = File(filePath)
            if (!f.exists()) {
                f.mkdirs()
            }
            val file = File(f, fileName)
            if (file.exists()) {
                file.delete()
            } else {
                file.createNewFile()
            }

            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            fWriter = FileWriter(file)
            fWriter.write(content)
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            try {
                if (fWriter != null) {
                    fWriter.flush()
                    fWriter.close()
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * readFile2byte
     */
    private fun readFile2String(filePath: String, fileName: String): String {
        return readFile2String(filePath + File.separator + fileName)
    }

    private fun readFile2String(path: String): String {
        val file = File(path)
        if (!file.exists()) {
            return ""
        }
        if (!file.isFile) {
            return ""
        }
        val fis: FileInputStream
        try {
            fis = FileInputStream(file)
            val bos = ByteArrayOutputStream(file.length().toInt())
            val buffer = ByteArray(1024)
            var len: Int
            while (fis.read(buffer).also { len = it } > 0) {
                bos.write(buffer, 0, len)
            }
            fis.close()
            bos.close()
            return bos.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}