package com.tridya.readsphere.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URLDecoder
import java.util.zip.ZipFile

class FindCover {
    @Throws(IOException::class)
    fun FindCoverRef(srcDir: String?): Bitmap? {
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
                    if (line.contains("itemref") && line.contains("idref")) {
                        val idrefIndex = line.indexOf("idref")
                        val firstIndex = line.indexOf("\"", idrefIndex)
                        val lastIndex = line.indexOf("\"", firstIndex + 1)
                        return if (firstIndex != -1 && lastIndex != -1 && firstIndex + 1 != lastIndex) {
                            FindCoverHtml(srcDir, line.substring(firstIndex + 1, lastIndex))
                        } else {
                            FindCoverItself(srcDir)
                        }
                    }
                }
                inputStream.close()
                bufferedReader.close()
                return FindCoverItself(srcDir)
            }
        }
        zipFile.close()
        return FindCoverItself(srcDir)
    }

    @Throws(IOException::class)
    fun FindCoverHtml(srcDir: String?, refName: String?): Bitmap? {
        var refName = refName
        refName = URLDecoder.decode(refName, "UTF-8")
        val zipFile = ZipFile(srcDir)
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            if (zipEntry.toString().endsWith(".opf")) {
                val inputStream = zipFile.getInputStream(zipEntry)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String
                while (bufferedReader.readLine().also { line = it } != null) {
                    if (line.contains(refName) && line.contains("href")) {
                        var veryLastItemIndex = 0
                        val str = line.substring(0, line.indexOf(refName))
                        var lastItemIndex = 0
                        while (lastItemIndex != -1) {
                            lastItemIndex = str.indexOf("item", lastItemIndex)
                            if (lastItemIndex != -1) {
                                lastItemIndex += "item".length
                                veryLastItemIndex = lastItemIndex
                            }
                        }
                        val hrefIndex = line.indexOf("href", veryLastItemIndex)
                        val firstIndex = line.indexOf("\"", hrefIndex)
                        val lastIndex = line.indexOf("\"", firstIndex + 1)
                        return if (firstIndex != -1 && lastIndex != -1 && firstIndex + 1 != lastIndex) {
                            FindCoverImage(srcDir, line.substring(firstIndex + 1, lastIndex))
                        } else {
                            FindCoverItself(srcDir)
                        }
                    }
                }
                inputStream.close()
                bufferedReader.close()
                return FindCoverItself(srcDir)
            }
        }
        zipFile.close()
        return FindCoverItself(srcDir)
    }

    @Throws(IOException::class)
    fun FindCoverImage(srcDir: String?, htmlName: String?): Bitmap? {
        var htmlName = htmlName
        htmlName = URLDecoder.decode(htmlName, "UTF-8")
        val zipFile = ZipFile(srcDir)
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            if (zipEntry.toString().contains(htmlName)) {
                val inputStream = zipFile.getInputStream(zipEntry)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String
                while (bufferedReader.readLine().also { line = it } != null) {
                    val charSequence = arrayOf<CharSequence>("<image", "<img")
                    for (sequence in charSequence) {
                        if (line.contains(sequence)) {
                            val imgIndex = line.indexOf(sequence.toString())
                            return if (line.contains("href")) {
                                val hrefIndex = line.indexOf("href", imgIndex)
                                val firstIndex = line.indexOf("\"", hrefIndex)
                                val lastIndex = line.indexOf("\"", firstIndex + 1)
                                if (firstIndex != -1 && lastIndex != -1 && firstIndex + 1 != lastIndex) {
                                    FindCoverLast(srcDir, line.substring(firstIndex + 1, lastIndex))
                                } else {
                                    FindCoverItself(srcDir)
                                }
                            } else if (line.contains("src")) {
                                val srcIndex = line.indexOf("src", imgIndex)
                                val firstIndex = line.indexOf("\"", srcIndex)
                                val lastIndex = line.indexOf("\"", firstIndex + 1)
                                if (firstIndex != -1 && lastIndex != -1 && firstIndex + 1 != lastIndex) {
                                    FindCoverLast(srcDir, line.substring(firstIndex + 1, lastIndex))
                                } else {
                                    FindCoverItself(srcDir)
                                }
                            } else {
                                FindCoverItself(srcDir)
                            }
                        }
                    }
                }
                inputStream.close()
                bufferedReader.close()
                return FindCoverItself(srcDir)
            }
        }
        zipFile.close()
        return FindCoverItself(srcDir)
    }

    @Throws(IOException::class)
    fun FindCoverLast(srcDir: String?, imageName: String): Bitmap? {
        var imageName = imageName
        imageName = URLDecoder.decode(imageName, "UTF-8")
        val zipFile = ZipFile(srcDir)
        val entries = zipFile.entries()
        val photo: Bitmap
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            if (imageName.contains("/")) {
                val arrOfStr =
                    imageName.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                imageName = arrOfStr[arrOfStr.size - 1]
            }

            /*
            if(imageName.contains("..")){
                int dotdotIndex = imageName.indexOf("..");
                int firstIndex = imageName.indexOf("/", dotdotIndex);
                imageName = imageName.substring(firstIndex);
            }
            */if (zipEntry.toString().contains(imageName)) {
                photo = BitmapFactory.decodeStream(zipFile.getInputStream(zipEntry))
                return photo
            }
        }
        FindCoverItself(srcDir)
        zipFile.close()
        return null
    }

    @Throws(IOException::class)
    fun FindCoverItself(srcDir: String?): Bitmap? {
        val zipFile = ZipFile(srcDir)
        val entries = zipFile.entries()
        val photo: Bitmap
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            val charSequence = arrayOf<CharSequence>(
                "cover.jpg",
                "cover.JPG",
                "cover.jpeg",
                "cover.JPEG",
                "cover.png",
                "cover.PNG"
            )
            for (sequence in charSequence) {
                if (zipEntry.toString().contains(sequence)) {
                    photo = BitmapFactory.decodeStream(zipFile.getInputStream(zipEntry))
                    return photo
                }
            }
        }
        FindCoverItself2(srcDir)
        zipFile.close()
        return null
    }

    @Throws(IOException::class)
    fun FindCoverItself2(srcDir: String?): Bitmap? {
        val zipFile = ZipFile(srcDir)
        val entries = zipFile.entries()
        val photo: Bitmap
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            val charSequence = arrayOf<CharSequence>("jpg", "JPG", "jpeg", "JPEG", "png", "PNG")
            for (sequence in charSequence) {
                if (zipEntry.toString().contains(sequence)) {
                    photo = BitmapFactory.decodeStream(zipFile.getInputStream(zipEntry))
                    return photo
                }
            }
        }
        zipFile.close()
        return null
    }
}