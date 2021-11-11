/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.databinding.twowaysample.ui

import android.content.Context
import androidx.databinding.BindingAdapter
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.example.android.databinding.twowaysample.R


/**
 * A collection of [BindingAdapter]s for different UI-related tasks.
 * 用于不同UI相关任务的[BindingAdapter]集合。
 * In Kotlin you can write the Binding Adapters in the traditional way:
 * 在Kotlin中，您可以用传统方式编写绑定适配器：
 * ```
 * @BindingAdapter("property")
 * @JvmStatic fun propertyMethod(view: ViewClass, parameter1: Param1, parameter2: Param2...)
 * ```
 *
 * Or using extension functions:
 *
 * ```
 * @BindingAdapter("property")
 * @JvmStatic fun ViewClass.propertyMethod(parameter1: Param1, parameter2: Param2...)
 * ```
 *
 * See [EditText.clearTextOnFocus].
 * 另外，请记住，只有【在类或对象内定义方法时】才需要@JvmStatic。考虑将绑定适配器移动到文件的顶层。
 * Also, keep in mind that @JvmStatic is only necessary if you define the methods inside a class or
 * object. Consider moving the Binding Adapters to the top level of the file.
 */
object BindingAdapters {

    /**
     * When defined in an [EditText], this [BindingAdapter] will clear the text on focus and
     * set the previous value if the user doesn't enter one. When the focus leaves, it calls
     * the listener that was passed as an argument.
     *
     * Note that `android:selectAllOnFocus="true"` does something similar but not exactly the same.
     *
     * @see [clearTextOnFocus] for a version without a listener.
     */
    // 在聚焦和销毁时候清理
    @BindingAdapter("clearOnFocusAndDispatch")
    @JvmStatic fun clearOnFocusAndDispatch(view: EditText, listener: View.OnFocusChangeListener?) {
        view.onFocusChangeListener = View.OnFocusChangeListener { focusedView, hasFocus ->
            val textView = focusedView as TextView
            if (hasFocus) {
                // Delete contents of the EditText if the focus entered.
                view.setTag(R.id.previous_value, textView.text)
                textView.text = ""
            } else {
                if (textView.text.isEmpty()) {
                    val tag: CharSequence? = textView.getTag(R.id.previous_value) as CharSequence
                    textView.text = tag ?: ""
                }
                // If the focus left, update the listener
                listener?.onFocusChange(focusedView, hasFocus)
            }
        }
    }

    /**
     * Clears the text on focus. 当聚焦清理文本
     *
     * This method is using extension functions. It's equivalent to: // 等效
     * ```
     * @JvmStatic fun clearTextOnFocus(view: EditText, enabled: Boolean)...
     * ```
     */
    @BindingAdapter("clearTextOnFocus")
    @JvmStatic fun EditText.clearTextOnFocus(enabled: Boolean) {
        if (enabled) {
            clearOnFocusAndDispatch(this, null)
        } else {
            this.onFocusChangeListener = null
        }
    }

    /**
     * Hides keyboard when the [EditText] is focused.
     *
     * Note that there can only be one [TextView.OnEditorActionListener] on each [EditText] and
     * this [BindingAdapter] sets it.
     * 请注意，每个[EditText]上只能有一个[TextView.OnEditorActionListener]，此[BindingAdapter]会设置它。
     */
    @BindingAdapter("hideKeyboardOnInputDone")
    @JvmStatic fun hideKeyboardOnInputDone(view: EditText, enabled: Boolean) {
        if (!enabled) return
        val listener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                view.clearFocus()
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            false
        }
        view.setOnEditorActionListener(listener)
    }

    /*
     * 您可以创建自己的绑定适配器，使布局更易于阅读，而不是在XML布局中使用if-else语句。
     * Instead of having if-else statements in the XML layout, you can create your own binding
     * adapters, making the layout easier to read.
     *
     * Instead of
     *
     * `android:visibility="@{viewmodel.isStopped ? View.INVISIBLE : View.VISIBLE}"`
     *
     * you use:
     *
     * `android:invisibleUnless="@{viewmodel.isStopped}"`
     *
     */

    /**
     * Makes the View [View.INVISIBLE] unless the condition is met.
     * 使视图[视图.不可见]，除非满足条件。
     */
    @Suppress("unused")
    @BindingAdapter("invisibleUnless")
    @JvmStatic fun invisibleUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Makes the View [View.GONE] unless the condition is met.
     * 除非满足条件，否则将创建视图[View.GONE]。
     */
    @Suppress("unused")
    @BindingAdapter("goneUnless")
    @JvmStatic fun goneUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * In [ProgressBar], [ProgressBar.setMax] must be called before [ProgressBar.setProgress].
     * By grouping both attributes in a BindingAdapter we can make sure the order is met.
     * 通过在BindingAdapter中对这两个属性进行分组，我们可以确保满足顺序。
     *
     * Also, this showcases how to deal with multiple API levels.
     * 此外，本文还展示了如何处理多个API级别。
     */
    @BindingAdapter(value=["android:max", "android:progress"], requireAll = true)
    @JvmStatic fun updateProgress(progressBar: ProgressBar, max: Int, progress: Int) {
        progressBar.max = max
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(progress, false)
        } else {
            progressBar.progress = progress
        }
    }
    // 失去焦点
    @BindingAdapter("loseFocusWhen")
    @JvmStatic fun loseFocusWhen(view: EditText, condition: Boolean) {
        if (condition) view.clearFocus()
    }
}
