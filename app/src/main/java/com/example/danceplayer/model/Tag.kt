package com.example.danceplayer.model

import org.json.JSONObject

data class Tag(
    var name:String = "",
    var type:Type = Type.STRING,
) {
    enum class Type(val id: Int) {
        STRING(0),
        INT(1),
        FLOAT(2),
        BOOL(3),
        DATETIME(4),
        DATE(5),
        TIME(6);

        companion object {
            fun fromInteger(x: Int): Type =
                entries.firstOrNull { it.id == x } ?: STRING
        }
        fun getText():String {
            when (this) {
                STRING -> return "Text"
                INT -> return "Integer"
                FLOAT -> return "Decimal"
                BOOL -> return "Yes/No"
                DATETIME -> return "Date & Time"
                DATE -> return "Date"
                TIME -> return "Time"
                else -> return ""
            }
        }

    }
    companion object {

        fun fromJSON(json: JSONObject, onError: (String) -> Unit): Tag? {
            return try {
                Tag(
                    name = json.getString("name"),
                    type = Tag.Type.fromInteger(json.getInt("type")),
                )
            } catch (e: Exception) {
                onError("Error parsing tag info: ${json.toString()}")
                return null
            }
        }
    }

    fun asJSON(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("type", type)
        return json
    }
}