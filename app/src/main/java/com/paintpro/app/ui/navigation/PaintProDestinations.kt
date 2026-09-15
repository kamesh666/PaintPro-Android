package com.paintpro.app.ui.navigation

/** Central place for every nav route string, plus the tiny helpers that build parameterized ones. */
object PaintProDestinations {
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val DASHBOARD = "dashboard"

    const val SITES = "sites"
    private const val SITE_FORM_BASE = "site_form"
    const val SITE_FORM_ARG = "siteId"
    const val SITE_FORM = "$SITE_FORM_BASE?$SITE_FORM_ARG={$SITE_FORM_ARG}"
    fun siteForm(siteId: String? = null) = "$SITE_FORM_BASE?$SITE_FORM_ARG=${siteId ?: ""}"

    const val LABOURS = "labours"
    private const val LABOUR_FORM_BASE = "labour_form"
    const val LABOUR_FORM_ARG = "labourId"
    const val LABOUR_FORM = "$LABOUR_FORM_BASE?$LABOUR_FORM_ARG={$LABOUR_FORM_ARG}"
    fun labourForm(labourId: String? = null) = "$LABOUR_FORM_BASE?$LABOUR_FORM_ARG=${labourId ?: ""}"

    const val ATTENDANCE = "attendance"
    const val SETTINGS = "settings"
}
