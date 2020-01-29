package org.komputing.etherscan.downloader

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val ETHERSCAN_API_TOKEN = "YourApiKeyToken"
private val baseContractPath = File("contracts/1/").apply { mkdirs() }
private val moshi = Moshi.Builder().build()
internal val filenameToContentToCodeAdapter = moshi.adapter<FilenameToContentToCode>(Map::class.java).lenient()

suspend fun main() {
    val inputFile = File("export-verified-contractaddress-opensource-license.csv")

    if (!inputFile.exists()) {
        error(
            """Cannot load the CSV ($inputFile)
                |please download from https://etherscan.io/exportData?type=open-source-contract-codes""".trimMargin()
        )
    }

    val list = csvReader().readAllWithHeader(inputFile)

    val addresses = list.mapNotNull { it["ContractAddress"] }
    addresses.forEachIndexed { i, address ->
        println("" + i + "/" + addresses.size)
        processContract(address)
    }
}

data class CodeGetResult(val SourceCode: String, val ABI: String)
data class CodeGetResultEnvelope(val status: Int, val message: String, val result: List<CodeGetResult>)

typealias ContentToCode = Map<String, String>
typealias FilenameToContentToCode = Map<String, ContentToCode>
typealias SourcesFilenameToContentToCode = Map<String, FilenameToContentToCode>

private suspend fun processContract(address: String) {
    val contractPath = File(baseContractPath, address.replace("0x", ""))
    val abiFile = File(contractPath, "contract.abi")

    if (contractPath.exists()) return

    contractPath.mkdirs()
    val url =
        "https://api.etherscan.io/api?module=contract&action=getsourcecode&address=$address&apikey=$ETHERSCAN_API_TOKEN"
    Fuel.get(url).timeout(1000).responseObject<CodeGetResultEnvelope> { _, _, result ->
        when (result) {
            is Result.Failure -> {
                println("problem downloading code for $address")
                println("Reason: " + result.getException().message)
                println("Retrying")
                retryProcessContract(address)
            }
            is Result.Success<CodeGetResultEnvelope> -> {
                val codeGetEnvelopeResult = result.get()
                if (codeGetEnvelopeResult.status == 1 && codeGetEnvelopeResult.message == "OK") {
                    val codeGetResult = codeGetEnvelopeResult.result.first()


                    if (codeGetResult.SourceCode.startsWith("{")) { // multiple files
                        processMultipleFiles(
                            codeGetResult,
                            contractPath,
                            address
                        )
                    } else { // single file
                        println("saving contract for $address")
                        val sourceFile = File(contractPath, "contract.sol")
                        sourceFile.writeText(codeGetResult.SourceCode)
                    }

                    abiFile.writeText(codeGetResult.ABI)
                } else {
                    println("error $codeGetEnvelopeResult")
                    retryProcessContract(address)
                }
            }
            else -> println(result)

        }

    }
    delay(420)
}

private fun retryProcessContract(address: String) = GlobalScope.launch {
    processContract(address)
}

private fun processMultipleFiles(
    codeGetResult: CodeGetResult,
    contractPath: File,
    address: String
) {

    try {
        filenameToContentToCodeAdapter.fromJson(codeGetResult.SourceCode)?.processData(contractPath)
    } catch (e: JsonEncodingException) {
        try {
            parseAlternativeJSON(codeGetResult.SourceCode)?.processData(contractPath) ?: error("sources not found")
        } catch (e: JsonEncodingException) {
            println("could not deserialize:" + codeGetResult.SourceCode + "\nNot one of the 2 formats we know about.")
            retryProcessContract(address)
        }
    }
}

internal fun parseAlternativeJSON(source: String): FilenameToContentToCode? {
    val adapterAlternativeJson = moshi.adapter<SourcesFilenameToContentToCode>(Map::class.java).lenient()
    val cleanedSourceJson = source.substring(1, source.length - 1)
    return adapterAlternativeJson.fromJson(cleanedSourceJson)?.get("sources")
}

private fun FilenameToContentToCode.processData(contractPath: File) {
    forEach { filenameToCodeMap ->
        val fileName = filenameToCodeMap.key.substringAfterLast("/")
        val sourceFile = File(contractPath, fileName)
        println("-> $fileName")

        sourceFile.writeText(
            filenameToCodeMap.value["content"] ?: error("content missing for $fileName")
        )
    }
}
