package com.tridya.ebookhaven.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URLDecoder
import java.util.zip.ZipFile

class FindAuthor {
    @Throws(IOException::class)
    fun FindAuthor1(srcDir: String?): String {
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
                    if (line.contains("creator")) {
                        val creatorIndex = line.indexOf("creator")
                        val firstIndex = line.indexOf(">", creatorIndex)
                        val lastIndex = line.indexOf("<", firstIndex)
                        return if (firstIndex != -1 && lastIndex != -1 && firstIndex + 1 != lastIndex) {
                            line.substring(firstIndex + 1, lastIndex)
                        } else {
                            "N/A"
                        }
                    }
                }
                inputStream.close()
                bufferedReader.close()
                return "N/A"
            }
        }
        zipFile.close()
        return "N/A"
    }
}