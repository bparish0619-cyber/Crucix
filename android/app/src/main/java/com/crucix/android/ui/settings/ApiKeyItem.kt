package com.crucix.android.ui.settings

data class ApiKeyItem(
    val label: String,
    val hint: String,
    val prefKey: String,
    val isPassword: Boolean = false,
    val isDropdown: Boolean = false,
    val dropdownOptions: List<String> = emptyList(),
    val getKeyUrl: String? = null,
    val getKeyLabel: String = "Get Key",
    val description: String = "",
    val required: Boolean = false
)

data class ApiKeyGroup(
    val title: String,
    val subtitle: String,
    val icon: String,
    val items: List<ApiKeyItem>,
    var isExpanded: Boolean = false
)
