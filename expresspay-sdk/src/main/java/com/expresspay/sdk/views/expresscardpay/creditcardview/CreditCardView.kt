/* Developer: Zohaib Kambrani (a2zzuhaib@gmai.com) */

package com.expresspay.sdk.views.expresscardpay.creditcardview

import android.animation.AnimatorSet
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Point
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.annotation.XmlRes
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.res.use
import com.expresspay.sdk.R
import com.expresspay.sdk.databinding.CardBinding
import com.expresspay.sdk.views.expresscardpay.creditcardview.animations.Animation
import com.expresspay.sdk.views.expresscardpay.creditcardview.animations.RotationAnimation
import com.expresspay.sdk.views.expresscardpay.creditcardview.extensions.*
import com.expresspay.sdk.views.expresscardpay.creditcardview.models.*
import com.expresspay.sdk.views.expresscardpay.creditcardview.util.Expiry
import com.expresspay.sdk.views.expresscardpay.creditcardview.util.Filler
import com.expresspay.sdk.views.expresscardpay.creditcardview.util.NumberFormat
import java.util.*

/**
 * Custom view that displays credit cards' information
 */
class CreditCardView : LinearLayout {
    var binding:CardBinding

    /**
     * Map of all the brands with their own styles
     */
    private val styleMap: StyleMap

    /**
     * Custom touch grid used to detect touch events in the defined grid
     */
    private var touchGrid: TouchGrid?

    /**
     * Data holder for this view, all of the information are stored inside this [CreditCard] instance
     */
    val cardData: CreditCard

    /**
     * Current style applied to the view, it can be 0 on the first
     * initialization or when there's no custom styles applied
     */
    @StyleRes
    var currentStyle: Int
        private set

    /**
     * Whether or not the card should be flipped when it is clicked
     */
    var flipOnClick: Boolean

    /**
     * Whether or not the card should be flipped when any of its field is being edited
     */
    var flipOnEdit: Boolean

    /**
     * Animation played when the card is clicked and [flipOnClick] is set to True
     *
     * By default this will be a [RotationAnimation], which [RotationAnimation.time] is 500ms
     * and the [RotationAnimation.startPoint] is [RotationAnimation.LEFT]
     */
    var flipOnClickAnimation: Animation

    /**
     * Animation played when any of the card's fields are being edited and [flipOnEdit] is set to True
     */
    var flipOnEditAnimation: Animation

    /**
     * Whether or not this View is being animated,
     * if the View is finishing an animation then the [flipOnClick] value should be ignored
     */
    var isAnimating: Boolean
        private set

    /**
     * Whether or not this credit card is flipped
     *
     * True means that the back of the card is showing, False (default) means that the front is showing, changing this value **will not** animate the card
     */
    var isFlipped: Boolean
        private set

    /**
     * Listener invoked when an area of the card is pressed, areas are defined inside [CardArea]
     */
    var areaClickListener: AreaClickListener?
        private set

    /**
     * Number on the credit card's front
     *
     * If [numberFormat] is null no format will be used
     */
    var number: String
        set(value) {
            var result = value

            numberFillers?.let {
                if (it.isNotEmpty()) {
                    for (f in it) {
                        if (result.length <= f.length) {
                            result = f.format(result)
                            break
                        }
                    }
                }
            }

            numberFormat?.let {
                if (result.isNotEmpty()) {
                    result = it.format(result)
                }
            }

            cardData.number = value
            binding.cardNumber.text = result

            restoreStyle()
        }
        get() {
            return cardData.number
        }

    /**
     * Hint of the [number]'s EditText
     */
    var numberHint: String
        set(value) {
            binding.cardNumber.hint = value
            field = value
        }

    /**
     * Size of the [number]'s text, in SP
     */
    var numberSize: Float
        set(value) {
            setNumberSize(TypedValue.COMPLEX_UNIT_SP, value)
            field = value
        }

    /**
     * [number]'s error
     */
    var numberError: String
        set(value) {
            binding.cardNumberError.text = value
            field = value
        }

    /**
     * Holder's name on the credit card's front
     */
    var holder: String
        set(value) {
            cardData.holder = value
            binding.cardHolder.text = value
        }
        get() {
            return cardData.holder
        }

    /**
     * Hint of the [holder]'s EditText
     */
    var holderHint: String
        set(value) {
            binding.cardHolder.hint = value
            field = value
        }

    /**
     * [holder]'s error
     */
    var holderError: String?
        set(value) {
            binding.cardHolderError.text = value
            field = value
        }

    /**
     * [holder]'s label
     */
    var holderLabel: String?
        set(value) {
            binding.cardHolderLabel.text = value
            field = value
        }

    /**
     * CVV code on the credit card's back
     */
    var cvv: String
        set(value) {
            var result = value

            cvvFillers?.let {
                if (it.isNotEmpty()) {
                    for (f in it) {
                        if (value.length <= f.length) {
                            result = f.format(value)
                            break
                        }
                    }
                }
            }

            cardData.cvv = value
            binding.cardCvv.text = result
        }
        get() {
            return cardData.cvv
        }

    /**
     * Whether or not the "/" should be added to the expiry,
     * if you turn this off you'll have to set it by yourself
     */
    var formatExpiry: Boolean
        set(value) {
            field = value
            expiry = expiry
        }

    /**
     * Hint of the [cvv]'s EditText
     */
    var cvvHint: String
        set(value) {
            binding.cardCvv.hint = value
            field = value
        }

    /**
     * [cvv]'s label
     */
    var cvvError: String
        set(value) {
            binding.cardCvvError.text = value
            field = value
        }

    /**
     * Size of the [cvv]'s text, in SP
     */
    var cvvSize: Float
        set(value) {
            binding.cardCvv.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

            field = value
        }

    /**
     * Color of the [cvv]'s text
     */
    @ColorInt
    var cvvTextColor: Int
        set(value) {
            binding.cardCvv.setTextColor(value)
            field = value
        }

    /**
     * Expiration date on the credit card's front
     */
    var expiry: String
        set(value) {
            var result = value

            if (value.length >= 2 && formatExpiry) {
                result = value.substring(0, 2) + "/"

                if (value.length > 2) {
                    result += value.substring(2, value.length)
                }
            }

            cardData.expiry = value
            binding.cardExpiry.text = result
        }
        get() {
            return cardData.expiry
        }

    /**
     * Hint of the [expiry]'s EditText
     */
    var expiryHint: String
        set(value) {
            binding.cardExpiry.hint = value
            field = value
        }

    /**
     * [expiry]'s error
     */
    var expiryError: String
        set(value) {
            binding.cardExpiryError.text = value
            field = value
        }

    /**
     * [expiry]'s label
     */
    var expiryLabel: String?
        set(value) {
            binding.cardExpiryLabel.text = value
            field = value
        }

    /**
     * Credit card's brand
     *
     * This value is directly retrieved from the [cardData] and changes whenever the [number] changes
     */
    val brand: Brand
        get() {
            return cardData.brand
        }

    /**
     * Format used to format the [number] when it is updated, by default the format is "%s4"
     *
     * This value can be set to null to disable the formatter
     */
    var numberFormat: NumberFormat?
        set(value) {
            field = value
            number = number
        }

    /**
     * Fillers used to fill the [cvv] if some digits are still not inserted by the user
     *
     * Take a look at the [Filler] class for more info,
     * fillers will be always applied before the [numberFormat]
     *
     * By default there's no filler set, if you need to remove all fillers either
     * clear the list or set this property to null
     */
    var cvvFillers: List<Filler>?
        set(value) {
            val list = value?.toMutableList()

            list?.let {
                it.sort()

                cvv = cvv
            }

            field = list
        }

    /**
     * Fillers used to fill the [number] if some digits are still not inserted by the user
     *
     * Take a look at the [Filler] class for more info,
     * fillers will be always applied before the [numberFormat]
     *
     * By default there's no filler set, if you need to remove all fillers either
     * clear the list or set this property to null
     */
    var numberFillers: List<Filler>?
        set(value) {
            val list = value?.toMutableList()

            list?.let {
                it.sort()

                number = number
            }

            field = list
        }

    /**
     * Color for all texts that are not labels or errors
     */
    @ColorInt
    var textColor: Int
        set(value) {
            binding.cardNumber.setTextColor(value)
            binding.cardHolder.setTextColor(value)
            binding.cardExpiry.setTextColor(value)
            binding.cardCvv.setTextColor(value)

            field = value
        }

    /**
     * Color for all labels
     */
    @ColorInt
    var labelColor: Int
        set(value) {
            binding.cardHolderLabel.setTextColor(value)
            binding.cardExpiryLabel.setTextColor(value)

            field = value
        }

    /**
     * Color for all errors
     */
    @ColorInt
    var errorColor: Int
        set(value) {
            binding.cardNumberError.setTextColor(value)
            binding.cardHolderError.setTextColor(value)
            binding.cardExpiryError.setTextColor(value)
            binding.cardCvvError.setTextColor(value)

            field = value
        }

    /**
     * Color for all text hints
     */
    @ColorInt
    var hintColor: Int
        set(value) {
            binding.cardNumber.setHintTextColor(value)
            binding.cardHolder.setHintTextColor(value)
            binding.cardExpiry.setHintTextColor(value)
            binding.cardCvv.setHintTextColor(value)

            field = value
        }

    /**
     * Size of all errors, in SP
     */
    var errorSize: Float
        set(value) {
            binding.cardNumberError.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
            binding.cardHolderError.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
            binding.cardExpiryError.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
            binding.cardCvvError.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

            field = value
        }

    /**
     * Size of all labels, in SP
     */
    var labelSize: Float
        set(value) {
            binding.cardHolderLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
            binding.cardExpiryLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

            field = value
        }

    /**
     * Size of all texts, in SP
     */
    var textSize: Float
        set(value) {
            binding.cardNumberError.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
            binding.cardHolderError.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
            binding.cardExpiryError.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
            binding.cardCvvError.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)

            field = value
        }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null
    ) : super(context, attrs) {

        binding = CardBinding.inflate(LayoutInflater.from(context), this, true)
//        inflate(context, R.layout.card, this)

        styleMap = mutableMapOf(
            Brand.AMEX to null,
            Brand.DINERS to null,
            Brand.DISCOVER to null,
            Brand.GENERIC to null,
            Brand.JCB to null,
            Brand.MAESTRO to null,
            Brand.MASTERCARD to null,
            Brand.UNIONPAY to null,
            Brand.VISA to null,
            Brand.MADA to null
        )
        touchGrid = null
        currentStyle = 0
        cardData = CreditCard()
        flipOnClick = false
        flipOnEdit = false
        flipOnClickAnimation = RotationAnimation()
        flipOnEditAnimation = RotationAnimation()
        isAnimating = false
        isFlipped = false
        areaClickListener = null
        numberHint = ""
        numberSize =
            CreditCardView.Companion.TEXT_BIG
        numberError = ""
        holderHint = ""
        holderError = ""
        holderLabel = ""
        formatExpiry = true
        cvvHint = ""
        cvvError = ""
        cvvTextColor = Color.BLACK
        cvvSize =
            CreditCardView.Companion.TEXT_BIG
        expiryHint = ""
        expiryError = ""
        expiryLabel = ""
        numberFormat = null
        cvvFillers = null
        numberFillers = null
        textColor = Color.WHITE
        labelColor =
            CreditCardView.Companion.LABEL_COLOR
        errorColor =
            CreditCardView.Companion.ERROR_COLOR
        hintColor =
            CreditCardView.Companion.LABEL_COLOR
        errorSize =
            CreditCardView.Companion.TEXT_SMALL
        labelSize =
            CreditCardView.Companion.TEXT_SMALL
        textSize =
            CreditCardView.Companion.TEXT_NORMAL

        applyStyle(attrs)
        flip(isFlipped)

        binding.cardHolder.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (flipOnEdit && isFlipped) {
                    flip(flipOnEditAnimation)
                }
            }
        })
        binding.cardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (flipOnEdit && isFlipped) {
                    flip(flipOnEditAnimation)
                }
            }
        })
        binding.cardExpiry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (flipOnEdit && isFlipped) {
                    flip(flipOnEditAnimation)
                }
            }
        })
        binding.cardCvv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (flipOnEdit && !isFlipped) {
                    flip(flipOnEditAnimation)
                }
            }
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x.toInt()
        val y = ev.y.toInt()

        if (flipOnClick) {
            flip(flipOnClickAnimation)
        }

        areaClickListener?.let { callback ->
            // Full grid (3x3)
            with(getPositionOnGrid(3, 3, x, y)) {
                when {
                    contentEquals(intArrayOf(0, 0)) -> callback(this@CreditCardView, CardArea.TOP_LEFT)
                    contentEquals(intArrayOf(1, 0)) -> callback(this@CreditCardView, CardArea.TOP_CENTER)
                    contentEquals(intArrayOf(2, 0)) -> callback(this@CreditCardView, CardArea.TOP_RIGHT)
                    contentEquals(intArrayOf(0, 1)) -> callback(this@CreditCardView, CardArea.CENTER_LEFT)
                    contentEquals(intArrayOf(1, 1)) -> callback(this@CreditCardView, CardArea.CENTER)
                    contentEquals(intArrayOf(2, 1)) -> callback(this@CreditCardView, CardArea.CENTER_RIGHT)
                    contentEquals(intArrayOf(0, 2)) -> callback(this@CreditCardView, CardArea.BOTTOM_LEFT)
                    contentEquals(intArrayOf(1, 2)) -> callback(this@CreditCardView, CardArea.BOTTOM_CENTER)
                    contentEquals(intArrayOf(2, 2)) -> callback(this@CreditCardView, CardArea.BOTTOM_RIGHT)
                }
            }

            // Top and bottom areas
            with(getPositionOnGrid(1, 2, x, y)) {
                when {
                    contentEquals(intArrayOf(0, 0)) -> callback(this@CreditCardView, CardArea.TOP)
                    contentEquals(intArrayOf(0, 1)) -> callback(this@CreditCardView, CardArea.BOTTOM)
                }
            }

            // Left and right areas
            with(getPositionOnGrid(2, 1, x, y)) {
                when {
                    contentEquals(intArrayOf(0, 0)) -> callback(this@CreditCardView, CardArea.LEFT)
                    contentEquals(intArrayOf(1, 0)) -> callback(this@CreditCardView, CardArea.RIGHT)
                }
            }
        }

        touchGrid?.let {
            val pos = getPositionOnGrid(it.rows, it.columns, x, y)

            it.callback(
                this,
                Point(
                    pos[0],
                    pos[1]
                )
            )
        }

        return true
    }

    /**
     * Returns the coordinates on the grid formed by the given [columns] and [rows]
     * of the given points [x] and [y], based on the width and the height of this View
     */
    fun getPositionOnGrid(columns: Int, rows: Int, x: Int, y: Int): IntArray {
        val colSize = width / columns
        val rowSize = height / rows

        for (c in 0..columns) {
            for (r in 0..rows) {
                val topLeft = Point(
                    c * colSize,
                    r * rowSize
                )
                val bottomRight = Point(
                    (c + 1) * colSize,
                    (r + 1) * rowSize
                )

                if (x >= topLeft.x &&
                    x <= bottomRight.x &&
                    y >= topLeft.y &&
                    y <= bottomRight.y
                ) {
                    return intArrayOf(c, r)
                }
            }
        }

        return intArrayOf(-1, -1)
    }

    /**
     * Sets an [AreaClickListener] for this [CreditCardView], this listener
     * will be invoked once the view is clicked and it will tell which area of the card
     * was clicked
     *
     * Note: The callback could be invoked multiple times, since some of the areas overlap, for
     * example [CardArea.LEFT] includes [CardArea.TOP_LEFT], [CardArea.CENTER_LEFT],
     * [CardArea.BOTTOM_LEFT] and half vertical size of the followings: [CardArea.TOP_CENTER],
     * [CardArea.CENTER], [CardArea.BOTTOM_CENTER]
     */
    fun setAreaClickListener(listener: AreaClickListener?) {
        this.areaClickListener = listener
    }

    /**
     * Defines a custom grid, with the given [rows] and [columns], which will be used when detecting touch events
     *
     * For example if you define a 2x2 grid, the card will be divided in 4 areas,
     * when a touch event is fired the [GridClickListener] will be invoked and the
     * clicked area will be passed to the listener
     *
     * The [setAreaClickListener] uses the same logic with a 3x3 grid
     *
     * @see setAreaClickListener
     */
    fun setGridClickListener(rows: Int, columns: Int, listener: GridClickListener) {
        this.touchGrid = TouchGrid(
            columns,
            rows,
            listener
        )
    }

    /**
     * Flips the card without playing any animation
     *
     * The given [flipped] value sets the new value to [isFlipped] and changes
     * the card's to its relative flipped state
     *
     * Note: This method will execute even if [isAnimating] is True
     */
    fun flip(flipped: Boolean) {
        val outView = if (!flipped) binding.cardBack else binding.cardFront
        val inView = if (!flipped) binding.cardFront else binding.cardBack

        outView.rotationY = 180F
        outView.alpha = 0F
        inView.rotationY = 360F
        inView.alpha = 1F

        isFlipped = flipped
    }

    /**
     * Flips the card using the given [animation] and invokes the
     * callbacks [onStart] when the animation starts and [onEnd] when the animation ends
     */
    @JvmOverloads
    fun flip(
        animation: Animation = RotationAnimation(),
        onStart: Callback = {},
        onEnd: Callback = {}
    ) {
        flip(
            animation.animate(
                binding.cardFront,
                binding.cardBack,
                this
            ),
            onStart,
            onEnd
        )
    }

    /**
     * Flips the card using the given [AnimatorSet], the [onStart] and [onEnd] callbacks
     * are invoked when the animation starts and when it ends, respectively
     *
     * Note: If you have a lot of animators you should consider creating your own [Animation]
     */
    @JvmOverloads
    fun flip(
        animators: AnimatorSet,
        onStart: Callback = {},
        onEnd: Callback = {}
    ) {
        if (!isAnimating) {
            isFlipped = !isFlipped

            animators.apply {
                doOnStart {
                    isAnimating = true
                    onStart()
                }
                doOnEnd {
                    isAnimating = false
                    onEnd()
                }

                start()
            }
        }
    }

    /**
     * Clears all the error fields
     *
     * This will empty [cvvError], [expiryError], [holderError] and [numberError]
     */
    fun clearErrors() {
        cvvError = ""
        expiryError = ""
        holderError = ""
        numberError = ""
    }

    /**
     * Clears all the card's fields
     *
     * This will empty [cvv], [number], [holder] and [expiry]
     */
    fun clearFields() {
        cvv = ""
        number = ""
        holder = ""
        expiry = ""
    }

    /**
     * Clears all the text and error fields
     *
     * This is the same thing as calling both [clearErrors] and [clearFields]
     */
    fun clearAll() {
        clearErrors()
        clearFields()
    }

    /**
     * Sets the [numberFormat] from a string
     */
    fun setNumberFormat(format: String) {
        numberFormat = NumberFormat(format)
    }

    /**
     * Sets the expiry from the given [date]
     */
    fun setExpiry(date: Date) {
        this.expiry = Expiry.from(date)
    }

    /**
     * Sets the expiry from the given [month] and [year]
     */
    fun setExpiry(month: Number, year: Number) {
        this.expiry = Expiry.from(month, year)
    }

    /**
     * Sets the expiry from a [calendar]
     */
    fun setExpiry(calendar: Calendar) {
        this.expiry = Expiry.from(calendar)
    }

    /**
     * Sets the [cardData] value from the given parameters
     */
    fun setCardData(holder: String, number: String, cvv: String, expiry: String) {
        this.holder = holder
        this.number = number
        this.cvv = cvv
        this.expiry = expiry
    }

    /**
     * Sets the [holder]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setHolderSize(unit: Int, size: Float) {
        binding.cardHolder.setTextSize(unit, size)
    }

    /**
     * Sets the [holderLabel]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setHolderLabelSize(unit: Int, size: Float) {
        binding.cardHolderLabel.setTextSize(unit, size)
    }

    /**
     * Sets the [holderError]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setHolderErrorSize(unit: Int, size: Float) {
        binding.cardHolder.setTextSize(unit, size)
    }

    /**
     * Sets the [number]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setNumberSize(unit: Int, size: Float) {
        binding.cardNumber.setTextSize(unit, size)
    }

    /**
     * Sets the [numberError]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setNumberErrorSize(unit: Int, size: Float) {
        binding.cardNumber.setTextSize(unit, size)
    }

    /**
     * Sets the [expiry]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setExpirySize(unit: Int, size: Float) {
        binding.cardExpiry.setTextSize(unit, size)
    }

    /**
     * Sets the [expiryLabel]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setExpiryLabelSize(unit: Int, size: Float) {
        binding.cardExpiryLabel.setTextSize(unit, size)
    }

    /**
     * Sets the [expiryError]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setExpiryErrorSize(unit: Int, size: Float) {
        binding.cardExpiryError.setTextSize(unit, size)
    }

    /**
     * Sets the [cvv]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setCvvSize(unit: Int, size: Float) {
        binding.cardNumber.setTextSize(unit, size)
    }

    /**
     * Sets the [cvv]'s text size with the given [unit], which can be
     * for example [TypedValue.COMPLEX_UNIT_PX] or [TypedValue.COMPLEX_UNIT_SP]
     */
    fun setCvvErrorSize(unit: Int, size: Float) {
        binding.cardExpiryError.setTextSize(unit, size)
    }

    /**
     * Sets the [cvvFillers] from an xml resource
     */
    fun setCvvFillers(@XmlRes resId: Int) {
        cvvFillers = Filler.parseList(resources.getXml(resId))
    }

    /**
     * Sets the [numberFormat] from an xml resource
     */
    fun setNumberFillers(@XmlRes resId: Int) {
        numberFillers = Filler.parseList(resources.getXml(resId))
    }

    /**
     * Adds the given [textWatcher] to the TextView associated with the given [input]
     */
    fun addInputListener(input: CardInput, textWatcher: TextWatcher) {
        when (input) {
            CardInput.HOLDER -> binding.cardHolder.addTextChangedListener(textWatcher)
            CardInput.NUMBER -> binding.cardNumber.addTextChangedListener(textWatcher)
            CardInput.EXPIRY -> binding.cardExpiry.addTextChangedListener(textWatcher)
            CardInput.CVV -> binding.cardCvv.addTextChangedListener(textWatcher)
        }
    }

    /**
     * Removes the given [textWatcher] from the TextView associated with the given [input]
     */
    fun removeInputListener(input: CardInput, textWatcher: TextWatcher) {
        when (input) {
            CardInput.HOLDER -> binding.cardHolder.removeTextChangedListener(textWatcher)
            CardInput.NUMBER -> binding.cardNumber.removeTextChangedListener(textWatcher)
            CardInput.EXPIRY -> binding.cardExpiry.removeTextChangedListener(textWatcher)
            CardInput.CVV -> binding.cardCvv.removeTextChangedListener(textWatcher)
        }
    }

    /**
     * Pairs the given [input] with the given [field]
     *
     * This will fill the card's information when the [field] is being edited
     */
    fun pairInput(input: CardInput, field: TextView) {
        when (input) {
            CardInput.HOLDER -> field.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    holder = s.toString()
                }
            })
            CardInput.NUMBER -> field.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    number = s.toString()
                }

            })
            CardInput.EXPIRY -> field.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    expiry = s.toString()
                }

            })
            CardInput.CVV -> field.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    cvv = s.toString()
                }

            })
        }
    }

    /**
     * Changes the style of the given [brand]
     *
     * This will update the view only if the given brand is the same as the currentBrand
     */
    fun setStyle(brand: Brand, @StyleRes style: Int) {
        styleMap[brand] = style

        if (this.brand == brand) {
            applyStyle(brand)
        }
    }

    /**
     * Applies the attribute of the style associated with the given [brand]
     *
     * This is called automatically whenever the [number] changes
     */
    fun applyStyle(brand: Brand) {
        styleMap[brand]?.let { style ->
            if (currentStyle != style) {
                currentStyle = style

                context.obtainStyledAttributes(style, R.styleable.CreditCardView).use { array ->
                    onStyle(array)
                }
            }
        }
    }

    /**
     * Applies the attributes of the given style
     *
     * This will override the current style, call [restoreStyle] to re-apply the style
     * associated with the current [brand]
     */
    fun applyStyle(@StyleRes styleRes: Int) {
        if (currentStyle != styleRes) {
            currentStyle = styleRes

            context.obtainStyledAttributes(styleRes, R.styleable.CreditCardView).use {
                onStyle(it)
            }
        }
    }

    /**
     * Restores the style according to the current [brand]
     */
    fun restoreStyle() {
        applyStyle(brand)
    }

    /**
     * Applies the attributes from the given [set] to the view
     *
     * This is the only method that loads the different styles
     */
    private fun applyStyle(set: AttributeSet?) {
        context.obtainStyledAttributes(set, R.styleable.CreditCardView).use {
            styleMap[Brand.AMEX] = it.getResourceId(
                R.styleable.CreditCardView_amexStyle,
                R.style.DefaultAmex
            )
            styleMap[Brand.DINERS] = it.getResourceId(
                R.styleable.CreditCardView_dinersStyle,
                R.style.DefaultDiners
            )
            styleMap[Brand.DISCOVER] = it.getResourceId(
                R.styleable.CreditCardView_discoverStyle,
                R.style.DefaultDiscover
            )
            styleMap[Brand.GENERIC] = it.getResourceId(
                R.styleable.CreditCardView_genericStyle,
                R.style.DefaultGeneric
            )
            styleMap[Brand.JCB] = it.getResourceId(
                R.styleable.CreditCardView_jcbStyle,
                R.style.DefaultJcb
            )
            styleMap[Brand.MAESTRO] = it.getResourceId(
                R.styleable.CreditCardView_maestroStyle,
                R.style.DefaultMaestro
            )
            styleMap[Brand.MASTERCARD] = it.getResourceId(
                R.styleable.CreditCardView_mastercardStyle,
                R.style.DefaultMastercard
            )
            styleMap[Brand.UNIONPAY] = it.getResourceId(
                R.styleable.CreditCardView_unionpayStyle,
                R.style.DefaultUnionpay
            )
            styleMap[Brand.VISA] = it.getResourceId(
                R.styleable.CreditCardView_visaStyle,
                R.style.DefaultVisa
            )
            styleMap[Brand.MADA] = it.getResourceId(
                R.styleable.CreditCardView_madaStyle,
                R.style.DefaultMada
            )

            onStyle(it, true)
        }
    }

    /**
     * Applies the attributes retrieved from the given [typedArray]
     *
     * This method is a generic method that applies the attributes,
     * the properties of the view must be already initialized since their values
     * will be used as default values in case some of the attributes are missing
     */
    private fun onStyle(
        typedArray: TypedArray,
        firstRun: Boolean = false
    ) {
        with(typedArray) {
            binding.cardFront.background = getDrawable(
                R.styleable.CreditCardView_cardFrontBackground,
                binding.cardFront.background
            )
            binding.cardBack.background = getDrawable(
                R.styleable.CreditCardView_cardBackBackground,
                binding.cardBack.background
            )
            binding.cardFrontLogo.setImageDrawable(
                getDrawable(
                    R.styleable.CreditCardView_cardFrontLogo,
                    binding.cardFrontLogo.drawable
                )
            )
            binding.cardBackLogo.setImageDrawable(
                getDrawable(
                    R.styleable.CreditCardView_cardBackLogo,
                    binding.cardBackLogo.drawable
                )
            )
            binding.cardFrontLogo.setSizes(
                getDimensionPixelSize(
                    R.styleable.CreditCardView_cardFrontLogoSize,
                    CreditCardView.Companion.LOGO_FRONT
                )
            )
            binding.cardBackLogo.setSizes(
                getDimensionPixelSize(
                    R.styleable.CreditCardView_cardBackLogoSize,
                    CreditCardView.Companion.LOGO_BACK
                )
            )
            binding.cardCvv.background = getDrawable(
                R.styleable.CreditCardView_cvvBackground,
                binding.cardCvv.background
            )
            binding.cardSignStrip.background = getDrawable(
                R.styleable.CreditCardView_signStripBackground,
                binding.cardSignStrip.background
            )
            binding.cardMagneticStrip.background = getDrawable(
                R.styleable.CreditCardView_magneticStripBackground,
                binding.cardMagneticStrip.background
            )

            numberFillers = getXmlOrNull(R.styleable.CreditCardView_cardNumberFillers) {
                Filler.parseList(it)
            } ?: numberFillers
            cvvFillers = getXmlOrNull(R.styleable.CreditCardView_cardCvvFillers) {
                Filler.parseList(it)
            } ?: cvvFillers
            numberFormat = NumberFormat(
                getString(R.styleable.CreditCardView_cardNumberFormat) ?: numberFormat?.format
            )
            textColor = getColor(R.styleable.CreditCardView_cardTextColor, textColor)
            textSize = getDimensionFontSize(R.styleable.CreditCardView_cardTextSize, textSize)
            labelColor = getColor(R.styleable.CreditCardView_cardLabelColor, labelColor)
            labelSize = getDimensionFontSize(R.styleable.CreditCardView_cardLabelSize, labelSize)
            errorColor = getColor(R.styleable.CreditCardView_cardLabelColor, errorColor)
            errorSize = getDimensionFontSize(R.styleable.CreditCardView_cardLabelSize, errorSize)
            hintColor = getColor(R.styleable.CreditCardView_cardHintColor, hintColor)
            holderHint = getString(R.styleable.CreditCardView_cardHolderHint) ?: holderHint
            holderError = getString(R.styleable.CreditCardView_cardHolderError) ?: holderError
            holderLabel = getString(R.styleable.CreditCardView_cardHolderLabel) ?: holderLabel
            numberSize = getDimensionFontSize(R.styleable.CreditCardView_cardNumberSize, numberSize)
            numberError = getString(R.styleable.CreditCardView_cardNumberError) ?: numberError
            numberHint = getString(R.styleable.CreditCardView_cardNumberHint) ?: numberHint
            cvvHint = getString(R.styleable.CreditCardView_cardCvvHint) ?: cvvHint
            cvvError = getString(R.styleable.CreditCardView_cardCvvError) ?: cvvError
            cvvTextColor = getColor(R.styleable.CreditCardView_cardCvvTextColor, cvvTextColor)
            cvvSize = getDimensionFontSize(R.styleable.CreditCardView_cardCvvSize, cvvSize)
            expiryHint = getString(R.styleable.CreditCardView_cardExpiryHint) ?: expiryHint
            expiryError = getString(R.styleable.CreditCardView_cardExpiryError) ?: expiryError
            expiryLabel = getString(R.styleable.CreditCardView_cardExpiryLabel) ?: expiryLabel
            isFlipped = getBoolean(R.styleable.CreditCardView_cardIsFlipped, isFlipped)
            flipOnClick = getBoolean(R.styleable.CreditCardView_flipOnClick, flipOnClick)
            flipOnEdit = getBoolean(R.styleable.CreditCardView_flipOnEdit, flipOnEdit)
            formatExpiry = getBoolean(R.styleable.CreditCardView_autoFormatExpiry, formatExpiry)

            if (firstRun) {
                cvv = getString(R.styleable.CreditCardView_cardCvv) ?: cvv
                expiry = getString(R.styleable.CreditCardView_cardExpiry) ?: expiry
                holder = getString(R.styleable.CreditCardView_cardHolder) ?: holder
                number = getString(R.styleable.CreditCardView_cardNumber) ?: number
            }

        }
    }

    companion object {
        private const val TEXT_BIG = 18F
        private const val TEXT_NORMAL = 16F
        private const val TEXT_SMALL = 10F
        private val LOGO_BACK = 42.px
        private val LOGO_FRONT = 48.px
        private val LABEL_COLOR = Color.parseColor("#dddddd")
        private val ERROR_COLOR = Color.parseColor("#f44336")
    }
}