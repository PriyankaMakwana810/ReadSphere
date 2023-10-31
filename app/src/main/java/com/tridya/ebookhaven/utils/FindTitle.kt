package com.tridya.ebookhaven.utils

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URLDecoder
import java.util.zip.ZipFile

class FindTitle {
    @Throws(IOException::class)
    fun FindTitle1(srcDir: String?): String {
        var srcDir = srcDir
        srcDir = URLDecoder.decode(srcDir, "UTF-8")
        val zipFile = ZipFile(srcDir)
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            if (zipEntry.toString().endsWith(".opf")) {
                val inputStream = zipFile.getInputStream(zipEntry)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String
                while (bufferedReader.readLine().also { line = it } != null) {
                    if (line.contains("title") && !line.contains("calibre:title_sort")) {
                        val titleIndex = line.indexOf("title")
                        val firstIndex = line.indexOf(">", titleIndex)
                        val lastIndex = line.indexOf("<", firstIndex)
                        return if (firstIndex != -1 && lastIndex != -1 && firstIndex + 1 != lastIndex) {
                            line.substring(firstIndex + 1, lastIndex)
                        } else {
                            val file = File(srcDir)
                            file.name.substring(0, file.name.length - 5)
                        }
                    }
                }
                inputStream.close()
                bufferedReader.close()
                val file = File(srcDir)
                return file.name.substring(0, file.name.length - 5)
            }
        }
        zipFile.close()
        val file = File(srcDir)
        return file.name.substring(0, file.name.length - 5)
    }
    @Throws(IOException::class)
    fun FindPublisher(srcDir: String?): String {
        var srcDir = srcDir
        srcDir = URLDecoder.decode(srcDir, "UTF-8")
        val zipFile = ZipFile(srcDir)
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            if (zipEntry.toString().endsWith(".opf")) {
                val inputStream = zipFile.getInputStream(zipEntry)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String
                while (bufferedReader.readLine().also { line = it } != null) {
                    if (line.contains("publisher")) {
                        val titleIndex = line.indexOf("publisher")
                        val firstIndex = line.indexOf(">", titleIndex)
                        val lastIndex = line.indexOf("<", firstIndex)
                        return if (firstIndex != -1 && lastIndex != -1 && firstIndex + 1 != lastIndex) {
                            line.substring(firstIndex + 1, lastIndex)
                        } else {
                            val file = File(srcDir)
                            file.name.substring(0, file.name.length - 5)
                        }
                    }
                }
                inputStream.close()
                bufferedReader.close()
                val file = File(srcDir)
                return file.name.substring(0, file.name.length - 5)
            }
        }
        zipFile.close()
        val file = File(srcDir)
        return file.name.substring(0, file.name.length - 5)
    }
    @Throws(IOException::class)
    fun FindLanguage(srcDir: String?): String {
        var srcDir = srcDir
        srcDir = URLDecoder.decode(srcDir, "UTF-8")
        val zipFile = ZipFile(srcDir)
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            if (zipEntry.toString().endsWith(".opf")) {
                val inputStream = zipFile.getInputStream(zipEntry)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String
                while (bufferedReader.readLine().also { line = it } != null) {
                    if (line.contains("language")) {
                        val titleIndex = line.indexOf("language")
                        val firstIndex = line.indexOf(">", titleIndex)
                        val lastIndex = line.indexOf("<", firstIndex)
                        return if (firstIndex != -1 && lastIndex != -1 && firstIndex + 1 != lastIndex) {
                            line.substring(firstIndex + 1, lastIndex)
                        } else {
                            val file = File(srcDir)
                            file.name.substring(0, file.name.length - 5)
                        }
                    }
                }
                inputStream.close()
                bufferedReader.close()
                val file = File(srcDir)
                return file.name.substring(0, file.name.length - 5)
            }
        }
        zipFile.close()
        val file = File(srcDir)
        return file.name.substring(0, file.name.length - 5)
    }
    @Throws(IOException::class)
    fun FindDescription(srcDir: String?): String {
        var srcDir = srcDir
        srcDir = URLDecoder.decode(srcDir, "UTF-8")
        val zipFile = ZipFile(srcDir)
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            if (zipEntry.toString().endsWith(".opf")) {
                val inputStream = zipFile.getInputStream(zipEntry)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String
                while (bufferedReader.readLine().also { line = it } != null) {
                    if (line.contains("description")) {
                        val titleIndex = line.indexOf("description")
                        val firstIndex = line.indexOf(">", titleIndex)
                        val lastIndex = line.indexOf("<", firstIndex)
                        return if (firstIndex != -1 && lastIndex != -1 && firstIndex + 1 != lastIndex) {
                            line.substring(firstIndex + 1, lastIndex)
                        } else {
                            val file = File(srcDir)
                            file.name.substring(0, file.name.length - 5)
                        }
                    }
                }
                inputStream.close()
                bufferedReader.close()
                val file = File(srcDir)
                return file.name.substring(0, file.name.length - 5)
            }
        }
        zipFile.close()
        val file = File(srcDir)
        return file.name.substring(0, file.name.length - 5)
    }
}