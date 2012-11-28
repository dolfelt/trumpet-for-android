package com.ofamilymedia.trumpet.controls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;

public class TweetAutoComplete extends AutoCompleteTextView {
	private String previous = "";
	private String next = "";
	private String seperator = " ";
	public TweetAutoComplete(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		this.setThreshold(0);
		int inputType = this.getInputType();
		inputType &= ~EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
		this.setRawInputType(inputType);
	}

	public TweetAutoComplete(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.setThreshold(0);
		int inputType = this.getInputType();
		inputType &= ~EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
		this.setRawInputType(inputType);
	}

	public TweetAutoComplete(final Context context) {
		super(context);
		this.setThreshold(0);
		int inputType = this.getInputType();
		inputType &= ~EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
		this.setRawInputType(inputType);
	}

	  /**
	  * This method filters out the existing text till the separator
	  * and launched the filtering process again
	  */

	@Override
	protected void performFiltering(final CharSequence text, final int keyCode) {
		int position = this.getSelectionEnd();
		String filterText = text.toString();
		next = filterText.substring(position);
		
		filterText = filterText.substring(0, position);
		Pattern search = Pattern.compile("(.*)(@|#)[A-Za-z0-9]+$");
		Matcher match = search.matcher(filterText);
		
		if(!match.matches()) return;
		
		int previousAt = filterText.lastIndexOf("@");
		int previousHash = filterText.lastIndexOf("#");

		if(previousAt > -1 && previousAt > previousHash) {
			previous = filterText.substring(0,previousAt);
			filterText = filterText.substring(previousAt);
		} else if(previousHash > -1) {
			previous = filterText.substring(0,previousHash);
			filterText = filterText.substring(previousHash);
		} else {
			return;
		}
		
		//if(!TextUtils.isEmpty(filterText) && filterText.length() > 1){
			//Log.w("SEND", filterText);
			super.performFiltering(filterText, keyCode);
		//}
	}

	/**
	* After a selection, capture the new value and append to the existing
	* text
	*/

	@Override
	protected void replaceText(final CharSequence text) {

		  super.replaceText(previous+text+getSeperator()+next);
	}

	public String getSeperator() {
		return seperator;
	}
	public void setSeperator(final String seperator) {
		this.seperator = seperator;
	}

}
