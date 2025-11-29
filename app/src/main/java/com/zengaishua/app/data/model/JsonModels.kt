package com.zengaishua.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * JSON导入格式的数据类
 */
data class JsonQuestionData(
    val status: Int,
    val msg: String,
    val obj: JsonQuestionObj
)

data class JsonQuestionObj(
    val id: String,
    val type: Int,
    val list: List<JsonQuestion>
)

data class JsonQuestion(
    val id: String,
    val stemlist: List<JsonTextItem>,
    val jxlist: List<JsonTextItem>? = null,
    val answer: String,
    val options: List<JsonOption>,
    val stemimg: List<Any>? = null,
    val type: Int,
    val jx: String? = null
)

data class JsonTextItem(
    val text: String,
    val type: Int
)

data class JsonOption(
    val valuelist: List<JsonTextItem>,
    val tag: String,
    val value: String
)
