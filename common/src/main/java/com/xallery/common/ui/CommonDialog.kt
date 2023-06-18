package com.xallery.common.ui

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.core.view.updatePadding
import com.xallery.common.R
import com.xallery.common.databinding.DialogCommonBinding
import com.xihh.base.android.BaseDialog

open class CommonDialog(context: Context) : BaseDialog<DialogCommonBinding>(context) {

    override fun getViewBinding() = DialogCommonBinding.inflate(layoutInflater)

    private val isTitle: Boolean
        get() = titleRes != -1 || !title.isNullOrEmpty()
    private var titleRes: Int = -1
    private var title: String = ""
    private var titleSize: Float? = null
    private var isTitleBold: Boolean = false

    private val isContent: Boolean
        get() = contentRes != -1 || !content.isNullOrEmpty()
    private var contentRes: Int = -1
    private var content: String = ""
    private var contentGravity: Int = Gravity.CENTER
    private var contentSize: Float? = null

    private val isCancel: Boolean
        get() = cancelRes != -1 || !cancelText.isNullOrEmpty()
    private var cancelRes: Int = -1
    private var cancelText: String = ""
    private var cancelBgRes: Int = -1
    private var cancelInvoke: (CommonDialog.() -> Unit)? = null

    private val isConfirm: Boolean
        get() = confirmRes != -1 || confirmText.isNotEmpty()
    private var confirmRes: Int = -1
    private var confirmText: String = ""
    private var confirmBgRes: Int = -1
    private var confirmInvoke: (CommonDialog.() -> Unit)? = null

    private var topPadding: Int? = null
    private var bottomPadding: Int? = null

    fun title(titleRes: Int, titleSize: Float? = null, isTitleBold: Boolean = false): CommonDialog {
        this.titleRes = titleRes
        this.titleSize = titleSize
        this.isTitleBold = isTitleBold
        return this
    }

    fun title(title: String, titleSize: Float? = null, isTitleBold: Boolean = false): CommonDialog {
        this.title = title
        this.titleSize = titleSize
        this.isTitleBold = isTitleBold
        return this
    }

    fun content(
        contentRes: Int, contentSize: Float? = null, gravity: Int = Gravity.CENTER,
    ): CommonDialog {
        this.contentRes = contentRes
        this.contentGravity = gravity
        this.contentSize = contentSize
        return this
    }

    fun content(
        content: String, contentSize: Float? = null, gravity: Int = Gravity.CENTER,
    ): CommonDialog {
        this.content = content
        this.contentGravity = gravity
        this.contentSize = contentSize
        return this
    }

    fun onConfirm(
        confirmRes: Int,
        block: CommonDialog.() -> Unit = { this.dismiss() },
    ): CommonDialog {
        this.confirmRes = confirmRes
        this.confirmInvoke = block
        return this
    }

    fun onConfirm(
        confirmText: String,
        block: CommonDialog.() -> Unit = { this.dismiss() },
    ): CommonDialog {
        this.confirmText = confirmText
        this.confirmInvoke = block
        return this
    }

    fun onConfirm(
        block: CommonDialog.() -> Unit,
    ): CommonDialog {
        this.confirmRes = R.string.dialog_ok
        this.confirmInvoke = block
        return this
    }

    fun onCancel(
        cancelText: String,
        block: CommonDialog.() -> Unit = { this.dismiss() },
    ): CommonDialog {
        this.cancelText = cancelText
        this.cancelInvoke = block
        return this
    }

    fun onCancel(
        cancelRes: Int,
        block: CommonDialog.() -> Unit = { this.dismiss() },
    ): CommonDialog {
        this.cancelRes = cancelRes
        this.cancelInvoke = block
        return this
    }

    fun onCancel(
        block: CommonDialog.() -> Unit,
    ): CommonDialog {
        this.cancelRes = R.string.dialog_cancel
        this.cancelInvoke = block
        return this
    }

    fun onCancelBg(@DrawableRes cancelBgRes: Int): CommonDialog {
        this.cancelBgRes = cancelBgRes
        return this
    }

    fun onConfirmBg(@DrawableRes confirmBgRes: Int): CommonDialog {
        this.confirmBgRes = confirmBgRes
        return this
    }

    fun padding(topPadding: Int? = null, bottomPadding: Int? = null): CommonDialog {
        this.topPadding = topPadding
        this.bottomPadding = bottomPadding
        return this
    }

    override fun initView(savedInstanceState: Bundle?) {
        this.window?.apply {
            setGravity(Gravity.CENTER)
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        }
        inflateView()
    }

    private fun inflateView() {
        // 标题
        if (isTitle) {
            vb.tvTitle.visibility = View.VISIBLE
            titleSize?.let {
                vb.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
            }
            vb.tvTitle.paint.isFakeBoldText = isTitleBold
            if (titleRes == -1) {
                vb.tvTitle.text = title
            } else {
                vb.tvTitle.setText(titleRes)
            }
        } else {
            vb.tvTitle.visibility = View.GONE
        }

        // 内容
        if (isContent) {
            vb.tvContent.visibility = View.VISIBLE
            contentSize?.let {
                vb.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
            }
            if (contentRes == -1) {
                vb.tvContent.text = content
            } else {
                vb.tvContent.setText(contentRes)
            }
            vb.tvContent.gravity = this.contentGravity
        } else {
            vb.tvContent.visibility = View.GONE
        }

        // 确认
        if (isConfirm) {
            vb.btnPositive.visibility = View.VISIBLE
            if (confirmRes == -1) {
                vb.btnPositive.text = confirmText
            } else {
                vb.btnPositive.setText(confirmRes)
            }
            if (confirmBgRes != -1) {
                vb.btnPositive.setBackgroundResource(confirmBgRes)
            }
            vb.btnPositive.setOnClickListener { confirmInvoke?.invoke(this) }

        } else {
            vb.btnPositive.visibility = View.INVISIBLE
        }

        // 取消
        if (isCancel) {
            vb.btnNegative.visibility = View.VISIBLE
            if (cancelRes == -1) {
                vb.btnNegative.text = cancelText
            } else {
                vb.btnNegative.setText(cancelRes)
            }
            if (cancelBgRes != -1) {
                vb.btnNegative.setBackgroundResource(cancelBgRes)
            }
            vb.btnNegative.setOnClickListener { cancelInvoke?.invoke(this) }

        } else {
            vb.btnNegative.visibility = View.INVISIBLE
        }

        val topPadding = topPadding ?: vb.llContainer.paddingTop
        val bottomPadding = bottomPadding ?: vb.llContainer.paddingBottom
        vb.llContainer.updatePadding(top = topPadding, bottom = bottomPadding)
    }
}