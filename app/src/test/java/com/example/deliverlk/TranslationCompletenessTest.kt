package com.example.deliverlk

import org.junit.Assert.fail
import org.junit.Test
import org.w3c.dom.Document
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class TranslationCompletenessTest {

    @Test
    fun testAllStringsExistInSinhalaAndTamil() {
        val baseRes = if (File("app/src/main/res").exists()) {
            File("app/src/main/res")
        } else {
            File("src/main/res")
        }

        val defaultStringsFile = File(baseRes, "values/strings.xml")
        val siStringsFile = File(baseRes, "values-si/strings.xml")
        val taStringsFile = File(baseRes, "values-ta/strings.xml")

        val defaultKeys = parseStringKeys(defaultStringsFile)
        val siKeys = parseStringKeys(siStringsFile)
        val taKeys = parseStringKeys(taStringsFile)

        val missingInSi = defaultKeys - siKeys
        val missingInTa = defaultKeys - taKeys

        var errorMsg = ""
        if (missingInSi.isNotEmpty()) {
            errorMsg += "Missing keys in values-si/strings.xml: $missingInSi. "
        }
        if (missingInTa.isNotEmpty()) {
            errorMsg += "Missing keys in values-ta/strings.xml: $missingInTa."
        }

        if (errorMsg.isNotBlank()) {
            fail(errorMsg)
        }
    }

    private fun parseStringKeys(file: File): Set<String> {
        if (!file.exists()) return emptySet()
        val keys = mutableSetOf<String>()
        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.parse(file)
            doc.normalize()
            val nodeList = doc.getElementsByTagName("string")
            for (i in 0 until nodeList.length) {
                val node = nodeList.item(i)
                val attr = node.attributes?.getNamedItem("name")
                if (attr != null) {
                    keys.add(attr.nodeValue)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return keys
    }
}
