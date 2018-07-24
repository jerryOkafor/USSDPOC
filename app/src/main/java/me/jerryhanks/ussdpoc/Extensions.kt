package me.jerryhanks.ussdpoc

import android.view.accessibility.AccessibilityNodeInfo

/**
 * @author Po10cio on 21/07/2018.
 *
 **/

val AccessibilityNodeInfo.children: List<AccessibilityNodeInfo>
    get() {
        return if (childCount <= 0) {
            emptyList()
        } else {
            // getChild(index) method can return null values.
            return (0 until childCount).mapNotNull { getChild(it) }
        }
    }

val AccessibilityNodeInfo.allChildren: List<AccessibilityNodeInfo>
    get() {
        val list = children.toMutableList()
        // Copy list using `toList()` to prevent ConcurrentModificationException on original list.
        list.toList().forEach { list.addAll(it.allChildren) }
        return list
    }