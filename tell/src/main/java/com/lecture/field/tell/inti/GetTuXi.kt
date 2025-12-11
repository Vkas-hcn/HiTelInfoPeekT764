package com.lecture.field.tell.inti

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class StringDelegate(private val value: String) : ReadOnlyProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return value
    }
}

object GetTuXi {
    val upUrl: String by StringDelegate("https://test-list.devinfocupramstatus.com/olive/lame")
    val adminUrl: String by StringDelegate("https://ertu.devinfocupramstatus.com/apitest/cds/pk/")
    val pangKey: String by StringDelegate("8580262")
    val applyKey: String by StringDelegate("5MiZBZBjzzChyhaowfLpyR")
}