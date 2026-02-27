package com.example.danceplayer.model

import org.json.JSONObject

data class Tag(
    var name:String = "",
    var type:Type = Type.STRING,
    var arg:Int = 0
) {
    enum class Type(val id: Int) {
        STRING(0),
        INT(1),
        FLOAT(2),
        RATING(3),
        BOOL(4),
        DATETIME(5),
        NONE(-1);

        companion object {
            fun fromInteger(x: Int): Type =
                entries.firstOrNull { it.id == x } ?: NONE


        }
        fun getText(arg: Int):String {
            when (this) {
                STRING -> return "Text"
                INT -> return "Integer"
                FLOAT -> return "Decimal"
                RATING -> return "Rating"
                BOOL -> return "Yes/No"
                DATETIME -> {
                    if (arg < 5) return "Date"
                    return "Time"
                }
                NONE -> return ""
            }
        }

    }
    companion object {

        fun fromJSON(json: JSONObject, onError: (String) -> Unit): Tag? {
            return try {
                Tag(
                    name = json.getString("name"),
                    type = Tag.Type.fromInteger(json.getInt("type")),
                    arg = json.optInt("arg", 0)
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
        json.put("arg", arg)
        return json
    }
}