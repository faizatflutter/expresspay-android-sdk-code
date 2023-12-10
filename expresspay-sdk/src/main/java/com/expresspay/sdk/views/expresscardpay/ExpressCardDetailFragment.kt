package com.expresspay.sdk.views.expresscardpay

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.expresspay.sdk.R
import com.expresspay.sdk.databinding.FragmentExpressCardPayBinding
import com.expresspay.sdk.model.request.card.ExpresspayCard
import com.expresspay.sdk.model.response.base.error.ExpresspayError
import com.expresspay.sdk.model.response.gettransactiondetails.ExpresspayGetTransactionDetailsSuccess
import com.expresspay.sdk.model.response.sale.ExpresspaySaleResponse
import com.expresspay.sdk.toolbox.serializable
import com.expresspay.sdk.views.expresscardpay.creditcardview.models.Brand
import com.expresspay.sdk.views.expresscardpay.creditcardview.models.CardInput
import com.expresspay.sdk.views.expresscardpay.creditcardview.util.NumberFormat

internal class ExpressCardDetailFragment(
    val onSubmit:((ExpresspayCard) -> Unit)?,
    val amount:String,
    val currency:String
    ) : Fragment(), TextWatcher, OnFocusChangeListener{

    lateinit var binding: FragmentExpressCardPayBinding
    var currentView:View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpressCardPayBinding.inflate(inflater, container, false)

        binding.txtName.hint = getString(R.string.card_holder)
        binding.txtCVV.hint = getString(R.string.cvv)
        binding.txtExpiry.hint = getString(R.string.expiry)
        binding.txtNumber.hint = getString(R.string.card_number)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addTextWatcher()
        makeInputsFocusable()
        bindCardViewWithInputs()


        with(binding) {
            lblAmount.setText(amount)
            lblCurrency.setText(currency)
            btnPay.setOnClickListener {
                val cardDetail = binding.card.cardData
                val cardNo = cardDetail.unformattedNumber
                val cardCvv = cardDetail.cvv
                val month = cardDetail.expiryMonth()
                val year = cardDetail.expiryYear()
                if(month == null || year == null || cardNo.isEmpty() || cardCvv.isEmpty()){
                    return@setOnClickListener
                }
                val card = ExpresspayCard(cardDetail.unformattedNumber, month, year, cardDetail.cvv)
                onSubmit?.let {
                    it(card)
                    requireActivity().finish()
                }
            }
        }
    }

    private fun bindCardViewWithInputs(){
        binding.card.pairInput(CardInput.HOLDER, binding.txtName)
        binding.card.pairInput(CardInput.NUMBER, binding.txtNumber)
//            binding.card.pairInput(CardInput.EXPIRY, binding.txtExpiry)
        binding.card.pairInput(CardInput.CVV, binding.txtCVV)
    }

    private fun makeInputsFocusable(){
        binding.txtName.onFocusChangeListener = this
        binding.txtNumber.onFocusChangeListener = this
        binding.txtExpiry.onFocusChangeListener = this
        binding.txtCVV.onFocusChangeListener = this
    }

    private fun addTextWatcher(){
        binding.txtName.addTextChangedListener(this)
        binding.txtNumber.addTextChangedListener(this)
        binding.txtExpiry.addTextChangedListener(this)
        binding.txtCVV.addTextChangedListener(this)
    }

    private fun removeTextWatcher(){
        binding.txtName.removeTextChangedListener(this)
        binding.txtNumber.removeTextChangedListener(this)
        binding.txtExpiry.removeTextChangedListener(this)
        binding.txtCVV.removeTextChangedListener(this)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(chars: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun afterTextChanged(text: Editable?) {
        removeTextWatcher()

        when (currentView) {
            binding.txtName -> cardNameChanged(text.toString())
            binding.txtNumber -> cardNumberChanged(text.toString())
            binding.txtExpiry -> cardExpiryChanged(text.toString())
            binding.txtCVV -> cardCvvChanged(text.toString())
            else -> {}
        }

        binding.btnPay.isEnabled =
            binding.card.cardData.isExpiryValid()
                    && binding.card.cardData.isNumberValid()
//                    && binding.card.cardData.brand != Brand.GENERIC
                    && binding.card.cardData.number.replace(" ", "").trim().length == 16
                    && binding.card.cardData.isCvvValid()
                    && binding.card.holder.length > 3

        addTextWatcher()
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        currentView = v
        if(currentView == binding.txtCVV)
            binding.card.flip()
    }

    private fun cardNameChanged(text:String){
        binding.txtName.setText(text.uppercase())
        binding.txtName.setSelection(text.length)
    }

   private fun cardNumberChanged(text:String){
        val unformatted = text.replace(" ","")
        val formatted = NumberFormat("%d4 %d4 %d4 %d4").format(unformatted)
        binding.txtNumber.setText(formatted)
        binding.txtNumber.setSelection(formatted.length)

        if(text.isEmpty()){
            binding.txtName.requestFocus()
        }else if(unformatted.length == 16){
            binding.txtExpiry.requestFocus()
        }
    }

    private fun cardExpiryChanged(text:String){
        // Format the Input in Card Expiry Format (12/22)
        val unformatted = text.replace("/","")
        var formatted = unformatted
        if(unformatted.length > 2){
            formatted = NumberFormat("%d2/%d2").format(unformatted)
            binding.txtExpiry.setText(formatted)
            binding.txtExpiry.setSelection(formatted.length)
        }else{
            binding.txtExpiry.setText(unformatted)
            binding.txtExpiry.setSelection(unformatted.length)
        }

        binding.card.expiry = unformatted

        if(text.isEmpty()){
            binding.txtNumber.requestFocus()
        }else if(unformatted.length == 4){
            binding.txtCVV.requestFocus()
        }
    }

    private fun cardCvvChanged(text:String){
        if(text.isEmpty()){
            binding.txtExpiry.requestFocus()
        }else if(text.length == 4){
            binding.txtCVV.clearFocus()
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentView?.windowToken, 0)
    }

}