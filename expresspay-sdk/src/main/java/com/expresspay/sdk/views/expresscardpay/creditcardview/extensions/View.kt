/* Developer: Zohaib Kambrani (a2zzuhaib@gmai.com) */

package com.expresspay.sdk.views.expresscardpay.creditcardview.extensions

import android.view.View

/**
 * Sets the width of the view
 */
internal fun View.setWidth(width: Int) {
    layoutParams.width = width
}

/**
 * Sets the height of the View
 */
internal fun View.setHeight(height: Int) {
    layoutParams.height = height
}

/**
 * Sets both the height and width of the View
 */
internal fun View.setSizes(sizes: Int) {
    setHeight(sizes)
    setWidth(sizes)
}