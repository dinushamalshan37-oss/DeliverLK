package com.example.deliverlk

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class StringResourcesUnitTest {

    private fun findResDirectory(): File {
        val candidates = listOf(
            File("src/main/res"),
            File("app/src/main/res"),
            File("../app/src/main/res")
        )
        return candidates.firstOrNull { it.exists() && it.isDirectory }
            ?: throw IllegalStateException("Res directory not found in ${candidates.map { it.absolutePath }}")
    }

    private fun extractStringKeys(xmlFile: File): Set<String> {
        val keys = mutableSetOf<String>()
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(xmlFile)
        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("string")
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            val nameAttr = node.attributes?.getNamedItem("name")?.nodeValue
            if (nameAttr != null) {
                keys.add(nameAttr)
            }
        }
        return keys
    }

    @Test
    fun testAllDefaultStringKeysExistInSinhalaAndTamil() {
        val resDir = findResDirectory()
        val defaultStringsFile = File(resDir, "values/strings.xml")
        val sinhalaStringsFile = File(resDir, "values-si/strings.xml")
        val tamilStringsFile = File(resDir, "values-ta/strings.xml")

        assertTrue("values/strings.xml must exist", defaultStringsFile.exists())
        assertTrue("values-si/strings.xml must exist", sinhalaStringsFile.exists())
        assertTrue("values-ta/strings.xml must exist", tamilStringsFile.exists())

        val defaultKeys = extractStringKeys(defaultStringsFile)
        val sinhalaKeys = extractStringKeys(sinhalaStringsFile)
        val tamilKeys = extractStringKeys(tamilStringsFile)

        val missingInSinhala = defaultKeys - sinhalaKeys
        val missingInTamil = defaultKeys - tamilKeys
        val extraInSinhala = sinhalaKeys - defaultKeys
        val extraInTamil = tamilKeys - defaultKeys

        val errorMessage = StringBuilder()
        if (missingInSinhala.isNotEmpty()) {
            errorMessage.append("Missing keys in values-si/strings.xml: $missingInSinhala\n")
        }
        if (missingInTamil.isNotEmpty()) {
            errorMessage.append("Missing keys in values-ta/strings.xml: $missingInTamil\n")
        }
        if (extraInSinhala.isNotEmpty()) {
            errorMessage.append("Extra keys in values-si/strings.xml: $extraInSinhala\n")
        }
        if (extraInTamil.isNotEmpty()) {
            errorMessage.append("Extra keys in values-ta/strings.xml: $extraInTamil\n")
        }

        assertTrue(
            errorMessage.toString(),
            missingInSinhala.isEmpty() && missingInTamil.isEmpty() && extraInSinhala.isEmpty() && extraInTamil.isEmpty()
        )
    }
}