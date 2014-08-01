package mikhail.kalashnikov.flags;

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class AnswerActivity extends Activity {
	public final static String URL = "url";
	public final static String NAME = "name";
//	private ProgressDialog mProgress;
	private WebView page;
//	private boolean isLoadingFinished = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.answer);
		page = (WebView) findViewById(R.id.webView1);
		page.getSettings().setBuiltInZoomControls(true);
		page.getSettings().setCacheMode(WebSettings.LOAD_NORMAL);
//		final Context ctxt=this;

//		page.setWebViewClient(new WebViewClient() {
//			@Override
//			public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				isLoadingFinished = false;
//				view.loadUrl(url);
//				
//				return true;
//			}
//			
//			@Override
//			public void onLoadResource(WebView view, String url) {
//				if(mProgress == null){
//					mProgress = new ProgressDialog(ctxt);
//					mProgress.setTitle(getString(R.string.web));
//					mProgress.setMessage(getString(R.string.web_loading));
//					mProgress.setCancelable(true);
//					
//					mProgress.setOnCancelListener(new OnCancelListener() {
//						
//						@Override
//						public void onCancel(DialogInterface dialog) {
//							if(!isLoadingFinished){
//								finish();
//							}
//						}
//					});
//					
//					mProgress.show();
//				}
//
//				isLoadingFinished = false;
//			}
//
//			@Override
//			public void onPageFinished(WebView view, String url) {
//				super.onPageFinished(view, url);
//				if (mProgress.isShowing()) {
//					mProgress.dismiss();
//					mProgress = null;
//				}
//				isLoadingFinished = true;
//			}
//
//			@Override
//			public void onReceivedError(WebView view, int errorCode,
//					String description, String failingUrl) {
//				if (mProgress.isShowing()) {
//					mProgress.dismiss();
//				}
//				super.onReceivedError(view, errorCode, description, failingUrl);
//			}
//
//		});

		this.setTitle(getIntent().getExtras().getString(NAME));
		page.loadUrl(getIntent().getExtras().getString(URL));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && page.canGoBack()) {
	        page.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	 }
}
