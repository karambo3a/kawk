package org.example.runtime

import java.io.BufferedReader
import java.io.Reader
import kotlin.collections.set
import kotlin.text.toInt

class LineReader(input: Reader) {
    private var line: String? = null
    val vars: MutableMap<String, String> = mutableMapOf<String, String>()
    private val reader = BufferedReader(input)

    init {
        setFS(" ")
        setNF(1)
        setNR(0)
    }

    private fun getFS() = vars[FS]!!
    private fun setFS(newFS: String) {
        vars[FS] = newFS
    }

    private fun getCurrentLine() = vars[LINE]!!
    private fun setCurrentLine(newLine: String) {
        vars[LINE] = newLine
    }

    fun updateCurrentLine(newValue: String) {
        putVars(newValue)
        line = newValue
    }

    private fun getNF() = vars[NF]!!.toInt()
    private fun setNF(newNF: Long) {
        vars[NF] = newNF.toString()
    }

    fun updateNF(newValue: String) {
        if (vars[NF]!!.toLong() > newValue.toLong()) {
            createCurrentLine(newValue.toLong() - 1)
        }
        vars[NF] = newValue
    }

    private fun getNR() = vars[NR]!!.toInt()
    private fun setNR(newNR: Long) {
        vars[NR] = newNR.toString()
    }

    fun getValue(fieldName: String): String = vars[fieldName]!!
    fun updateField(newValue: String, fieldsNumber: Long, fieldName: String) {
        if (vars[fieldName] == null) {
            addFields(fieldsNumber)
        }
        vars[fieldName] = newValue
        createCurrentLine(fieldsNumber - 1)
    }

    fun updateIdentifier(newValue: String, fieldName: String) {
        vars[fieldName] = newValue
    }

    fun lineNotNull() = line != null

    fun readNextLine() {
        var newLine: String? = reader.readLine()
        if (newLine != null) {
            removeVars()
        }
        putVars(newLine)
        setNR(getNR() + 1L)
        line = newLine
    }

    private fun splitLine(line: String) =
        line.split(getFS()).withIndex().map { Pair(getFieldName(it.index.toLong()), it.value) }

    private fun removeVars() {
        if (line == null) {
            return
        }
        val oldFields = splitLine(line!!)
        assert(getNF() == oldFields.size)
        oldFields.forEach { field -> assert(vars.remove(field.first) == field.second) }
        assert(getCurrentLine() == line)
    }

    private fun putVars(newLine: String?) {
        if (newLine == null) {
            return
        }
        setCurrentLine(newLine)
        val newFields = splitLine(newLine)
        vars.putAll(newFields)
        setNF(newFields.size.toLong())
    }

    private fun createCurrentLine(fieldsNumber: Long) {
        val sb = StringBuilder()
        for (i in 0..fieldsNumber - 1) {
            sb.append(vars[getFieldName(i)])
            sb.append(vars[FS])
        }
        sb.append(vars[getFieldName(fieldsNumber)])
        setCurrentLine(sb.toString())
        line = vars[LINE]
    }

    private fun addFields(fieldsNumber: Long) {
        for (i in 0..fieldsNumber - 1) {
            if (vars[getFieldName(i)] == null) {
                vars[getFieldName(i)] = ""
            }
        }
        setNF(fieldsNumber)
    }

    fun close() {
        reader.close()
    }

    companion object {
        fun getFieldName(index: Long) = "\$${index + 1}"
        const val FS = "FS"
        val LINE = getFieldName(-1)
        const val NF = "NF"
        const val NR = "NR"
    }
}